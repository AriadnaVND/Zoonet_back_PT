package com.tecsup.pe.back_zonet.repository;

import com.tecsup.pe.back_zonet.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // Importar List

public interface SupportRepository extends JpaRepository<SupportTicket, Long> {
    // 🟢 Método para obtener todos los tickets de un usuario, ordenados por fecha
    List<SupportTicket> findByUserIdOrderByCreatedAtDesc(Long userId);
}