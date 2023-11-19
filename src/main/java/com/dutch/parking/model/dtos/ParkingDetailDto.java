package com.dutch.parking.model.dtos;


import com.dutch.parking.misc.ParkingStatusEnum;
import com.dutch.parking.model.ParkingDetail;
import com.dutch.parking.model.ParkingMonitoringDetail;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;


import java.time.LocalDateTime;

@Data
@Builder
public class ParkingDetailDto {

	@NotEmpty(message = "Licence number of the car is required")
	@Size(min = 2, max = 10, message = "The length of licence number must be between 2 and 10 characters.")
	private String licenceNumber;

	@NotNull(message = "Street Name is required")
	private String streetName;

	public ParkingDetail toParkingDetails(){
		return new ParkingDetail().setLicenceNumber(licenceNumber)
				.setStreetName(streetName)
				.setRegisterDatetime(LocalDateTime.now())
				.setParkingStatus(ParkingStatusEnum.PARKING_REGISTERED.getValue());
	}

	public ParkingMonitoringDetail toParkingMonitoringDetail(){
		return new ParkingMonitoringDetail().setLicenceNumber(licenceNumber)
				.setStreetName(streetName)
				.setRecordingDate(LocalDateTime.now());
	}

}
