package com.tecsup.pe.back_zonet.service.admin;

import com.tecsup.pe.back_zonet.config.AdminGeminiConfig;
import com.tecsup.pe.back_zonet.entity.AdminModerationLog;
import com.tecsup.pe.back_zonet.entity.CommunityPost; // Asegúrate de importar esto
import com.tecsup.pe.back_zonet.repository.CommunityRepository;
import com.tecsup.pe.back_zonet.repository.ModerationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Service
public class AdminModerationService {

    @Autowired
    private CommunityRepository communityRepo;

    @Autowired
    private ModerationRepository moderationRepository;

    @Autowired
    private AdminGeminiConfig adminGeminiConfig;

    @Autowired
    private RestTemplate restTemplate;

    // Métodos requeridos por AdminCommunityController
    public List<CommunityPost> listAllPosts() {
        return communityRepo.findAll();
    }

    public void deletePost(Long postId) {
        communityRepo.deleteById(postId);
    }

    public void registrarModeracion(Long postId, Double score, String status, String reason) {
        AdminModerationLog log = new AdminModerationLog();
        log.setPostId(postId);
        log.setAiScore(score);
        log.setStatus(status);
        log.setAiReason(reason);
        moderationRepository.save(log);
    }

    public void analizarPost(Long postId, String descripcion) {
        String url = adminGeminiConfig.getApiUrl() + "gemini-1.5-flash:generateContent?key=" + adminGeminiConfig.getApiKey();

        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", "Analiza este post y responde en JSON: {'score': 0.0, 'status': 'APPROVED/MANUAL_REVIEW', 'reason': '...'}. Post: " + descripcion)
                        })
                }
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            System.out.println("Respuesta recibida: " + response);
        } catch (Exception e) {
            System.err.println("Error al llamar a Gemini: " + e.getMessage());
        }
    }
}