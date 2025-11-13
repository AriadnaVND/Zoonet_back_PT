package com.tecsup.pe.back_zonet.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

/**
 * Entidad para guardar el historial de coincidencias de IA (AI Matching)
 */
@Entity
@Table(name = "ai_match_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiMatchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Usuario que realizó la búsqueda Premium
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    // Publicación de la comunidad que resultó ser una coincidencia
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_post_id", nullable = false)
    @JsonIgnoreProperties({"comments", "reactions", "lostPetSource", "hibernateLazyInitializer", "handler"})
    private CommunityPost matchPost;

    private Integer matchPercentage; // Porcentaje de similitud devuelto por Gemini

    // 🛑 SOLUCIÓN: Usar LONGTEXT para asegurar espacio suficiente (hasta 4GB)
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String aiReasoning;

    private LocalDateTime searchDate = LocalDateTime.now();
}