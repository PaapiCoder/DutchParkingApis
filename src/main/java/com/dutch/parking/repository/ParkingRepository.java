package com.dutch.parking.repository;

import com.dutch.parking.model.ParkingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ParkingRepository extends JpaRepository<ParkingDetail, Long> {

	@Query("SELECT pd FROM ParkingDetail pd WHERE pd.licenceNumber = ?1 AND parkingStatus = ?2")
	Optional<ParkingDetail> findByLicenceNumberAndParkingStatus(String licenceNumber, String parkingStatus);

	List<ParkingDetail> findByRegisterDatetimeBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
}
