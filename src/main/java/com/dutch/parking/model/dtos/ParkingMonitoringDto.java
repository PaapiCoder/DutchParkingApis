package com.dutch.parking.model.dtos;

import com.dutch.parking.misc.ParkingStatusEnum;
import com.dutch.parking.model.ParkingDetail;
import com.dutch.parking.model.ParkingMonitoringDetail;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ParkingMonitoringDto {
	private List<ParkingMonitoringDetail> parkingMonitoringDetails;
}
