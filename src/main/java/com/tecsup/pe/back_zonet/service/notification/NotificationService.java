package com.tecsup.pe.back_zonet.service.notification;

import com.tecsup.pe.back_zonet.dto.NotificationDTO;
import com.tecsup.pe.back_zonet.entity.Notification;
import com.tecsup.pe.back_zonet.entity.User;
import com.tecsup.pe.back_zonet.repository.NotificationRepository;
import com.tecsup.pe.back_zonet.repository.UserRepository;
import com.tecsup.pe.back_zonet.util.RoleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleValidator roleValidator;

    /**
     * 🟢 GET: Obtiene y mapea las notificaciones de un usuario.
     */
    public List<NotificationDTO> getNotificationsByUserId(Long userId) {
        List<Notification> notifications = notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId);

        return notifications.stream().map(n -> {
            NotificationDTO dto = new NotificationDTO();
            dto.setId(n.getId());
            dto.setTitle(n.getTitle());
            dto.setMessage(n.getMessage());
            dto.setRead(n.isRead());
            dto.setType(n.getType());
            dto.setCreatedAt(n.getCreatedAt());
            dto.setUrgencyLevel(n.getUrgencyLevel());
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 🟢 INTERNO/SISTEMA: Crea y guarda una notificación automática.
     * Llamado SOLAMENTE por otros servicios (ej. LostPetService).
     */
    public Notification createSystemNotification(Long recipientUserId, String title, String message, String type, String urgencyLevel) {
        User recipient = userRepository.findById(recipientUserId)
                .orElseThrow(() -> new RuntimeException("Usuario receptor no encontrado"));

        Notification notification = new Notification();
        notification.setRecipientUser(recipient);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type != null ? type : "INFO");

        // Aplicar lógica de servicio avanzado para usuarios Premium (alerta por SMS)
        boolean isPremium = roleValidator.isPremiumUser(recipientUserId);

        notification.setUrgencyLevel(urgencyLevel != null ? urgencyLevel : "MEDIUM");

        // Si el evento es de alta prioridad y el usuario es Premium, se habilita SMS.
        if (isPremium && urgencyLevel != null && urgencyLevel.equalsIgnoreCase("HIGH")) {
            notification.setSentViaSMS(true);
        } else {
            notification.setSentViaSMS(false);
        }

        return notificationRepository.save(notification);
    }
}