package com.dutch.parking.controller;

import com.dutch.parking.misc.ParkingStatusEnum;
import com.dutch.parking.model.ParkingDetail;
import com.dutch.parking.model.ParkingMonitoringDetail;
import com.dutch.parking.model.dtos.ParkingDetailDto;
import com.dutch.parking.model.dtos.ParkingMonitoringDto;
import com.dutch.parking.model.dtos.ParkingResponseDto;
import com.dutch.parking.model.dtos.ParkingUnRegistrationDto;
import com.dutch.parking.report.ReportDetails;
import com.dutch.parking.service.ParkingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

	@Test
	@DisplayName("Register A vehicle for parking")
	void registerForParking() throws Exception {
		//build request body
		ParkingDetailDto input = ParkingDetailDto.builder()
				.licenceNumber("PB12x0007")
				.streetName("Java Testing Street")
				.build();
		//call controller endpoints
		Mockito.when(parkingService.registerParkingDetails(ArgumentMatchers.any())).thenReturn(input.toParkingDetails());
		mockMvc.perform(post("/api/register").contentType(MediaType.APPLICATION_JSON)
						.characterEncoding("utf-8")
						.content(asJsonString(input))
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.licenceNumber", Matchers.equalTo("PB12x0007")))
				.andExpect(jsonPath("$.streetName", Matchers.equalTo("Java Testing Street")));
	}

	@Test
	@DisplayName("De-register vehicle already parked and calculate parking fee.")
	void deRegisterForParking() throws Exception {
		//build request body
		ParkingUnRegistrationDto input = new ParkingUnRegistrationDto();
		input.setLicenceNumber("PB12X1000");

		ParkingDetail pd = new ParkingDetail();
		pd.setLicenceNumber(input.getLicenceNumber());
		pd.setStreetName("Java Testing Street");
		pd.setParkingStatus(ParkingStatusEnum.PARKING_DE_REGISTERED.getValue());
		pd.setRegisterDatetime(LocalDateTime.now().withNano(0).minusMinutes(5));
		pd.setUnregisterDatetime(LocalDateTime.now().withNano(0));
		Optional<ParkingDetail> op = Optional.of(pd);
		Mockito.when(parkingService.deRegisterParkingDetails(ArgumentMatchers.any())).thenReturn(new ParkingResponseDto
				("You have successfully De-Registered you vehicle. Total Time : " + 5+" min",BigDecimal.ZERO));
		mockMvc.perform(post("/api/unregister").contentType(MediaType.APPLICATION_JSON)
						.characterEncoding("utf-8")
						.content(asJsonString(input))
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.parkingAmount", Matchers.equalTo(BigDecimal.ZERO.intValue())));
	}

	@Test
	@DisplayName("Load list of vehicles found during monitoring in to DB.")
	void loadListVehicleTest() throws Exception {
		//build request body
		ParkingMonitoringDto monitoringDto = new ParkingMonitoringDto();
		ParkingMonitoringDetail pmd1 = new ParkingMonitoringDetail().setLicenceNumber("PB12x1234")
				.setStreetName("Java").setRecordingDate(LocalDateTime.now().withNano(0));
		ParkingMonitoringDetail pmd2 = new ParkingMonitoringDetail().setLicenceNumber("HP12x1234")
				.setStreetName("Jakarta").setRecordingDate(LocalDateTime.now().withNano(0));
		ParkingMonitoringDetail pmd3 = new ParkingMonitoringDetail().setLicenceNumber("MH12x1234")
				.setStreetName("Azure").setRecordingDate(LocalDateTime.now().withNano(0));
		monitoringDto.setParkingMonitoringDetails(List.of(pmd1,pmd2,pmd3));
		//call controller endpoints
		Mockito.when(parkingService.uploadMonitoringDetails(ArgumentMatchers.any())).thenReturn(List.of(pmd1,pmd2,pmd3));
		mockMvc.perform(post("/api/loadParkingRecordList").contentType(MediaType.APPLICATION_JSON)
						.characterEncoding("utf-8")
						.content(asJsonString(monitoringDto))
						.accept(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$[0].licenceNumber", Matchers.equalTo("PB12x1234")))
				.andExpect(jsonPath("$[1].licenceNumber", Matchers.equalTo("HP12x1234")))
				.andExpect(jsonPath("$", Matchers.hasSize(3)));
	}
	@Test
	@DisplayName("Fetch all the data to generate fines for.")
	void fineReportListVehicleTest() throws Exception {
		ReportDetails reportDetails0 = new ReportDetails("UP14X8976", "Java", LocalDateTime.now().withNano(0).minusMinutes(50));
		ReportDetails reportDetails1 = new ReportDetails("PB13X8976", "Jakarta", LocalDateTime.now().withNano(0).minusMinutes(200));
		ReportDetails reportDetails2 = new ReportDetails("MH15X8976", "Azure", LocalDateTime.now().withNano(0).minusDays(500));

		List<ReportDetails> dataList = List.of(reportDetails1, reportDetails2, reportDetails0);

		Mockito.when(parkingService.listUnregisteredVehicles()).thenReturn(dataList);

		mockMvc.perform(get("/api/notRegisteredVehicleReport").contentType(MediaType.APPLICATION_JSON)
						.characterEncoding("utf-8")
						.accept(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", Matchers.hasSize(3)));
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
