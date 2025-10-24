package com.tecsup.pe.back_zonet.repository;

import com.tecsup.pe.back_zonet.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Método para obtener notificaciones por el ID del usuario receptor, ordenadas por más recientes.
    List<Notification> findByRecipientUserIdOrderByCreatedAtDesc(Long recipientUserId);
}