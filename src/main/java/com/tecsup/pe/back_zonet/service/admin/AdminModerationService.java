package com.tecsup.pe.back_zonet.service.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecsup.pe.back_zonet.config.AdminGeminiConfig;
import com.tecsup.pe.back_zonet.entity.AdminModerationLog;
import com.tecsup.pe.back_zonet.entity.CommunityPost;
import com.tecsup.pe.back_zonet.repository.CommunityRepository;
import com.tecsup.pe.back_zonet.repository.ModerationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AdminModerationService {

    @Autowired private CommunityRepository communityRepo;
    @Autowired private ModerationRepository moderationRepository;
    @Autowired private AdminGeminiConfig adminGeminiConfig;
    @Autowired private RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void analizarPost(Long postId) {
        // ✅ Si ya existe un log para este post, no volver a analizar
        boolean yaAnalizado = moderationRepository.existsByPostId(postId);
        if (yaAnalizado) {
            System.out.println("⚠️ Post " + postId + " ya fue analizado. Saltando...");
            return;
        }

        CommunityPost post = communityRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post no encontrado con ID: " + postId));

        ejecutarAnalisisIA(postId, post.getDescription());
    }

    private void ejecutarAnalisisIA(Long postId, String descripcion) {
        String apiKey = adminGeminiConfig.getApiKey();

        System.out.println("DEBUG API KEY: " + (apiKey == null || apiKey.isBlank() ? "⚠️ VACÍA O NULA" : "✅ OK (longitud: " + apiKey.length() + ")"));

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;
        System.out.println("DEBUG URL: " + url);

        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text",
                                        "Analiza este texto y responde SOLO con un JSON válido, sin explicaciones ni bloques de código. " +
                                                "Formato exacto: {\"score\": 0.95, \"status\": \"APPROVED\", \"reason\": \"motivo aquí\"}. " +
                                                "El status debe ser APPROVED si el contenido es apropiado, o REJECTED si no lo es. " +
                                                "Texto a analizar: " + descripcion)
                        })
                }
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);

            if (response != null && response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                String respuestaTexto = (String) parts.get(0).get("text");

                System.out.println("DEBUG RESPUESTA IA: " + respuestaTexto);

                String jsonLimpio = respuestaTexto
                        .replaceAll("(?s)```json", "")
                        .replaceAll("(?s)```", "")
                        .trim();

                JsonNode root = objectMapper.readTree(jsonLimpio);

                AdminModerationLog log = new AdminModerationLog();
                log.setPostId(postId);
                log.setAiScore(root.path("score").asDouble(0.5));
                log.setStatus(root.path("status").asText("PENDING"));
                log.setAiReason(root.path("reason").asText("Sin razón definida"));

                moderationRepository.save(log);
                System.out.println("✅ Moderación exitosa guardada para post: " + postId);
            } else {
                System.err.println("⚠️ Respuesta de IA sin candidatos: " + response);
                throw new RuntimeException("La IA no devolvió candidatos válidos");
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR AL LLAMAR A LA IA: " + e.getMessage());
            throw new RuntimeException("Fallo en IA: " + e.getMessage());
        }
    }

    public List<CommunityPost> listAllPosts() {
        return communityRepo.findAll();
    }

    public void deletePost(Long postId) {
        communityRepo.deleteById(postId);
    }
}