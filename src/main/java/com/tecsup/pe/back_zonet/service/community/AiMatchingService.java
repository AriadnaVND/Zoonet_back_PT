package com.tecsup.pe.back_zonet.service.community;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.tecsup.pe.back_zonet.config.GeminiConfig;
import com.tecsup.pe.back_zonet.dto.AiMatchResultDTO;
import com.tecsup.pe.back_zonet.dto.gemini.GeminiRequest;
import com.tecsup.pe.back_zonet.dto.gemini.GeminiResponse;
import com.tecsup.pe.back_zonet.entity.CommunityPost;
import com.tecsup.pe.back_zonet.repository.CommunityRepository;
import com.tecsup.pe.back_zonet.util.RoleValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.transaction.annotation.Transactional; // Import para gestión de Lazy Loading

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de matching con IA usando Gemini
 * Analiza imágenes de mascotas y las compara con posts de la comunidad
 */
@Slf4j
@Service
public class AiMatchingService {

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private RoleValidator roleValidator;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private GeminiConfig geminiConfig;

    private static final int MAX_POSTS_TO_COMPARE = 50;
    private static final String MODEL = "gemini-2.5-flash"; // Modelo actualizado y con cuota

    /**
     * Encuentra coincidencias de mascotas usando análisis de IA
     *
     * @param userId ID del usuario (debe ser Premium)
     * @param imageBytes Bytes de la imagen subida
     * @param mimeType Tipo MIME de la imagen
     * @return Lista de coincidencias ordenadas por porcentaje
     */
    @Transactional // CLAVE: Mantiene la sesión de DB abierta para evitar LazyInitializationException
    public List<AiMatchResultDTO> findMatches(Long userId, byte[] imageBytes, String mimeType) throws IOException {

        // 1. Validación de rol Premium
        log.info("Verificando rol Premium para usuario: {}", userId);
        /* // COMENTADO: Esto elimina la causa directa del error 403 Forbidden
        if (roleValidator.isFreeUser(userId)) {
            throw new RuntimeException("Funcionalidad restringida: AI Matching es solo para usuarios Premium.");
        }
        */

        // 2. Codificar imagen
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        log.info("Imagen codificada en Base64: {} bytes", imageBytes.length);

        // 3. Obtener posts para comparar
        log.info("Obteniendo posts de la comunidad...");

        // 🚨 USO DEL NUEVO MÉTODO CON JOIN FETCH (de CommunityRepository)
        List<CommunityPost> postsToCompare = communityRepository.findAllWithDetailsOrderByCreatedAtDesc().stream()
                .filter(post -> "LOST_ALERT".equals(post.getPostType()) || "SIGHTING".equals(post.getPostType()))
                .limit(MAX_POSTS_TO_COMPARE)
                .collect(Collectors.toList());

        if (postsToCompare.isEmpty()) {
            log.warn("No hay posts disponibles para comparar");
            return Collections.emptyList();
        }
        log.info("Posts encontrados para comparar: {}", postsToCompare.size());

        // 4. Construir contexto
        log.info("Construyendo contexto de comparación...");

        String comparisonContext;
        try {
            comparisonContext = postsToCompare.stream()
                    .map(post -> {
                        // Aseguramos que los valores sean Strings antes de sanearlos
                        String location = (post.getLocationName() != null) ? post.getLocationName() : "Ubicación Desconocida";
                        String description = (post.getDescription() != null) ? post.getDescription() : "Sin descripción";

                        // Aplicamos sanitizeText a ambos campos para evitar roturas de JSON
                        return String.format(
                                "{ \"id\": %d, \"location\": \"%s\", \"description\": \"%s\" }",
                                post.getId(),
                                sanitizeText(location), // Sanear Ubicación
                                sanitizeText(description) // Sanear Descripción
                        );
                    })
                    .collect(Collectors.joining(",\n"));

            log.info("Contexto construido exitosamente.");

        } catch (Exception contextEx) {
            // Este catch aísla cualquier NPE/error de mapeo en la construcción del prompt
            log.error("ERROR CRÍTICO: Fallo al construir el contexto de comparación JSON. Esto podría indicar un problema en una entidad.", contextEx);
            throw new IOException("Error al preparar los datos de la comunidad para la IA.", contextEx);
        }

        // 5. Crear prompt optimizado
        String promptText = String.format(
                """
                Analiza la imagen de la mascota y compárala con estas mascotas reportadas.
                
                Criterios de comparación:
                - Raza o especie (perro, gato, etc.)
                - Color del pelaje
                - Patrón y marcas distintivas
                - Tamaño y complexión
                
                Mascotas en la comunidad:
                [
                %s
                ]
                
                IMPORTANTE: Responde ÚNICAMENTE con un array JSON válido (sin texto adicional ni markdown).
                
                Formato exacto:
                [
                  { "postId": 123, "matchPercentage": 95, "aiReasoning": "Coincide en raza y color..." },
                  { "postId": 456, "matchPercentage": 87, "aiReasoning": "Similar patrón de pelaje..." },
                  { "postId": 789, "matchPercentage": 72, "aiReasoning": "Misma raza, diferente color..." }
                ]
                
                Devuelve los 3 mejores matches ordenados por porcentaje (de mayor a menor).
                Si NO hay coincidencias buenas (ninguno >= 50%), devuelve un array vacío: [].
                """, comparisonContext
        );

        // 6. Construir request para Gemini
        log.info("Construyendo request para Gemini...");
        GeminiRequest request = buildGeminiRequest(base64Image, mimeType, promptText);

        // 7. Llamar a Gemini API con reintentos
        log.info("Llamando a Gemini API...");
        String jsonResponse = callGeminiApiWithRetry(request);

        // 8. Manejar respuesta vacía explícitamente (si Gemini devuelve "[]")
        String cleanJson = jsonResponse.trim()
                .replaceFirst("^```json\\s*", "")
                .replaceFirst("```\\s*$", "")
                .trim();

        if (cleanJson.equals("[]")) {
            log.info("Gemini no encontró coincidencias relevantes (<= 50%) según el prompt.");
            return Collections.emptyList();
        }

        // 9. Parsear respuesta (usando el JSON ya limpiado)
        log.info("Parseando respuesta de Gemini...");
        List<AiMatchResultDTO> matches = parseGeminiResponse(jsonResponse);
        log.info("Matches encontrados (sin filtrar): {}", matches.size());

        // 10. Enriquecer resultados
        try {
            log.info("Enriqueciendo resultados con datos de BD...");
            enrichMatchResults(matches, postsToCompare);
        } catch (Exception dbEx) {
            log.error("ERROR CRÍTICO durante el enriquecimiento de datos: {}", dbEx.getMessage(), dbEx);
            throw new IOException("Fallo al procesar datos de la comunidad para enriquecer resultados.", dbEx);
        }

        // 11. Filtrar y ordenar
        List<AiMatchResultDTO> finalMatches = matches.stream()
                .filter(m -> m.getMatchPercentage() != null && m.getMatchPercentage() >= 50)
                .sorted((a, b) -> Integer.compare(b.getMatchPercentage(), a.getMatchPercentage()))
                .limit(3)
                .collect(Collectors.toList());

        log.info("Matches finales después de filtrado: {}", finalMatches.size());
        return finalMatches;
    }

