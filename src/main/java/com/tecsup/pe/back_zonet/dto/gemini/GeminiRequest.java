package com.tecsup.pe.back_zonet.dto.gemini;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO para construir el request que se envía a Gemini API
 * Estructura según la documentación oficial de Google Generative AI
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeminiRequest {

    private List<Content> contents;
    private GenerationConfig generationConfig;

    /**
     * Representa el contenido que se envía (texto + imagen)
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Content {
        private List<Part> parts;
    }

    /**
     * Representa una parte del contenido (puede ser texto o imagen)
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Part {
        private String text;              // Para texto
        private InlineData inlineData;    // Para imágenes en base64
    }

    /**
     * Datos de imagen en formato inline (base64)
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InlineData {
        private String mimeType;  // Ej: "image/jpeg", "image/png"
        private String data;      // Imagen codificada en Base64
    }

    /**
     * Configuración de generación de respuesta
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GenerationConfig {
        private Double temperature;         // Creatividad (0.0 - 1.0)
        private String responseMimeType;    // Formato de respuesta
        private Integer maxOutputTokens;    // Máximo de tokens en respuesta
    }
}