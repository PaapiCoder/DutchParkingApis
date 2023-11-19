package com.dutch.parking.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
@Data
@Entity
@Accessors(chain = true)
@Table(name = "ParkingDetail")
public class ParkingDetail {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name = "licence_number")
	private String licenceNumber;

	@Column(name = "street_name")
	private String streetName;

	@Column(name = "register_datetime")
	private LocalDateTime registerDatetime;

	@Column(name = "unregister_datetime")
	private LocalDateTime unregisterDatetime;

	@Column(name = "parking_status")
	private String parkingStatus;

}
