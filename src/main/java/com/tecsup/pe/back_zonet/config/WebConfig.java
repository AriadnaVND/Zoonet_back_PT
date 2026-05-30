package com.tecsup.pe.back_zonet.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Expone de forma limpia la carpeta física de fotos para el frontend
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }

    // 🟢 CORRECCIÓN: Se eliminó addCorsMappings de aquí.
    // Al centralizar todo el flujo CORS en SecurityConfig evitamos el conflicto repetido de allowCredentials.
}