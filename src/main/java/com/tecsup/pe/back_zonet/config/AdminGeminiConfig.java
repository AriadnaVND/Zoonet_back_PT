package com.tecsup.pe.back_zonet.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminGeminiConfig {

    // Estas variables leerán directamente las keys que definiste en application.properties
    @Value("${gemini.admin.api.key}")
    private String apiKey;

    @Value("${gemini.admin.api.url}")
    private String apiUrl;

    // Getters para que tu AdminModerationService pueda usar estas configuraciones
    public String getApiKey() {
        return apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }
}