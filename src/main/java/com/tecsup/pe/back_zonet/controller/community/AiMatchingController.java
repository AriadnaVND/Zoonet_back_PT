package com.tecsup.pe.back_zonet.controller.community;

import com.tecsup.pe.back_zonet.dto.AiMatchResultDTO;
import com.tecsup.pe.back_zonet.service.community.AiMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community/ai-matching")
public class AiMatchingController {

    @Autowired
    private AiMatchingService aiMatchingService;

    // Nota: El manejo de archivos temporales se mueve al servicio o a una utilidad.
    // El controlador solo se encarga de la recepción.

    @PostMapping(
            value = "/{userId}",
            consumes = {"multipart/form-data"}
    )
    public ResponseEntity<?> findMatches(
            @PathVariable Long userId,
            @RequestParam("photo") MultipartFile photo
    ) {
        if (photo.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debe subir una foto de la mascota a buscar."));
        }

        String mimeType = photo.getContentType();
        if (mimeType == null || !mimeType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("error", "El archivo debe ser una imagen válida."));
        }

        try {
            // El servicio toma los bytes directamente para evitar I/O redundante y manejar la limpieza.
            byte[] imageBytes = photo.getBytes();

            List<AiMatchResultDTO> matches = aiMatchingService.findMatches(userId, imageBytes, mimeType);

            return ResponseEntity.ok(matches);

        } catch (RuntimeException e) {
            // Captura la excepción de restricción de rol (FORBIDDEN)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            // Error de lectura de archivo o en la llamada a la IA
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al procesar la solicitud: " + e.getMessage()));
        }
    }
}