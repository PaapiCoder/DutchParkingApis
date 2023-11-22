package com.dutch.parking.repository;

import com.dutch.parking.model.ParkingMonitoringDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ParkingMonitoringRepository  extends JpaRepository<ParkingMonitoringDetail, Long> {
	List<ParkingMonitoringDetail> findByRecordingDateBetween(LocalDateTime previousRecordingDate, LocalDateTime recordingDate);
}
