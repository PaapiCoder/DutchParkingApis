package com.dutch.parking.controller;

import com.dutch.parking.exceptions.AlreadyRegisteredException;
import com.dutch.parking.exceptions.RegistrationNotFoundException;
import com.dutch.parking.model.ParkingDetail;
import com.dutch.parking.model.ParkingMonitoringDetail;
import com.dutch.parking.model.dtos.ParkingDetailDto;
import com.dutch.parking.model.dtos.ParkingMonitoringDto;
import com.dutch.parking.model.dtos.ParkingResponseDto;
import com.dutch.parking.model.dtos.ParkingUnRegistrationDto;
import com.dutch.parking.report.ReportDetails;
import com.dutch.parking.service.ParkingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Dutch Parking Application", description = "Dutch Parking APIs")
@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api")
public class ParkingController {

	private final ParkingService parkingService;

	public ParkingController(ParkingService parkingService) {
		this.parkingService = parkingService;
	}

	/**
	 * Create a new Parking Registration with licence number and street name.
	 *
	 * @param parkingDetail contains licence number and street number
	 * @return parking details as response.
	 * @throws AlreadyRegisteredException if user tried to re-register same vehicle.
	 */
	@Operation(summary = "Create a new Parking Registration", tags = { "register", "post" })
	@ApiResponses({
			@ApiResponse(responseCode = "201", content = {
					@Content(schema = @Schema(implementation = ParkingDetailDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", content = { @Content(schema = @Schema()) }) })
	@PostMapping("/register")
	public ResponseEntity<ParkingDetail> register(@Valid @RequestBody ParkingDetailDto parkingDetail) throws AlreadyRegisteredException {
		return new ResponseEntity<>(parkingService.registerParkingDetails(parkingDetail.toParkingDetails()), HttpStatus.CREATED);
	}

	/**
	 * De-registered the parking and calculate parking fee using licence number of vehicle.
	 *
	 * @param details contain licence number of already registered vehicle.
	 * @return provide message and Parking fee.
	 * @throws RegistrationNotFoundException if Registration not found.
	 */
	@Operation(summary = "De-registered the parking and calculate parking fee.", tags = { "unregister", "post" })
	@ApiResponses({
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = ParkingUnRegistrationDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", description = "Parking De-registered", content = {
					@Content(schema = @Schema()) }) })
	@PostMapping("/unregister")
	public ResponseEntity<ParkingResponseDto> unregister(@RequestBody ParkingUnRegistrationDto details) throws RegistrationNotFoundException {
		return new ResponseEntity<>(parkingService.deRegisterParkingDetails(details.getLicenceNumber()), HttpStatus.OK);
	}

	/**
	 * Load list of vehicle licence number collected during monitoring.
	 *
	 * @param data list of all vehicles licence number along with street name.
	 * @return return same as response.
	 */
	@Operation(summary = "Load list of vehicle licence number collected during monitoring", tags = { "loadParkingRecordList", "post" })
	@ApiResponses({
			@ApiResponse(responseCode = "201", content = {
					@Content(schema = @Schema(implementation = ParkingDetailDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", content = { @Content(schema = @Schema()) }) })
	@PostMapping("/loadParkingRecordList")
	public ResponseEntity<List<ParkingMonitoringDetail>> loadLicenceDetails(@RequestBody ParkingMonitoringDto data) {
		return new ResponseEntity<>(parkingService.uploadMonitoringDetails(data.getParkingMonitoringDetails()), HttpStatus.CREATED);
	}

	/**
	 * Create Report for all the vehicles which are parked without any registration.
	 *
	 * @return return same as response.
	 */
	@Operation(summary = "List the Report data for which fine report will be generated.", tags = { "fetchReportData", "get" })
	@ApiResponses({
			@ApiResponse(responseCode = "201", content = {
					@Content(schema = @Schema(implementation = ParkingDetailDto.class), mediaType = "application/json") }),
			@ApiResponse(responseCode = "404", content = { @Content(schema = @Schema()) }) })
	@GetMapping("/notRegisteredVehicleReport")
	public ResponseEntity<List<ReportDetails>> fetchReportData() {
		return new ResponseEntity<>(parkingService.listUnregisteredVehicles(), HttpStatus.OK);
	}
}
