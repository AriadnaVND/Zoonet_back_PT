package com.tecsup.pe.back_zonet.repository;

import com.tecsup.pe.back_zonet.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RouteRepository extends JpaRepository<Location, Long> {
    List<Location> findByPetIdAndTimestampBetweenOrderByTimestampAsc(Long petId, LocalDateTime startDate, LocalDateTime endDate);
}
