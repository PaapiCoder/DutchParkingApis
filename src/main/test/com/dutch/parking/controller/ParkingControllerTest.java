package com.dutch.parking.controller;

import com.dutch.parking.exceptions.AlreadyRegisteredException;
import com.dutch.parking.misc.ParkingStatusEnum;
import com.dutch.parking.model.ParkingDetail;
import com.dutch.parking.model.dtos.ParkingDetailDto;
import com.dutch.parking.model.dtos.ParkingResponseDto;
import com.dutch.parking.model.dtos.ParkingUnRegistrationDto;
import com.dutch.parking.repository.ParkingMonitoringRepository;
import com.dutch.parking.repository.ParkingRepository;
import com.dutch.parking.service.ParkingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.containers.MySQLContainer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class ParkingControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ParkingService parkingService;
	@MockBean
	private ParkingRepository parkingRepository;

	@MockBean
	private ParkingMonitoringRepository parkingMonitoringRepository;

	@Test
	void registerForParking() throws Exception {
		//build request body
		ParkingDetail input = ParkingDetailDto.builder()
				.licenceNumber("PB12x0007")
				.streetName("Java Testing Street")
				.build().toParkingDetails();
		//call controller endpoints
		Mockito.when(parkingRepository.save(ArgumentMatchers.any())).thenReturn(input);
		mockMvc.perform(post("/api/register").contentType(MediaType.APPLICATION_JSON)
						.characterEncoding("utf-8")
						.content(asJsonString(input))
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.licenceNumber", Matchers.equalTo("PB12x0007")))
				.andExpect(jsonPath("$.streetName", Matchers.equalTo("Java Testing Street")));
	}

	@Test
	void deRegisterForParking() throws Exception {
		//build request body
		ParkingUnRegistrationDto input = new ParkingUnRegistrationDto();
		input.setLicenceNumber("PB12X1000");

		ParkingDetail pd = new ParkingDetail();
		pd.setLicenceNumber(input.getLicenceNumber());
		pd.setStreetName("Java Testing Street");
		pd.setParkingStatus(ParkingStatusEnum.PARKING_DE_REGISTERED.getValue());
		pd.setRegisterDatetime(LocalDateTime.now().minusMinutes(5));
		pd.setUnregisterDatetime(LocalDateTime.now());
		Optional<ParkingDetail> op = Optional.of(pd);
		Mockito.when(parkingRepository.findByLicenceNumberAndParkingStatus(ArgumentMatchers.any(), ArgumentMatchers.any()))
				.thenReturn(op);
		Mockito.when(parkingService.calculateParkingCost(pd)).thenReturn(new ParkingResponseDto("You have successfully De-Registered you vehicle. Total Time : "
				+ 5+" min",BigDecimal.ZERO));
		mockMvc.perform(post("/api/unregister").contentType(MediaType.APPLICATION_JSON)
						.characterEncoding("utf-8")
						.content(asJsonString(input))
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.parkingAmount", Matchers.equalTo(BigDecimal.ZERO.intValue())));
	}

	@Test
	void loadListVehicleTest() throws Exception {
		//build request body
		ParkingDetailDto[] input = {ParkingDetailDto.builder().licenceNumber("PB12x0007").streetName("Java").build(),
									 ParkingDetailDto.builder().licenceNumber("PB12x0009").streetName("Azure").build()};
		//call controller endpoints
		Mockito.when(parkingMonitoringRepository.saveAll(ArgumentMatchers.any())).thenReturn(Arrays.stream(input)
				.map(ParkingDetailDto::toParkingMonitoringDetail).toList());
		mockMvc.perform(post("/api/loadParkingRecordList").contentType(MediaType.APPLICATION_JSON)
						.characterEncoding("utf-8")
						.content(asJsonString(input))
						.accept(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$[0].licenceNumber", Matchers.equalTo("PB12x0007")))
				.andExpect(jsonPath("$[1].licenceNumber", Matchers.equalTo("PB12x0009")))
				.andExpect(jsonPath("$", Matchers.hasSize(2)));
	}

	private String asJsonString(Object object) {
		try {
			ObjectMapper op = new ObjectMapper();
			op.findAndRegisterModules();
			return op.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			e.getStackTrace();
		}
		return null;
	}
}
