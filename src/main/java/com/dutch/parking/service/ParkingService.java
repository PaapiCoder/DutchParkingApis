package com.dutch.parking.service;

import com.dutch.parking.exceptions.AlreadyRegisteredException;
import com.dutch.parking.exceptions.RegistrationNotFoundException;
import com.dutch.parking.misc.ParkingStatusEnum;
import com.dutch.parking.misc.Utilities;
import com.dutch.parking.model.ParkingDetail;
import com.dutch.parking.model.ParkingMonitoringDetail;
import com.dutch.parking.model.PriceDetail;
import com.dutch.parking.model.dtos.ParkingDetailDto;
import com.dutch.parking.model.dtos.ParkingResponseDto;
import com.dutch.parking.report.ReportDetails;
import com.dutch.parking.repository.ParkingMonitoringRepository;
import com.dutch.parking.repository.ParkingPriceRepository;
import com.dutch.parking.repository.ParkingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.DayOfWeek.SUNDAY;

@Service
public class ParkingService {

	private final Long DAILY_FREE_MINUTE = 660l;

	private final ParkingRepository parkingRepository;

	private final ParkingPriceRepository priceRepository;

	private final ParkingMonitoringRepository parkingMonitoringRepository;

	public ParkingService(ParkingRepository parkingRepository, ParkingPriceRepository priceRepository,
	                      ParkingMonitoringRepository parkingMonitoringRepository) {
		this.parkingRepository = parkingRepository;
		this.priceRepository = priceRepository;
		this.parkingMonitoringRepository = parkingMonitoringRepository;
	}

	/**
	 *
	 * @param parkingDetail
	 * @return
	 * @throws AlreadyRegisteredException
	 */
	public ParkingDetail registerParkingDetails(ParkingDetail parkingDetail) throws AlreadyRegisteredException {
		var alreadyRegistered = parkingRepository.findByLicenceNumberAndParkingStatus(parkingDetail.getLicenceNumber(),
				ParkingStatusEnum.PARKING_REGISTERED.getValue());
		if(alreadyRegistered.isPresent()){
			throw new AlreadyRegisteredException("Licence Number is already registered at " + parkingDetail.getStreetName() +
					" street, Please Unregister.");
		}
		return parkingRepository.save(parkingDetail);
	}

	/**
	 *
	 *
	 * @param licence
	 * @return
	 * @throws RegistrationNotFoundException
	 */
	public ParkingResponseDto deRegisterParkingDetails(String licence) throws RegistrationNotFoundException {
		Optional<ParkingDetail> detail = parkingRepository.findByLicenceNumberAndParkingStatus(licence,
						ParkingStatusEnum.PARKING_REGISTERED.getValue());
		if (detail.isEmpty()) {
			throw new RegistrationNotFoundException("Parking registration is not found for licence number : "
					+ licence +" Or already De-registration");
		}
		var priceMap = priceRepository.findAll().stream().collect(Collectors
				.toMap(PriceDetail::getStreetName, PriceDetail::getPrice));
		return this.calculateParkingCost(detail.get(), priceMap );
	}

	public List<ParkingMonitoringDetail> uploadMonitoringDetails(List<ParkingMonitoringDetail> data){
		return parkingMonitoringRepository.saveAll(data);
	}

	/**
	 * Calculate the parking amount with help of registration and de-registered time and price for street
	 * where licence number or vehicle is parked.
	 *
	 * @param parkingDetail parked vehicle detail based on licence number.
	 * @return message with parking amount.
	 */
	private ParkingResponseDto calculateParkingCost(ParkingDetail parkingDetail, Map<String, BigDecimal> prices) {
		var updatedParkingDetails = parkingRepository.save(parkingDetail
				.setParkingStatus(ParkingStatusEnum.PARKING_DE_REGISTERED.getValue())
				.setUnregisterDatetime(LocalDateTime.now().withNano(0)));

		Long minutes = calculateParkingMinutes(updatedParkingDetails);

		var parkingAmount = prices.get(updatedParkingDetails.getStreetName()).multiply(BigDecimal.valueOf(minutes));
		return new ParkingResponseDto( "You have successfully De-Registered you vehicle. Total Time : "
				+ minutes+" min", parkingAmount.divide(BigDecimal.valueOf(100)));
	}

