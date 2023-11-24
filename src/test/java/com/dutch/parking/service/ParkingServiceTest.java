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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

	@Mock
	private ParkingRepository parkingRepository;
	@Mock
	private ParkingPriceRepository priceRepository;
	@Mock
	private ParkingMonitoringRepository parkingMonitoringRepository;

	@InjectMocks
	private ParkingService parkingService;

	static List<PriceDetail> priceList;

	@BeforeAll
	static void beforeAll() {
		PriceDetail pd1 = new PriceDetail().setPrice(BigDecimal.valueOf(3)).setStreetName("Java");
		PriceDetail pd2 = new PriceDetail().setPrice(BigDecimal.valueOf(8)).setStreetName("Azure");
		PriceDetail pd3 = new PriceDetail().setPrice(BigDecimal.valueOf(10)).setStreetName("Jakarta");
		priceList = List.of(pd1,pd2,pd3);

	}

	@Test
	@DisplayName("Registering vehicle test.")
	void registerParkingDetails() throws AlreadyRegisteredException {
		when(parkingRepository.save(any(ParkingDetail.class))).thenReturn(new ParkingDetail().setLicenceNumber("MH14AH9001").setStreetName("Java"));
		ParkingDetail prd = parkingService.registerParkingDetails(ParkingDetailDto.builder().licenceNumber("MH14AH9001")
				.streetName("Java").build().toParkingDetails());
		Assertions.assertEquals(prd.getLicenceNumber(),"MH14AH9001");
		Assertions.assertEquals(prd.getStreetName(),"Java");
	}

	@Test
	@DisplayName("Registering Same vehicle again. Exception : AlreadyRegisteredException.")
	void registerParkingDetailsEx() throws AlreadyRegisteredException {
		when(parkingRepository.findByLicenceNumberAndParkingStatus(any(),any())).thenReturn(Optional.of(new ParkingDetail()
				.setLicenceNumber("MH14AH9001").setStreetName("Java")));
		assertThrows(AlreadyRegisteredException.class,
				()-> when(parkingService.registerParkingDetails(new ParkingDetail().setLicenceNumber("MH14AH9001").setStreetName("Java")))
						.thenThrow(new AlreadyRegisteredException("")));
	}

	@Test
	@DisplayName("De-Register vehicle for 5 minute on Java street. Time 5 min and parking fee 0.15 EUR.")
	void deRegisterParkingDetails() throws RegistrationNotFoundException {
		when(parkingRepository.save(any())).thenReturn(new ParkingDetail().setLicenceNumber("PB12X9002").setStreetName("Java")
				.setUnregisterDatetime(LocalDateTime.of(2023,11, 20,11,5,1))
				.setRegisterDatetime(LocalDateTime.of(2023,11, 20,11,0,1)));
		Mockito.<Optional<ParkingDetail>>when(parkingRepository.findByLicenceNumberAndParkingStatus(any(),any()))
				.thenReturn(Optional.of(new ParkingDetail().setLicenceNumber("PB12X9002").setStreetName("Java")
						.setUnregisterDatetime(LocalDateTime.of(2023,11, 20,11,5,1))
						.setRegisterDatetime(LocalDateTime.of(2023,11, 20,11,0,1))));
		when(priceRepository.findAll()).thenReturn(priceList);
		ParkingResponseDto result = parkingService.deRegisterParkingDetails("PB12X9002");
		//Test
		Assertions.assertEquals(result.getParkingAmount(),BigDecimal.valueOf(0.15));
	}

	@Test
	@DisplayName("De-Register vehicle for which is not parked or already de-registered. Exception : RegistrationNotFoundException.")
	void deRegisterParkingDetailsEx() throws RegistrationNotFoundException {
		Mockito.<Optional<ParkingDetail>>when(parkingRepository.findByLicenceNumberAndParkingStatus(any(),any()))
				.thenReturn(Optional.empty());
		assertThrows(RegistrationNotFoundException.class,
				()-> when(parkingService.deRegisterParkingDetails("")).thenThrow(new RegistrationNotFoundException("")));
	}

	@Test
	@DisplayName("De-Register vehicle for 8 minute on Jakarta street. Time 8 min and parking fee 0.8 EUR.")
	void deRegisterParkingDetails2() throws RegistrationNotFoundException {
		when(parkingRepository.save(any())).thenReturn(new ParkingDetail().setParkingStatus("BR13X9897").setStreetName("Jakarta")
				.setRegisterDatetime(LocalDateTime.now().withNano(0).minusMinutes(8))
				.setUnregisterDatetime(LocalDateTime.now().withNano(0)));
		Mockito.<Optional<ParkingDetail>>when(parkingRepository.findByLicenceNumberAndParkingStatus(any(),any()))
				.thenReturn(Optional.of((new ParkingDetail().setParkingStatus("BR13X9897").setStreetName("Jakarta")
						.setRegisterDatetime(LocalDateTime.now().withNano(0).minusMinutes(8))
						.setUnregisterDatetime(LocalDateTime.now().withNano(0)))));
		when(priceRepository.findAll()).thenReturn(priceList);
		ParkingResponseDto result = parkingService.deRegisterParkingDetails("BR13X9897");
		//Test
		Assertions.assertEquals(result.getParkingAmount(),BigDecimal.valueOf(0.8));
	}

	@Test
	@DisplayName("De-Register vehicle for 25 minute on Jakarta street. Time 25 min and parking fee 2 EUR.")
	void deRegisterParkingDetails3() throws RegistrationNotFoundException {
		when(parkingRepository.save(any())).thenReturn(new ParkingDetail().setParkingStatus("BH13X9897").setStreetName("Azure")
				.setRegisterDatetime(LocalDateTime.now().withNano(0).minusMinutes(25))
				.setUnregisterDatetime(LocalDateTime.now().withNano(0)));
		Mockito.<Optional<ParkingDetail>>when(parkingRepository.findByLicenceNumberAndParkingStatus(any(),any())).thenReturn(Optional.of(new ParkingDetail().setParkingStatus("BH13X9897").setStreetName("Azure")
				.setRegisterDatetime(LocalDateTime.now().withNano(0).minusMinutes(25))
				.setUnregisterDatetime(LocalDateTime.now().withNano(0))));
		when(priceRepository.findAll()).thenReturn(priceList);
		ParkingResponseDto result = parkingService.deRegisterParkingDetails("PB12X9002");
		//Test
		Assertions.assertEquals(result.getParkingAmount(),BigDecimal.valueOf(2));
	}

	@Test
	@DisplayName("De-registering vehicle on Sunday. It's registered on the same Sunday. Parking fee 0 Euros")
	void deRegisterParkingDetailsSundayOnly() throws RegistrationNotFoundException {
		when(parkingRepository.save(any())).thenReturn(new ParkingDetail().setLicenceNumber("PB12X9002").setStreetName("Azure")
				.setRegisterDatetime(LocalDateTime.of(2023,11, 19,7,0,1))
				.setUnregisterDatetime(LocalDateTime.of(2023,11, 19,11,0,1)));
		Mockito.<Optional<ParkingDetail>>when(parkingRepository.findByLicenceNumberAndParkingStatus(any(),any()))
				.thenReturn(Optional.of(new ParkingDetail().setLicenceNumber("PB12X9002").setStreetName("Azure")
						.setRegisterDatetime(LocalDateTime.of(2023,11, 19,7,0,1))
						.setUnregisterDatetime(LocalDateTime.of(2023,11, 19,11,0,0))));
		when(priceRepository.findAll()).thenReturn(priceList);
		ParkingResponseDto result = parkingService.deRegisterParkingDetails("PB12X9002");
		//Test
		Assertions.assertEquals(result.getParkingAmount(),BigDecimal.valueOf(0));
	}

	@Test
	@DisplayName("De-registering vehicle on Sunday. It's registered on day which is not Sunday. Parking fee must be > 0 Euros")
	void deRegisterParkingDetailsDRSunday() throws RegistrationNotFoundException {
		when(parkingRepository.save(any())).thenReturn(new ParkingDetail().setLicenceNumber("PB12X9002").setStreetName("Azure")
				.setRegisterDatetime(LocalDateTime.of(2023,11, 18,7,0,1))
				.setUnregisterDatetime(LocalDateTime.of(2023,11, 19,11,0,1)));
		Mockito.<Optional<ParkingDetail>>when(parkingRepository.findByLicenceNumberAndParkingStatus(any(),any()))
				.thenReturn(Optional.of(new ParkingDetail().setLicenceNumber("PB12X9002").setStreetName("Azure")
						.setRegisterDatetime(LocalDateTime.of(2023,11, 18,7,0,1))
						.setUnregisterDatetime(LocalDateTime.of(2023,11, 19,11,0,0))));
		when(priceRepository.findAll()).thenReturn(priceList);
		ParkingResponseDto result = parkingService.deRegisterParkingDetails("PB12X9002");
		//Test
		Assertions.assertEquals(result.getParkingAmount(),BigDecimal.valueOf(62.4));
	}

	@Test
	@DisplayName("De-registering vehicle on day which is not Sunday. It's registered on Sunday. Parking fee must be > 0 Euros")
	void deRegisterParkingDetailsRSunday() throws RegistrationNotFoundException {
		when(parkingRepository.save(any())).thenReturn(new ParkingDetail().setLicenceNumber("PB12X9002").setStreetName("Azure")
				.setRegisterDatetime(LocalDateTime.of(2023,11, 19,7,0,1))
				.setUnregisterDatetime(LocalDateTime.of(2023,11, 20,11,0,1)));
		Mockito.<Optional<ParkingDetail>>when(parkingRepository.findByLicenceNumberAndParkingStatus(any(),any()))
				.thenReturn(Optional.of(new ParkingDetail().setLicenceNumber("PB12X9002").setStreetName("Azure")
						.setRegisterDatetime(LocalDateTime.of(2023,11, 19,7,0,1))
						.setUnregisterDatetime(LocalDateTime.of(2023,11, 20,11,0,0))));
		when(priceRepository.findAll()).thenReturn(priceList);
		ParkingResponseDto result = parkingService.deRegisterParkingDetails("PB12X9002");
		//Test
		Assertions.assertEquals(result.getParkingAmount(),BigDecimal.valueOf(14.4));
	}

	@Test
	@DisplayName("De-registering vehicle on Sunday and also registered on Sunday but different date. Parking fee must be > 0 Euros")
	void deRegisterParkingDetailsSundayOnly2daya() throws RegistrationNotFoundException {
		when(parkingRepository.save(any())).thenReturn(new ParkingDetail().setLicenceNumber("PB12X9002").setStreetName("Azure")
				.setRegisterDatetime(LocalDateTime.of(2023,11, 5,7,0,1))
				.setUnregisterDatetime(LocalDateTime.of(2023,11, 12,11,0,1)));
		Mockito.<Optional<ParkingDetail>>when(parkingRepository.findByLicenceNumberAndParkingStatus(any(),any()))
				.thenReturn(Optional.of(new ParkingDetail().setLicenceNumber("PB12X9002").setStreetName("Azure")
						.setRegisterDatetime(LocalDateTime.of(2023,11, 5,7,0,1))
						.setUnregisterDatetime(LocalDateTime.of(2023,11, 12,11,0,0))));
		when(priceRepository.findAll()).thenReturn(priceList);
		ParkingResponseDto result = parkingService.deRegisterParkingDetails("PB12X9002");
		//Test
		Assertions.assertEquals(result.getParkingAmount(),BigDecimal.valueOf(374.4));
	}


	@Test
	@DisplayName("Uploading monitored vehicles collected during automated vehicle monitoring.")
	void uploadMonitoringDetails() {
		ParkingMonitoringDetail pmd1 = new ParkingMonitoringDetail().setLicenceNumber("PB12x1234")
				.setStreetName("Java").setRecordingDate(LocalDateTime.now().withNano(0));
		ParkingMonitoringDetail pmd2 = new ParkingMonitoringDetail().setLicenceNumber("HP12x1234")
				.setStreetName("Jakarta").setRecordingDate(LocalDateTime.now().withNano(0));
		ParkingMonitoringDetail pmd3 = new ParkingMonitoringDetail().setLicenceNumber("MH12x1234")
				.setStreetName("Azure").setRecordingDate(LocalDateTime.now().withNano(0));
		when(parkingMonitoringRepository.saveAll(any())).thenReturn(List.of(pmd1,pmd2,pmd3));

		List<ParkingMonitoringDetail> monitoringDetails = parkingService.uploadMonitoringDetails(List.of(pmd1, pmd2, pmd3));
		assertEquals(monitoringDetails.size(),3);
	}

	@Test
	@DisplayName("Get list of vehicle which are parked without registration.")
	void listUnregisteredVehicles() {
		ParkingMonitoringDetail pmd1 = new ParkingMonitoringDetail().setLicenceNumber("PB12x1234")
				.setStreetName("Java").setRecordingDate(LocalDateTime.now().withNano(0));
		ParkingMonitoringDetail pmd2 = new ParkingMonitoringDetail().setLicenceNumber("HP12x1234")
				.setStreetName("Jakarta").setRecordingDate(LocalDateTime.now().withNano(0));
		ParkingMonitoringDetail pmd3 = new ParkingMonitoringDetail().setLicenceNumber("MH12x1234")
				.setStreetName("Azure").setRecordingDate(LocalDateTime.now().withNano(0));

		ParkingDetail pd1 = new ParkingDetail().setParkingStatus(ParkingStatusEnum.PARKING_REGISTERED.getValue())
				.setRegisterDatetime(LocalDateTime.now().withNano(0).minusMinutes(2)).setStreetName("Java")
				.setLicenceNumber("PB12x1234").setUnregisterDatetime(LocalDateTime.now().withNano(0));
		ParkingDetail pd2 = new ParkingDetail().setParkingStatus(ParkingStatusEnum.PARKING_REGISTERED.getValue())
				.setRegisterDatetime(LocalDateTime.now().withNano(0)).setStreetName("Java")
				.setLicenceNumber("PB13x1234").setUnregisterDatetime(LocalDateTime.now().withNano(0));
		when(parkingMonitoringRepository.findByRecordingDateBetween(any(),any())).thenReturn(List.of(pmd1,pmd2,pmd3));
		when(parkingRepository.findByRegisterDatetimeBetween(any(),any())).thenReturn(List.of(pd1,pd2));
		List<ReportDetails> reportDetails = parkingService.listUnregisteredVehicles();
		assertEquals(reportDetails.size(), 2);
	}
}
