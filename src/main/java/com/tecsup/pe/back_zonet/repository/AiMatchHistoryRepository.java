package com.tecsup.pe.back_zonet.repository;

import com.tecsup.pe.back_zonet.entity.AiMatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la entidad AiMatchHistory (Historial de Coincidencias de IA)
 */
@Repository
public interface AiMatchHistoryRepository extends JpaRepository<AiMatchHistory, Long> {
    // Aquí se podrían añadir métodos personalizados, como:
    // List<AiMatchHistory> findByUserIdOrderBySearchDateDesc(Long userId);
}