	private Long calculateParkingMinutes(ParkingDetail pd) {
		Long minutes = null;
		LocalDateTime updatedRegisterDateTime = LocalDateTime.of(pd.getRegisterDatetime().toLocalDate(),
				Utilities.isBetweenNineAndEight(pd.getRegisterDatetime().toLocalTime(),ParkingStatusEnum.PARKING_REGISTERED.getValue()));
		LocalDateTime updatedDeRegisterDateTime = LocalDateTime.of(pd.getUnregisterDatetime().toLocalDate(),
				Utilities.isBetweenNineAndEight(pd.getUnregisterDatetime().toLocalTime(),ParkingStatusEnum.PARKING_DE_REGISTERED.getValue()));
		//total minutes
		minutes = ChronoUnit.MINUTES.between(updatedRegisterDateTime, updatedDeRegisterDateTime);

		if(ChronoUnit.DAYS.between(updatedRegisterDateTime,updatedDeRegisterDateTime)>0){
			minutes = minutes - ChronoUnit.DAYS.between(updatedRegisterDateTime,updatedDeRegisterDateTime)*DAILY_FREE_MINUTE;
		}
		if(updatedDeRegisterDateTime.getDayOfWeek().equals(SUNDAY)){
			minutes = minutes - ChronoUnit.MINUTES.between(LocalTime.of(8,00,00),updatedDeRegisterDateTime.toLocalTime());
		}
		if(updatedRegisterDateTime.getDayOfWeek().equals(SUNDAY)){
			minutes = minutes - ChronoUnit.MINUTES.between(updatedRegisterDateTime.toLocalTime(), LocalTime.of(21,00,00));
		}
		if(updatedRegisterDateTime.getDayOfWeek().equals(SUNDAY)&&updatedDeRegisterDateTime.getDayOfWeek().equals(SUNDAY)
			&& Utilities.isSameDay(updatedDeRegisterDateTime,updatedRegisterDateTime)){
			minutes=0l;
		}
		return minutes;
	}

	/**
	 * Job to generate the fine report.
	 */
	public List<ReportDetails> listUnregisteredVehicles() {
		LocalDate date = LocalDateTime.now().toLocalDate();
		//All Parked vehicle details for the day
		var parkingDetails = parkingRepository.findByRegisterDatetimeBetween(
				LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date,LocalTime.MAX));
		//All monitoring details of parked vehicle.
		var monitoringDetails = parkingMonitoringRepository.findByRecordingDateBetween(
				LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date,LocalTime.MAX));

		List<ParkingMonitoringDetail> reportData = new ArrayList<>();

		//Registered parking record doesn't contain monitoring record.
		monitoringDetails.forEach(item->{
			if (!parkingDetails.stream().map(m -> m.getLicenceNumber()).toList().contains(item.getLicenceNumber())){
				reportData.add(item);
			}
		});

		// Monitoring record contain vehicle number but parked outside there registration  time period.
		reportData.addAll(monitoringDetails.stream()
				.filter(m -> parkingDetails.stream()
						.anyMatch(p -> m.getLicenceNumber().equals(p.getLicenceNumber())
								&& m.getStreetName().equals(p.getStreetName())
								&& m.getRecordingDate().isAfter(p.getUnregisterDatetime())
								&& m.getRecordingDate().isBefore(p.getRegisterDatetime())))
				.collect(Collectors.toList()));

		return reportData.stream().map(item->new ReportDetails(item.getLicenceNumber(),
				item.getStreetName(),item.getRecordingDate())).collect(Collectors.toList());

	}
}
