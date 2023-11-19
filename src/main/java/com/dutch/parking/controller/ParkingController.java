package com.dutch.parking.controller;

import com.dutch.parking.exceptions.AlreadyRegisteredException;
import com.dutch.parking.exceptions.RegistrationNotFoundException;
import com.dutch.parking.misc.ParkingStatusEnum;
import com.dutch.parking.model.ParkingDetail;
import com.dutch.parking.model.ParkingMonitoringDetail;
import com.dutch.parking.model.dtos.ParkingDetailDto;
import com.dutch.parking.model.dtos.ParkingResponseDto;
import com.dutch.parking.model.dtos.ParkingUnRegistrationDto;
import com.dutch.parking.repository.ParkingMonitoringRepository;
import com.dutch.parking.repository.ParkingRepository;
import com.dutch.parking.service.ParkingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ParkingController {

	private final ParkingRepository parkingRepository;

	private final ParkingMonitoringRepository parkingMonitoringRepository;

	private final ParkingService parkingService;

	public ParkingController(ParkingRepository parkingRepository, ParkingMonitoringRepository parkingMonitoringRepository
			, ParkingService parkingService) {
		this.parkingRepository = parkingRepository;
		this.parkingMonitoringRepository = parkingMonitoringRepository;
		this.parkingService = parkingService;
	}

	@PostMapping("/register")
	public ResponseEntity<ParkingDetail> register(@Valid @RequestBody ParkingDetailDto parkingDetail) throws AlreadyRegisteredException {
		Optional<ParkingDetail> alreadyRegistered = parkingRepository
				.findByLicenceNumberAndParkingStatus(parkingDetail.getLicenceNumber(),
						ParkingStatusEnum.PARKING_REGISTERED.getValue());
		if(alreadyRegistered.isPresent()){
			throw new AlreadyRegisteredException("Licence Number is already registered at " + parkingDetail.getStreetName()+ " street, Please Unregister.");
		}
		ParkingDetail detail = parkingRepository.save(parkingDetail.toParkingDetails());
		return new ResponseEntity<>(detail, HttpStatus.CREATED);
	}

	@PostMapping("/unregister")
	public ResponseEntity<ParkingResponseDto> unregister(@RequestBody ParkingUnRegistrationDto details) throws RegistrationNotFoundException {
		Optional<ParkingDetail> parkingDetail = parkingRepository
				.findByLicenceNumberAndParkingStatus(details.getLicenceNumber(),
						ParkingStatusEnum.PARKING_REGISTERED.getValue());
		if (parkingDetail.isEmpty()) {
			throw new RegistrationNotFoundException("Parking registration is not found for licence number : "
					+ details.getLicenceNumber()+" Or already De-registration");
		}
		ParkingResponseDto responseDto = parkingService.calculateParkingCost(parkingDetail.get());
		return new ResponseEntity<>(responseDto, HttpStatus.OK);
	}

	@PostMapping("/loadParkingRecordList")
	public ResponseEntity<List<ParkingMonitoringDetail>> loadLicenceDetails(@RequestBody ParkingDetailDto[] data) {
		List<ParkingMonitoringDetail> insertedDetails = parkingMonitoringRepository.saveAll(Arrays.stream(data)
				.toList().stream().map(ParkingDetailDto::toParkingMonitoringDetail).toList());
		return new ResponseEntity<>(insertedDetails, HttpStatus.OK);
	}
}
