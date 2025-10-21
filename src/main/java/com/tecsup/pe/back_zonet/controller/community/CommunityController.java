package com.tecsup.pe.back_zonet.controller.community;

import com.tecsup.pe.back_zonet.dto.CommentDTO;
import com.tecsup.pe.back_zonet.dto.ReactionDTO; // Importado
import com.tecsup.pe.back_zonet.entity.Comment;
import com.tecsup.pe.back_zonet.entity.CommunityPost;
import com.tecsup.pe.back_zonet.entity.User;
import com.tecsup.pe.back_zonet.repository.UserRepository;
import com.tecsup.pe.back_zonet.service.community.CommunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    @Autowired
    private UserRepository userRepository;

    private static final String UPLOAD_DIR = "uploads/";

    /**
     * 🟢 GET /api/community/posts
     * Ver publicaciones (Feed).
     */
    @GetMapping("/posts")
    public ResponseEntity<List<CommunityPost>> getAllPosts() {
        List<CommunityPost> posts = communityService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    // ... [POST /posts/{userId} para Avistamiento] ...
    @PostMapping(
            value = "/posts/{userId}",
            consumes = {"multipart/form-data"}
    )
    public ResponseEntity<?> createCommunityPost(
            @PathVariable Long userId,
            @RequestParam("description") String description, // El texto del post
            @RequestParam("locationName") String locationName,
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam("photo") MultipartFile photo
    ) {
        // Lógica de creación de Avistamiento (se mantiene igual)
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado con ID: " + userId);
            }

            if (photo == null || photo.isEmpty()) {
                return ResponseEntity.badRequest().body("Debe seleccionar una foto para el avistamiento.");
            }

            // 1. Guardar la foto localmente
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            String fileName = System.currentTimeMillis() + "_" + photo.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.write(filePath, photo.getBytes());

            String photoUrl = "/" + UPLOAD_DIR + fileName;

            // 2. Crear y guardar el CommunityPost (Tipo SIGHTING)
            CommunityPost post = new CommunityPost();
            post.setUser(user);
            post.setPostType("SIGHTING"); // Tipo Avistamiento de la Comunidad
            post.setDescription(description);
            post.setImageUrl(photoUrl);
            post.setLocationName(locationName);
            post.setLatitude(latitude);
            post.setLongitude(longitude);

            CommunityPost savedPost = communityService.save(post);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedPost);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar la foto del avistamiento: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al publicar el avistamiento: " + e.getMessage());
        }
    }

    /**
     * 🟢 POST /api/community/comments
     * Comentar una publicación.
     */
    @PostMapping("/comments")
    public ResponseEntity<?> addComment(@RequestBody CommentDTO dto) {
        try {
            Comment comment = communityService.addComment(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(comment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * 🟢 POST /api/community/reactions
     * Añadir o eliminar una reacción (Toggle Like/Unlike).
     */
    @PostMapping("/reactions")
    public ResponseEntity<Map<String, String>> toggleReaction(@RequestBody ReactionDTO dto) {
        try {
            boolean isAdded = communityService.toggleReaction(dto);

            String message = isAdded ? "Reacción añadida (Like)." : "Reacción eliminada (Unlike).";

            return ResponseEntity.ok(Map.of("message", message, "isAdded", String.valueOf(isAdded)));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}