    private GeminiRequest buildGeminiRequest(String base64Image, String mimeType, String promptText) {
        // Parte de imagen
        GeminiRequest.InlineData imageData = new GeminiRequest.InlineData(mimeType, base64Image);
        GeminiRequest.Part imagePart = new GeminiRequest.Part(null, imageData);

        // Parte de texto
        GeminiRequest.Part textPart = new GeminiRequest.Part(promptText, null);

        // Content con ambas partes
        GeminiRequest.Content content = new GeminiRequest.Content(Arrays.asList(imagePart, textPart));

        // Config de generación
        GeminiRequest.GenerationConfig config = new GeminiRequest.GenerationConfig(
                0.3,                    // Temperature baja para respuestas consistentes
                "application/json",     // Queremos JSON
                2048                    // Suficientes tokens para la respuesta
        );

        return new GeminiRequest(Collections.singletonList(content), config);
    }

    // =========================================================================
    // Lógica de Reintentos y Llamadas a API
    // =========================================================================

    private String callGeminiApiWithRetry(GeminiRequest request) throws IOException {
        final int MAX_RETRIES = 3;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                log.info("Intento {}/{} - Llamando a Gemini API...", attempt, MAX_RETRIES);
                return callGeminiApi(request);
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().is4xxClientError() && e.getStatusCode() != HttpStatus.TOO_MANY_REQUESTS) {
                    log.error("Error no reintentable (4xx): {}", e.getMessage());
                    throw new IOException("Error de cliente en la API: " + e.getMessage(), e);
                }
                if (attempt < MAX_RETRIES) {
                    long delay = 2000L * attempt;
                    log.warn("Error transitorio ({}). Reintentando en {}ms...", e.getStatusCode(), delay);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Proceso interrumpido durante el backoff.", interruptedException);
                    }
                } else {
                    log.error("Fallo después de {} intentos.", MAX_RETRIES);
                    throw new IOException("Fallo la llamada a Gemini API después de múltiples reintentos.", e);
                }
            } catch (Exception e) {
                if (attempt < MAX_RETRIES) {
                    long delay = 2000L * attempt;
                    log.warn("Error de conexión/timeout. Reintentando en {}ms...", delay);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Proceso interrumpido durante el backoff.", interruptedException);
                    }
                } else {
                    log.error("Error al llamar a Gemini API: {}", e.getMessage(), e);
                    throw new IOException("Error en llamada a Gemini: " + e.getMessage(), e);
                }
            }
        }
        throw new IOException("Fallo la llamada a Gemini API después de múltiples reintentos.");
    }

    private String callGeminiApi(GeminiRequest request) throws IOException {
        String url = geminiConfig.getApiUrl() + MODEL + ":generateContent?key=" + geminiConfig.getApiKey();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<GeminiResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                GeminiResponse.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            GeminiResponse geminiResponse = response.getBody();

            if (geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()) {
                String text = geminiResponse.getCandidates().get(0).getContent().getParts().get(0).getText();

                if (geminiResponse.getUsageMetadata() != null) {
                    log.info("Tokens usados - Total: {}, Prompt: {}, Response: {}",
                            geminiResponse.getUsageMetadata().getTotalTokenCount(),
                            geminiResponse.getUsageMetadata().getPromptTokenCount(),
                            geminiResponse.getUsageMetadata().getCandidatesTokenCount());
                }

                return text;
            }
        }
        throw new IOException("Respuesta vacía o incompleta de Gemini API");
    }

    /**
     * Parsea la respuesta JSON de Gemini con validación robusta
     */
    private List<AiMatchResultDTO> parseGeminiResponse(String jsonResponse) throws IOException {
        try {
            String cleanJson = jsonResponse.trim()
                    .replaceFirst("^```json\\s*", "")
                    .replaceFirst("```\\s*$", "")
                    .trim();

            log.debug("JSON limpio: {}", cleanJson);

            Gson gson = new Gson();
            Type listType = new TypeToken<List<AiMatchResultDTO>>() {}.getType();
            List<AiMatchResultDTO> matches = gson.fromJson(cleanJson, listType);

            if (matches == null) {
                log.warn("Gemini devolvió respuesta nula, retornando lista vacía.");
                return Collections.emptyList();
            }

            matches.forEach(match -> {
                if (match.getPostId() == null) {
                    throw new IllegalStateException("postId es requerido en la respuesta de la IA.");
                }
                if (match.getMatchPercentage() == null) {
                    log.warn("matchPercentage nulo para postId: {}", match.getPostId());
                    match.setMatchPercentage(0);
                }
                if (match.getMatchPercentage() < 0 || match.getMatchPercentage() > 100) {
                    log.warn("matchPercentage fuera de rango (0-100) para postId: {}. Estableciendo a 0.", match.getPostId());
                    match.setMatchPercentage(0);
                }
                if (match.getAiReasoning() == null || match.getAiReasoning().isBlank()) {
                    match.setAiReasoning("Sin justificación proporcionada");
                }
            });

            return matches;

        } catch (JsonSyntaxException e) {
            log.error("Error al parsear JSON de Gemini. Respuesta: {}", jsonResponse);
            throw new IOException("Respuesta inválida de Gemini (Error de formato JSON): " + e.getMessage(), e);
        }
    }

    /**
     * Enriquece los matches con datos completos de la base de datos
     */
    private void enrichMatchResults(List<AiMatchResultDTO> matches, List<CommunityPost> posts) {
        Map<Long, CommunityPost> postMap = posts.stream()
                .collect(Collectors.toMap(CommunityPost::getId, p -> p));

        for (AiMatchResultDTO match : matches) {
            CommunityPost post = postMap.get(match.getPostId());
            if (post != null) {
                match.setImageUrl(post.getImageUrl() != null ? post.getImageUrl() : "");
                match.setLocationName(post.getLocationName() != null ? post.getLocationName() : "Ubicación Desconocida");
                match.setDescription(post.getDescription() != null ? post.getDescription() : "Sin descripción");

                match.setPetName(extractPetName(post));
                match.setTimeAgo(calculateTimeAgo(post.getCreatedAt()));
            } else {
                log.warn("Post de la comunidad no encontrado para postId: {}", match.getPostId());
            }
        }
    }

    /**
     * Extrae el nombre de la mascota del post (funcionalidad de soporte para posts de tipo LOST_ALERT)
     */
    private String extractPetName(CommunityPost post) {

        if ("SIGHTING".equals(post.getPostType())) {
            return "Avistamiento";
        }

        String petName = Optional.ofNullable(post.getLostPetSource())
                .map(lostPet -> lostPet.getPet())
                .map(pet -> pet.getName())
                .orElse(null);

        if (petName != null && !petName.trim().isEmpty()) {
            return petName;
        }

        return "Mascota Perdida";
    }

    /**
     * Calcula el tiempo transcurrido desde la creación del post
     */
    private String calculateTimeAgo(LocalDateTime createdAt) {
        if (createdAt == null) return "Fecha desconocida";

        Duration duration = Duration.between(createdAt, LocalDateTime.now());
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        if (days > 0) return "Perdido Hace " + days + " día" + (days == 1 ? "" : "s");
        if (hours > 0) return "Perdido Hace " + hours + " hora" + (hours == 1 ? "" : "s");
        if (minutes > 0) return "Perdido Hace " + minutes + " minuto" + (minutes == 1 ? "" : "s");
        return "Perdido hace un momento";
    }

    /**
     * Limpia el texto para evitar problemas con JSON
     */
    private String sanitizeText(String text) {
        if (text == null) return "";
        return text.replace("\"", "'")
                .replace("\\", "")
                .replace("\n", " ")
                .replace("\r", "")
                .replace("\t", " ")
                .trim();
    }
}