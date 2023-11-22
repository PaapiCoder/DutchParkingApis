package com.dutch.parking.service;

import com.dutch.parking.exceptions.AlreadyRegisteredException;
import com.dutch.parking.exceptions.RegistrationNotFoundException;
import com.dutch.parking.model.ParkingDetail;
import com.dutch.parking.model.PriceDetail;
import com.dutch.parking.model.dtos.ParkingDetailDto;
import com.dutch.parking.model.dtos.ParkingResponseDto;
import com.dutch.parking.repository.ParkingMonitoringRepository;
import com.dutch.parking.repository.ParkingPriceRepository;
import com.dutch.parking.repository.ParkingRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
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

	static ParkingDetail rpd;
	static ParkingDetail urpd;

	static List<PriceDetail> priceList;

	@BeforeAll
	static void beforeAll() {
		rpd = new ParkingDetail().setLicenceNumber("MH14AH9001").setStreetName("Java");
		urpd = new ParkingDetail().setLicenceNumber("PB12X9002").setStreetName("Azure")
				.setUnregisterDatetime(LocalDateTime.now());
		PriceDetail pd1 = new PriceDetail().setPrice(BigDecimal.valueOf(3)).setStreetName("Java");
		PriceDetail pd2 = new PriceDetail().setPrice(BigDecimal.valueOf(8)).setStreetName("Azure");
		PriceDetail pd3 = new PriceDetail().setPrice(BigDecimal.valueOf(10)).setStreetName("Jakarta");
		priceList = List.of(pd1,pd2,pd3);

	}

	@Test
	void registerParkingDetails() throws AlreadyRegisteredException {
		when(parkingRepository.save(any(ParkingDetail.class))).thenReturn(rpd);
		ParkingDetail prd = parkingService.registerParkingDetails(ParkingDetailDto.builder().licenceNumber("MH14AH9001")
				.streetName("Java").build().toParkingDetails());
		Assertions.assertEquals(prd.getLicenceNumber(),"MH14AH9001");
		Assertions.assertEquals(prd.getStreetName(),"Java");

	}

	@Test
	void deRegisterParkingDetails() throws RegistrationNotFoundException {
		when(parkingRepository.save(any())).thenReturn(urpd);
		Mockito.<Optional<ParkingDetail>>when(parkingRepository.findByLicenceNumberAndParkingStatus(any(),any()))
				.thenReturn(Optional.of(urpd.setRegisterDatetime(LocalDateTime.now().minusMinutes(2))));
		when(priceRepository.findAll()).thenReturn(priceList);
		ParkingResponseDto result = parkingService.deRegisterParkingDetails("PB12X9002");
		//Test
		Assertions.assertEquals(result.getParkingAmount(),BigDecimal.valueOf(16));
	}

	@Test
	void deRegisterParkingDetails2() throws RegistrationNotFoundException {
		when(parkingRepository.save(any())).thenReturn(urpd.setParkingStatus("BR13X9897").setStreetName("Jakarta").setRegisterDatetime(LocalDateTime.now().minusMinutes(8)));
		Mockito.<Optional<ParkingDetail>>when(parkingRepository.findByLicenceNumberAndParkingStatus(any(),any())).thenReturn(Optional.of(urpd));
		when(priceRepository.findAll()).thenReturn(priceList);
		ParkingResponseDto result = parkingService.deRegisterParkingDetails("PB12X9002");
		//Test
		Assertions.assertEquals(result.getParkingAmount(),BigDecimal.valueOf(80));
	}

	@Test
	void deRegisterParkingDetails3() throws RegistrationNotFoundException {
		when(parkingRepository.save(any())).thenReturn(urpd.setParkingStatus("BH13X9897").setStreetName("Azure").setRegisterDatetime(LocalDateTime.now().minusMinutes(25)));
		Mockito.<Optional<ParkingDetail>>when(parkingRepository.findByLicenceNumberAndParkingStatus(any(),any())).thenReturn(Optional.of(urpd));
		when(priceRepository.findAll()).thenReturn(priceList);
		ParkingResponseDto result = parkingService.deRegisterParkingDetails("PB12X9002");
		//Test
		Assertions.assertEquals(result.getParkingAmount(),BigDecimal.valueOf(200));
	}

	@Test
	void uploadMonitoringDetails() {
	}

	@Test
	void listUnregisteredVehicles() {
	}
}
