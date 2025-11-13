package com.tecsup.pe.back_zonet.controller.community;

import com.tecsup.pe.back_zonet.dto.AiMatchResultDTO;
import com.tecsup.pe.back_zonet.service.community.AiMatchingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para AI Matching de mascotas
 * Endpoint: POST /api/community/ai-matching/{userId}
 */
@Slf4j
@RestController
@RequestMapping("/api/community/ai-matching")
@CrossOrigin(origins = "*")
public class AiMatchingController {

    @Autowired
    private AiMatchingService aiMatchingService;

    /**
     * Encuentra coincidencias de mascotas usando análisis de IA
     *
     * @param userId ID del usuario (debe ser Premium)
     * @param photo Imagen de la mascota a buscar
     * @return Lista de coincidencias o mensaje de error
     */
    @PostMapping(
            value = "/{userId}",
            consumes = {"multipart/form-data"}
    )
    public ResponseEntity<?> findMatches(
            @PathVariable Long userId,
            @RequestParam("photo") MultipartFile photo
    ) {
        log.info("Solicitud de AI Matching recibida para usuario: {}", userId);

        // Validar que se envió una imagen
        if (photo.isEmpty()) {
            log.warn("No se recibió ninguna imagen en la solicitud");
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Debe subir una foto de la mascota a buscar."));
        }

        // Validar tipo MIME
        String mimeType = photo.getContentType();
        if (mimeType == null || !mimeType.startsWith("image/")) {
            log.warn("Tipo de archivo inválido: {}", mimeType);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El archivo debe ser una imagen válida."));
        }

        try {
            // Convertir imagen a bytes
            byte[] imageBytes = photo.getBytes();
            log.info("Procesando imagen: {} bytes, tipo: {}", imageBytes.length, mimeType);

            // Llamar al servicio de matching
            List<AiMatchResultDTO> matches = aiMatchingService.findMatches(userId, imageBytes, mimeType);

            // ✅ MEJORA: Manejar respuesta vacía de forma informativa
            if (matches.isEmpty()) {
                log.info("No se encontraron coincidencias para el usuario {}", userId);
                Map<String, Object> response = new HashMap<>();
                response.put("matches", matches);
                // 🛑 CAMBIO: El mensaje ahora menciona el 40%
                response.put("message", "No se encontraron coincidencias con un porcentaje mayor al 40%. Intenta con una imagen diferente.");
                response.put("totalAnalyzed", "Se analizaron todas las mascotas reportadas en la comunidad");
                return ResponseEntity.ok(response);
            }

            // Respuesta exitosa con coincidencias
            log.info("Se encontraron {} coincidencias para el usuario {}", matches.size(), userId);
            return ResponseEntity.ok(matches);

        } catch (RuntimeException e) {
            // Captura la excepción de restricción de rol (FORBIDDEN)
            log.error("Error de restricción para usuario {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));

        } catch (IOException e) {
            // Error de lectura de archivo o en la llamada a la IA
            log.error("Error de I/O al procesar AI Matching para usuario {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al procesar la solicitud: " + e.getMessage()));
        }
    }
}