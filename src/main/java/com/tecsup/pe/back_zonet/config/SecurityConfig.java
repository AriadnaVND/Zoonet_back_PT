package com.tecsup.pe.back_zonet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig() {
        this.jwtAuthenticationFilter = new JwtAuthenticationFilter();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Enlazamos de forma mandatoria el origen de datos seguro de abajo
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                        // Permiso libre absoluto a las fotos de las mascotas
                        .requestMatchers("/uploads/**").permitAll()

                        // 1. Rutas de autenticación libres (Móvil y Web Admin)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/admin/auth/login").permitAll()

                        // 2. Endpoints libres para los collares IoT
                        .requestMatchers("/api/location/tracker/**").permitAll()
                        .requestMatchers("/api/tracker/**").permitAll()

                        // 3. Control estricto para el Panel de Administración (Requiere ROLE_ADMIN)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 4. Cualquier otra ruta queda permitida para evitar romper Flutter
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes explícitos autorizados (PROHIBIDO usar "*" con allowCredentials)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "https://admin.vickari.site",
                "https://vickari.site"
        ));

        // Métodos y verbos HTTP habilitados para todo el ecosistema de ZooNet
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 🟢 CORRECCIÓN: Agregamos "Authorization" de forma explícita y permitimos todas las cabeceras estándar entrantes
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Habilitamos el transporte seguro de cookies o cabeceras de autenticación
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}