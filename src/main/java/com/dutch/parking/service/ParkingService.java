package com.dutch.parking.service;

import com.dutch.parking.exceptions.AlreadyRegisteredException;
import com.dutch.parking.exceptions.RegistrationNotFoundException;
import com.dutch.parking.misc.ParkingStatusEnum;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ParkingService {

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
		return this.calculateParkingCost(detail.get());
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
	private ParkingResponseDto calculateParkingCost(ParkingDetail parkingDetail) {
		var updatedParkingDetails = parkingRepository.save(parkingDetail
				.setParkingStatus(ParkingStatusEnum.PARKING_DE_REGISTERED.getValue())
				.setUnregisterDatetime(LocalDateTime.now()));

		var localDateTime = updatedParkingDetails.getUnregisterDatetime().minusMinutes(updatedParkingDetails
				.getRegisterDatetime().getMinute());

		var collect = priceRepository.findAll().stream().collect(Collectors
				.toMap(PriceDetail::getStreetName, PriceDetail::getPrice));

		var parkingAmount = collect.get(updatedParkingDetails.getStreetName()).multiply(BigDecimal.valueOf(localDateTime.getMinute()));
		return new ParkingResponseDto("You have successfully De-Registered you vehicle. Total Time : "
				+ localDateTime.getMinute()+" min", parkingAmount);
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
