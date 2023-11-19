package com.dutch.parking.model.dtos;

import lombok.Data;

import java.util.List;

@Data
public class ParkingMonitoringDto {
	private List<ParkingDetailDto> parkingDetailDto;
}
