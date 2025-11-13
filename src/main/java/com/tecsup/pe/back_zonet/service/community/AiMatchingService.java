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
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private static final String MODEL = "gemini-2.5-flash"; // ✅ Modelo confirmado funcionando con curl

    @Transactional
    public List<AiMatchResultDTO> findMatches(Long userId, byte[] imageBytes, String mimeType) throws IOException {

        // 1. Validación de rol Premium
        log.info("Verificando rol Premium para usuario: {}", userId);
        // ✅ RESTRICCIÓN PREMIUM HABILITADA
        if (roleValidator.isFreeUser(userId)) {
            throw new RuntimeException("Funcionalidad restringida: AI Matching es solo para usuarios Premium.");
        }
        // FIN RESTRICCIÓN PREMIUM

        // 2. Codificar imagen
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        log.info("Imagen codificada en Base64: {} bytes", imageBytes.length);

        // 3. Obtener posts para comparar
        log.info("Obteniendo posts de la comunidad...");
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
                        String location = (post.getLocationName() != null) ? post.getLocationName() : "Ubicación Desconocida";
                        String description = (post.getDescription() != null) ? post.getDescription() : "Sin descripción";
                        return String.format(
                                "{ \"id\": %d, \"location\": \"%s\", \"description\": \"%s\" }",
                                post.getId(),
                                sanitizeText(location),
                                sanitizeText(description)
                        );
                    })
                    .collect(Collectors.joining(",\n"));
            log.info("Contexto construido exitosamente.");
        } catch (Exception contextEx) {
            log.error("ERROR CRÍTICO: Fallo al construir el contexto de comparación JSON.", contextEx);
            throw new IOException("Error al preparar los datos de la comunidad para la IA.", contextEx);
        }

        // 5. Comparar imagen con cada post individualmente
        log.info("Iniciando comparación individual con cada post...");
        List<AiMatchResultDTO> allMatches = new ArrayList<>();

        for (CommunityPost post : postsToCompare) {
            try {
                // Leer la imagen del post desde el sistema de archivos
                byte[] postImageBytes = readImageFromPost(post);
                if (postImageBytes == null) {
                    // log.warn("No se pudo leer la imagen del post {}", post.getId()); // Ya se registra el warning en el método
                    continue;
                }

                // Codificar imagen del post en Base64
                String postBase64Image = Base64.getEncoder().encodeToString(postImageBytes);

                // Crear prompt para comparación uno a uno
                String promptText = String.format(
                        """
                        Compara estas dos imágenes de mascotas y determina el porcentaje de similitud.
                        
                        Primera imagen: La mascota que se está buscando.
                        Segunda imagen: Post #%d - %s en %s
                        
                        Criterios de comparación:
                        - Raza o especie (perro, gato, etc.)
                        - Color del pelaje
                        - Patrón y marcas distintivas
                        - Tamaño y complexión
                        - Características faciales
                        
                        IMPORTANTE: Responde ÚNICAMENTE con un objeto JSON (sin texto adicional ni markdown).
                        
                        Formato exacto:
                        { "postId": %d, "matchPercentage": 85, "aiReasoning": "Explicación detallada..." }
                        
                        Si las imágenes NO son de la misma mascota o similitud < 10%%, responde:
                        { "postId": %d, "matchPercentage": 0, "aiReasoning": "Sin similitud significativa" }
                        """,
                        post.getId(),
                        post.getDescription() != null ? post.getDescription() : "Mascota reportada",
                        post.getLocationName() != null ? post.getLocationName() : "Ubicación desconocida",
                        post.getId(),
                        post.getId()
                );

                log.info("Comparando con post {}...", post.getId());

                // Construir request con AMBAS imágenes
                GeminiRequest request = buildGeminiRequestWithTwoImages(
                        base64Image, postBase64Image, mimeType, promptText
                );

                // Llamar a Gemini
                String jsonResponse = callGeminiApiWithRetry(request);

                // Parsear respuesta individual
                AiMatchResultDTO match = parseSingleMatchResponse(jsonResponse);

                if (match != null && match.getMatchPercentage() >= 10) {
                    allMatches.add(match);
                    log.info("Match encontrado con post {}: {}%", post.getId(), match.getMatchPercentage());
                }

            } catch (Exception e) {
                log.error("Error al comparar con post {}: {}", post.getId(), e.getMessage());
            }
        }

        log.info("Comparación completada. Total de matches: {}", allMatches.size());

        // 9. No hay parseGeminiResponse aquí porque ya procesamos individualmente
        List<AiMatchResultDTO> matches = allMatches;

        // 10. Enriquecer resultados
        try {
            log.info("Enriqueciendo resultados con datos de BD...");
            enrichMatchResults(matches, postsToCompare);
        } catch (Exception dbEx) {
            log.error("ERROR CRÍTICO durante el enriquecimiento de datos: {}", dbEx.getMessage(), dbEx);
            throw new IOException("Fallo al procesar datos de la comunidad para enriquecer resultados.", dbEx);
        }

        // 11. Filtrar y ordenar (umbral reducido a 10% para mejor UX)
        List<AiMatchResultDTO> finalMatches = matches.stream()
                .filter(m -> m.getMatchPercentage() != null && m.getMatchPercentage() >= 10)
                .sorted((a, b) -> Integer.compare(b.getMatchPercentage(), a.getMatchPercentage()))
                .limit(3)
                .collect(Collectors.toList());

        log.info("Matches finales después de filtrado: {}", finalMatches.size());
        return finalMatches;
    }

    // El método buildGeminiRequest sin la segunda imagen se elimina o se usa el nuevo.
    // El método buildGeminiRequestWithTwoImages se mantiene.

    private GeminiRequest buildGeminiRequestWithTwoImages(String base64Image1, String base64Image2, String mimeType, String promptText) {
        // Primera imagen (la que busca el usuario)
        GeminiRequest.InlineData imageData1 = new GeminiRequest.InlineData(mimeType, base64Image1);
        GeminiRequest.Part imagePart1 = new GeminiRequest.Part(null, imageData1);

        // Segunda imagen (del post en la comunidad)
        GeminiRequest.InlineData imageData2 = new GeminiRequest.InlineData(mimeType, base64Image2);
        GeminiRequest.Part imagePart2 = new GeminiRequest.Part(null, imageData2);

        // Parte de texto
        GeminiRequest.Part textPart = new GeminiRequest.Part(promptText, null);

        // Content con todas las partes
        GeminiRequest.Content content = new GeminiRequest.Content(
                Arrays.asList(textPart, imagePart1, imagePart2)
        );

        GeminiRequest.GenerationConfig config = new GeminiRequest.GenerationConfig(
                0.3,
                "application/json",
                2048
        );

        return new GeminiRequest(Collections.singletonList(content), config);
    }

    private byte[] readImageFromPost(CommunityPost post) {
        try {
            if (post.getImageUrl() == null || post.getImageUrl().isEmpty()) {
                return null;
            }

            // La imageUrl tiene formato: /uploads/filename.jpg
            String imagePath = post.getImageUrl();

            // 1. Remover el prefijo '/' si existe
            if (imagePath.startsWith("/")) {
                imagePath = imagePath.substring(1);
            }

            // 2. Intentar buscar la ruta relativa al directorio de trabajo (ROOT DIR)
            Path fullPath = Paths.get(imagePath);

            if (!Files.exists(fullPath)) {
                // 3. Si falla, intentar buscar la ruta absoluta usando el working directory
                fullPath = Paths.get(System.getProperty("user.dir"), imagePath);

                if (!Files.exists(fullPath)) {
                    log.warn("Archivo no encontrado en ninguna ruta: {}", imagePath);
                    return null;
                }
            }

            return Files.readAllBytes(fullPath);

        } catch (Exception e) {
            log.error("Error al leer imagen del post {}: {}", post.getId(), e.getMessage());
            return null;
        }
    }

    private AiMatchResultDTO parseSingleMatchResponse(String jsonResponse) throws IOException {
        try {
            String cleanJson = jsonResponse.trim()
                    .replaceFirst("^```json\\s*", "")
                    .replaceFirst("```\\s*$", "")
                    .trim();

            log.debug("JSON limpio individual: {}", cleanJson);

            Gson gson = new Gson();
            AiMatchResultDTO match = gson.fromJson(cleanJson, AiMatchResultDTO.class);

            if (match == null) {
                return null;
            }

            if (match.getPostId() == null) {
                throw new IllegalStateException("postId es requerido en la respuesta de la IA.");
            }
            if (match.getMatchPercentage() == null) {
                match.setMatchPercentage(0);
            }
            if (match.getMatchPercentage() < 0 || match.getMatchPercentage() > 100) {
                match.setMatchPercentage(0);
            }
            if (match.getAiReasoning() == null || match.getAiReasoning().isBlank()) {
                match.setAiReasoning("Sin justificación proporcionada");
            }

            return match;

        } catch (JsonSyntaxException e) {
            log.error("Error al parsear JSON individual de Gemini. Respuesta: {}", jsonResponse);
            throw new IOException("Respuesta inválida de Gemini: " + e.getMessage(), e);
        }
    }

    private String callGeminiApiWithRetry(GeminiRequest request) throws IOException {
        final int MAX_RETRIES = 3;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                log.info("Intento {}/{} - Llamando a Gemini API...", attempt, MAX_RETRIES);
                return callGeminiApi(request);
            } catch (HttpClientErrorException e) {
                // ✅ MEJORADO: Capturar el cuerpo de la respuesta de error
                log.error("Error HTTP al llamar a Gemini API. Status: {}, Body: {}",
                        e.getStatusCode(), e.getResponseBodyAsString());

                if (e.getStatusCode().is4xxClientError() && e.getStatusCode() != HttpStatus.TOO_MANY_REQUESTS) {
                    throw new IOException("Error de cliente en la API de Gemini: " + e.getResponseBodyAsString(), e);
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
                    throw new IOException("Fallo la llamada a Gemini API después de múltiples reintentos.", e);
                }
            } catch (Exception e) {
                log.error("Error inesperado al llamar a Gemini API: {}", e.getMessage(), e);
                if (attempt < MAX_RETRIES) {
                    long delay = 2000L * attempt;
                    log.warn("Reintentando en {}ms...", delay);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Proceso interrumpido durante el backoff.", interruptedException);
                    }
                } else {
                    throw new IOException("Error en llamada a Gemini: " + e.getMessage(), e);
                }
            }
        }
        throw new IOException("Fallo la llamada a Gemini API después de múltiples reintentos.");
    }

    private String callGeminiApi(GeminiRequest request) throws IOException {
        String url = geminiConfig.getApiUrl() + MODEL + ":generateContent?key=" + geminiConfig.getApiKey();

        // ✅ LOG AGREGADO
        log.info("URL de Gemini API: {}", url.replace(geminiConfig.getApiKey(), "***API_KEY***"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);

        try {
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
        } catch (HttpClientErrorException e) {
            // ✅ MEJORADO: Re-lanzar con más contexto
            log.error("Error HTTP: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }

    private List<AiMatchResultDTO> parseGeminiResponse(String jsonResponse) throws IOException {
        try {
            String cleanJson = jsonResponse.trim()
                    .replaceFirst("^```json\\s*", "")
                    .replaceFirst("```\\s*$", "")
                    .trim();

            log.info("JSON limpio para parsear. Longitud: {} caracteres", cleanJson.length());
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
                    log.warn("matchPercentage fuera de rango para postId: {}", match.getPostId());
                    match.setMatchPercentage(0);
                }
                if (match.getAiReasoning() == null || match.getAiReasoning().isBlank()) {
                    match.setAiReasoning("Sin justificación proporcionada");
                }
            });

            return matches;

        } catch (JsonSyntaxException e) {
            log.error("Error al parsear JSON de Gemini. Respuesta recibida: {}", jsonResponse);
            throw new IOException("Respuesta inválida de Gemini (Error de formato JSON): " + e.getMessage(), e);
        }
    }

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