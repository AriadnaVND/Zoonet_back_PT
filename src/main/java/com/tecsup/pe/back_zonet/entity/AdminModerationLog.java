package com.tecsup.pe.back_zonet.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp; // IMPORTANTE
import java.time.LocalDateTime;

@Entity
@Table(name = "moderation_logs")
@Data
public class AdminModerationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long postId;

    private String status;
    private Double aiScore;

    @Column(columnDefinition = "TEXT")
    private String aiReason;

    @CreationTimestamp // Se llena solo automáticamente
    @Column(updatable = false)
    private LocalDateTime createdAt;
}