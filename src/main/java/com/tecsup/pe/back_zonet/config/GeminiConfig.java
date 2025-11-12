package com.tecsup.pe.back_zonet.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración para la integración con Gemini API
 * Proporciona el cliente RestTemplate y las credenciales necesarias
 */
@Configuration
public class GeminiConfig {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/}")
    private String apiUrl;

    /**
     * Bean de RestTemplate para hacer llamadas HTTP a Gemini
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Obtiene la API Key de Gemini desde application.properties
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Obtiene la URL base de la API de Gemini
     */
    public String getApiUrl() {
        return apiUrl;
    }
}