package com.dutch.parking.model;

import jakarta.persistence.*;
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

	@Column(name = "licence_number")
	private String licenceNumber;

	@Column(name = "street_name")
	private String streetName;

	@Column(name = "recording_datetime")
	private LocalDateTime recordingDate;

}
