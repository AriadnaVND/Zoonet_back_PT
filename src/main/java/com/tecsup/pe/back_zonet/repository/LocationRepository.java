package com.tecsup.pe.back_zonet.repository;

import com.tecsup.pe.back_zonet.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findFirstByPetIdOrderByTimestampDesc(Long petId);
}
