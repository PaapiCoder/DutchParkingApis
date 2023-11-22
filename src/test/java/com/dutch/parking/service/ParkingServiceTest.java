package com.dutch.parking.service;

import com.dutch.parking.misc.ParkingStatusEnum;
import com.dutch.parking.model.ParkingDetail;
import com.dutch.parking.model.PriceDetail;
import com.dutch.parking.model.dtos.ParkingResponseDto;
import com.dutch.parking.repository.ParkingPriceRepository;
import com.dutch.parking.repository.ParkingRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

	@Mock
	private ParkingRepository parkingRepository;
	@Mock
	private ParkingPriceRepository priceRepository;

	@InjectMocks
	private ParkingService parkingService;

	@Test
	void calculateParkingCostTest(){
		//Input
		ParkingDetail pd = new ParkingDetail();
		pd.setLicenceNumber("PB12X9001");
		pd.setStreetName("Java");
		pd.setParkingStatus(ParkingStatusEnum.PARKING_DE_REGISTERED.getValue());
		pd.setRegisterDatetime(LocalDateTime.now().minusMinutes(5));
		pd.setUnregisterDatetime(LocalDateTime.now());

		//when(parkingRepository.save(any(ParkingDetail.class))).thenReturn(pd);

		//Price detail
		PriceDetail price1 = new PriceDetail();
		price1.setStreetName("Java");
		price1.setPrice(BigDecimal.TEN);
		PriceDetail price2 = new PriceDetail();
		price2.setStreetName("Azure");
		price2.setPrice(BigDecimal.valueOf(5));

		//when(priceRepository.findAll()).thenReturn(List.of(price1,price2));

		//ParkingResponseDto prd = parkingService.calculateParkingCost(pd);

		//Assertions.assertEquals(prd.getParkingAmount(),BigDecimal.valueOf(50));

	}
}
