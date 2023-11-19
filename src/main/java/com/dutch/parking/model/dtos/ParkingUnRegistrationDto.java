package com.dutch.parking.model.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ParkingUnRegistrationDto {

	@NotEmpty(message = "Licence number of the car is required")
	@Size(min = 2, max = 10, message = "The length of licence number must be between 2 and 10 characters.")
	private String licenceNumber;

}
