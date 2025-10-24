package com.tecsup.pe.back_zonet.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private Long id;
    private String title;
    private String message;
    private boolean read;
    private String type; // Ej: LOST_ALERT, LOCATION, REMINDER, INFO
    private LocalDateTime createdAt;
    private String urgencyLevel; // Mapeado para la vista (Premium: Urgente)
}