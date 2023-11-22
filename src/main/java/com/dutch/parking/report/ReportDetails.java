package com.dutch.parking.report;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReportDetails {
	private String licenceNumber;
	private String streetName;
	private LocalDateTime date;
}
