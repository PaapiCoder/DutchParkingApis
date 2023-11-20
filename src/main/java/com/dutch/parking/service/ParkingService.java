package com.dutch.parking.service;

import com.dutch.parking.misc.ParkingStatusEnum;
import com.dutch.parking.model.ParkingDetail;
import com.dutch.parking.model.PriceDetail;
import com.dutch.parking.model.dtos.ParkingResponseDto;
import com.dutch.parking.repository.ParkingPriceRepository;
import com.dutch.parking.repository.ParkingRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ParkingService {

	private final ParkingRepository parkingRepository;

	private final ParkingPriceRepository priceRepository;

	public ParkingService(ParkingRepository parkingRepository, ParkingPriceRepository priceRepository) {
		this.parkingRepository = parkingRepository;
		this.priceRepository = priceRepository;
	}

	/**
	 * Calculate the parking amount with help of registration and de-registered time and price for street
	 * where licence number or vehicle is parked.
	 *
	 * @param parkingDetail parked vehicle detail based on licence number.
	 * @return message with parking amount.
	 */
	public ParkingResponseDto calculateParkingCost(ParkingDetail parkingDetail) {
		ParkingDetail updatedParkingDetails = parkingRepository.save(parkingDetail
				.setParkingStatus(ParkingStatusEnum.PARKING_DE_REGISTERED.getValue())
				.setUnregisterDatetime(LocalDateTime.now()));

		LocalDateTime localDateTime = updatedParkingDetails.getUnregisterDatetime().minusMinutes(updatedParkingDetails
				.getRegisterDatetime().getMinute());

		Map<String, BigDecimal> collect = priceRepository.findAll().stream().collect(Collectors
				.toMap(PriceDetail::getStreetName, PriceDetail::getPrice));

		BigDecimal parkingAmount = collect.get(updatedParkingDetails.getStreetName()).multiply(BigDecimal.valueOf(localDateTime.getMinute()));
		return new ParkingResponseDto("You have successfully De-Registered you vehicle. Total Time : "
				+ localDateTime.getMinute()+" min", parkingAmount);
	}
}
