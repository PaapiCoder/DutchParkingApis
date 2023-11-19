package com.dutch.parking.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ParkingResponseDto {

	private String message;

	private BigDecimal parkingAmount;
}
