package com.tecsup.pe.back_zonet.repository;

import com.tecsup.pe.back_zonet.entity.LostPet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LostPetRepository extends JpaRepository<LostPet, Long> {
    // Cuenta reportes activos (found = false) de un usuario específico.
    @Query("SELECT COUNT(lp) FROM LostPet lp WHERE lp.pet.user.id = :userId AND lp.found = false")
    long countActiveReportsByUserId(@Param("userId") Long userId);

    // Obtener todos los reportes de mascotas perdidas activos.
    List<LostPet> findByFoundFalse();
}