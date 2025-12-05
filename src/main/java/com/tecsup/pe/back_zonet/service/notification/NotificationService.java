package com.tecsup.pe.back_zonet.service.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
// No importamos la Notification de Firebase aquí arriba para evitar conflictos con tu Entidad
import com.tecsup.pe.back_zonet.dto.NotificationDTO;
import com.tecsup.pe.back_zonet.entity.Notification; // Tu entidad
import com.tecsup.pe.back_zonet.entity.User;
import com.tecsup.pe.back_zonet.repository.NotificationRepository;
import com.tecsup.pe.back_zonet.repository.UserRepository;
import com.tecsup.pe.back_zonet.util.RoleValidator;
import lombok.extern.slf4j.Slf4j; // 🟢 Importante para logs
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j // 🟢 Anotación para activar el log (log.info, log.error)
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
     * Guarda en BD y envía Push Notification a Firebase.
     */
    public Notification createSystemNotification(Long recipientUserId, String title, String message, String type, String urgencyLevel) {
        // 1. Validar usuario
        User recipient = userRepository.findById(recipientUserId)
                .orElseThrow(() -> new RuntimeException("Usuario receptor no encontrado"));

        // 2. Crear objeto entidad para BD
        Notification notification = new Notification();
        notification.setRecipientUser(recipient);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type != null ? type : "INFO");

        // Lógica Premium / SMS
        boolean isPremium = roleValidator.isPremiumUser(recipientUserId);
        notification.setUrgencyLevel(urgencyLevel != null ? urgencyLevel : "MEDIUM");

        if (isPremium && urgencyLevel != null && urgencyLevel.equalsIgnoreCase("HIGH")) {
            notification.setSentViaSMS(true);
        } else {
            notification.setSentViaSMS(false);
        }

        // 3. Guardar en Base de Datos
        Notification savedNotification = notificationRepository.save(notification);

        // 4. 🚀 Lógica de Notificación Push (Firebase)
        // Verificamos si el usuario tiene un token guardado en su perfil
        if (recipient.getFcmToken() != null && !recipient.getFcmToken().isEmpty()) {
            sendPushNotification(recipient.getFcmToken(), title, message, type);
        } else {
            log.warn("El usuario ID {} no tiene token FCM. No se envió push.", recipientUserId);
        }

        return savedNotification;
    }

    /**
     * 🟢 Método privado para enviar a Firebase Cloud Messaging
     */
    private void sendPushNotification(String token, String title, String body, String type) {
        try {
            // Construir la notificación visual de Firebase
            // Usamos el nombre completo de la clase para no confundirla con tu entidad 'Notification'
            com.google.firebase.messaging.Notification firebaseNotif = com.google.firebase.messaging.Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // Construir el mensaje
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(firebaseNotif)
                    .putData("type", type != null ? type : "INFO") // Data extra para que el celular sepa qué hacer
                    .putData("click_action", "FLUTTER_NOTIFICATION_CLICK") // Estandar para Flutter/Android
                    .build();

            // Enviar
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Push notification enviada a Firebase: {}", response);

        } catch (Exception e) {
            log.error("Error al enviar Push Notification: {}", e.getMessage());
        }
    }
}