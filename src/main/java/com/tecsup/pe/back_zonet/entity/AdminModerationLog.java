package com.tecsup.pe.back_zonet.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "moderation_logs")
@Data
public class AdminModerationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long postId; // Relación con el ID de CommunityPost
    private String status; // Ej: PENDING, APPROVED, REJECTED, MANUAL_REVIEW
    private Double aiScore;

    @Column(columnDefinition = "TEXT")
    private String aiReason;

    private LocalDateTime createdAt = LocalDateTime.now();
}