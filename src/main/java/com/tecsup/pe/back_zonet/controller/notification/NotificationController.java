package com.tecsup.pe.back_zonet.controller.notification;

import com.tecsup.pe.back_zonet.dto.NotificationDTO;
import com.tecsup.pe.back_zonet.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 🟢 GET /api/notifications/{userId}
     * ÚNICO ENDPOINT: Permite al frontend ver las notificaciones automáticas.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<NotificationDTO>> getNotifications(@PathVariable Long userId) {
        List<NotificationDTO> notifications = notificationService.getNotificationsByUserId(userId);
        return ResponseEntity.ok(notifications);
    }

    // 🚫 El endpoint POST /send se ha ELIMINADO ya que todas las notificaciones deben ser automáticas.
}