package com.tecsup.pe.back_zonet.controller.community;

import com.tecsup.pe.back_zonet.dto.CommentDTO;
import com.tecsup.pe.back_zonet.dto.ContactRequest; // 🟢 NUEVO
import com.tecsup.pe.back_zonet.dto.ReactionDTO;
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

    @GetMapping("/posts")
    public ResponseEntity<List<CommunityPost>> getAllPosts() {
        List<CommunityPost> posts = communityService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    @PostMapping(
            value = "/posts/{userId}",
            consumes = {"multipart/form-data"}
    )
    public ResponseEntity<?> createCommunityPost(
            @PathVariable Long userId,
            @RequestParam("description") String description,
            @RequestParam("locationName") String locationName,
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam("photo") MultipartFile photo
    ) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado con ID: " + userId);
            }

            if (photo == null || photo.isEmpty()) {
                return ResponseEntity.badRequest().body("Debe seleccionar una foto para el avistamiento.");
            }

            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            String fileName = System.currentTimeMillis() + "_" + photo.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.write(filePath, photo.getBytes());

            String photoUrl = "/" + UPLOAD_DIR + fileName;

            CommunityPost post = new CommunityPost();
            post.setUser(user);
            post.setPostType("SIGHTING");
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

    @PostMapping("/comments")
    public ResponseEntity<?> addComment(@RequestBody CommentDTO dto) {
        try {
            Comment comment = communityService.addComment(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(comment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

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

    /**
     * 🟢 NUEVO ENDPOINT: Contactar al Autor
     * POST /api/community/contact
     */
    @PostMapping("/contact")
    public ResponseEntity<?> contactAuthor(@RequestBody ContactRequest request) {
        try {
            communityService.sendContactAlert(request);
            return ResponseEntity.ok(Map.of("message", "Mensaje enviado al autor del post exitosamente."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
