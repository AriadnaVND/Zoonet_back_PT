package com.tecsup.pe.back_zonet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa un resultado de coincidencia de IA
 * Contiene los datos de una mascota que coincide con la búsqueda
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiMatchResultDTO {

    private Long postId;              // ID del post en la comunidad
    private String petName;           // Nombre de la mascota
    private String description;       // Descripción del post
    private String imageUrl;          // URL de la imagen de la mascota
    private String locationName;      // Ubicación donde fue reportada
    private String timeAgo;           // Tiempo desde que fue publicado
    private Integer matchPercentage;  // Porcentaje de similitud (0-100)
    private String aiReasoning;       // Justificación de la IA sobre el match
}