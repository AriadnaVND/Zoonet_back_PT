package com.tecsup.pe.back_zonet.repository;

import com.tecsup.pe.back_zonet.entity.SafeZone;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SafeZoneRepository extends JpaRepository<SafeZone, Long> {
    List<SafeZone> findByUserId(Long userId);
}
