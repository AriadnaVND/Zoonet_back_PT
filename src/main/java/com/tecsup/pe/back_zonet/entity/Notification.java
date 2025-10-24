package com.tecsup.pe.back_zonet.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User recipientUser; // El usuario que recibe la notificación

    private String title;
    private String message;

    private boolean isRead = false;
    private String type; // Ej: LOST_ALERT, LOCATION, REMINDER

    // Campos avanzados (Premium)
    private String urgencyLevel; // Nivel de urgencia
    private Boolean sentViaSMS = false; // Envío por SMS

    private LocalDateTime createdAt = LocalDateTime.now();
}