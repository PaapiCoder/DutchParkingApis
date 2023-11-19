package com.dutch.parking.repository;


import com.dutch.parking.model.PriceDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingPriceRepository extends JpaRepository<PriceDetail, Long> {
}
