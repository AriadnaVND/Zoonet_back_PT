package com.tecsup.pe.back_zonet.repository;

import com.tecsup.pe.back_zonet.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SupportRepository extends JpaRepository<SupportTicket, Long> {

    // 📱 Usado por la App Móvil: Obtiene los tickets de un cliente específico
    List<SupportTicket> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 💻 Usado por el Administrador: Obtiene absolutamente todos los tickets del sistema ordenados por los más recientes
    List<SupportTicket> findAllByOrderByCreatedAtDesc();
}