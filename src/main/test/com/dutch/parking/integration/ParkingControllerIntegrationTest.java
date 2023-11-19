package com.dutch.parking.integration;

import com.dutch.parking.controller.ParkingController;
import com.dutch.parking.model.dtos.ParkingDetailDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.containers.MySQLContainer;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ParkingControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	static MySQLContainer mySQLContainer = new MySQLContainer("mysql:latest");


	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
		registry.add("spring.datasource.username", mySQLContainer::getUsername);
		registry.add("spring.datasource.password", mySQLContainer::getPassword);
	}

	@BeforeAll
	static void beforeAll() {
		mySQLContainer.start();
	}

	@AfterAll
	static void afterAll() {
		mySQLContainer.stop();
	}

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders
				.standaloneSetup(ParkingController.class)
				.build();
	}

	@Ignore
	public void registerForParking() throws Exception {
		//build request body
		ParkingDetailDto course = ParkingDetailDto.builder()
				.licenceNumber("PB12x0007")
				.streetName("Java Testing Street")
				.build();
		//call controller endpoints
		mockMvc.perform(MockMvcRequestBuilders
						.post("/api/register")
						.contentType("application/json")
						.content(asJsonString(course))
						.accept("application/json"))
				.andExpect(status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
	}

	private String asJsonString(Object object) {
		try {
			return new ObjectMapper().writeValueAsString(object);
		} catch (JsonProcessingException e) {
			e.getStackTrace();
		}
		return null;
	}
}
