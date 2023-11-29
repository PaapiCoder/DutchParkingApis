package com.dutch.parking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "MonitoringDetail")
public class ParkingMonitoringDetail {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@NotEmpty(message = "Licence number of the car is required")
	@Size(min = 2, max = 10, message = "The length of licence number must be between 2 and 10 characters.")
	@Column(name = "licence_number")
	private String licenceNumber;

	@NotNull(message = "Street Name is required")
	@Column(name = "street_name")
	private String streetName;

	@Column(name = "recording_datetime")
	private LocalDateTime recordingDate;

}
