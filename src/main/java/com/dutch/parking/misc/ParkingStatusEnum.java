package com.dutch.parking.misc;

import lombok.Getter;

@Getter
public enum ParkingStatusEnum {
	PARKING_REGISTERED("Registered"),
	PARKING_DE_REGISTERED("DeRegistered");
	private final String value;

	ParkingStatusEnum(String value) {
		this.value = value;
	}
}
