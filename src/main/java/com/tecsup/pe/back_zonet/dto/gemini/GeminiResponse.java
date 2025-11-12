package com.tecsup.pe.back_zonet.dto.gemini;

import lombok.Data;
import java.util.List;

/**
 * DTO para recibir y parsear la respuesta de Gemini API
 * Estructura según la documentación oficial de Google Generative AI
 */
@Data
public class GeminiResponse {

    private List<Candidate> candidates;
    private UsageMetadata usageMetadata;

    /**
     * Candidato de respuesta (Gemini puede devolver múltiples)
     */
    @Data
    public static class Candidate {
        private Content content;
        private String finishReason;  // Por qué terminó la generación
        private Integer index;
    }

    /**
     * Contenido de la respuesta
     */
    @Data
    public static class Content {
        private List<Part> parts;
        private String role;  // "model" o "user"
    }

    /**
     * Parte de contenido (texto o imagen)
     */
    @Data
    public static class Part {
        private String text;  // El texto generado por Gemini
    }

    /**
     * Metadata sobre el uso de tokens
     */
    @Data
    public static class UsageMetadata {
        private Integer promptTokenCount;       // Tokens del prompt
        private Integer candidatesTokenCount;   // Tokens de la respuesta
        private Integer totalTokenCount;        // Total de tokens usados
    }
}