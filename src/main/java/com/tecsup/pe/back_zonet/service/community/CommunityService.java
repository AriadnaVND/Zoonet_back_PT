package com.tecsup.pe.back_zonet.service.community;

import com.tecsup.pe.back_zonet.dto.CommentDTO;
import com.tecsup.pe.back_zonet.dto.ContactRequest;
import com.tecsup.pe.back_zonet.dto.ReactionDTO;
import com.tecsup.pe.back_zonet.entity.Comment;
import com.tecsup.pe.back_zonet.entity.CommunityPost;
import com.tecsup.pe.back_zonet.entity.Reaction;
import com.tecsup.pe.back_zonet.entity.User;
import com.tecsup.pe.back_zonet.repository.CommentRepository;
import com.tecsup.pe.back_zonet.repository.CommunityRepository;
import com.tecsup.pe.back_zonet.repository.ReactionRepository;
import com.tecsup.pe.back_zonet.repository.UserRepository;
import com.tecsup.pe.back_zonet.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CommunityService {

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    public CommunityPost save(CommunityPost post) {
        return communityRepository.save(post);
    }

    public List<CommunityPost> getAllPosts() {
        // ← CAMBIADO: filtra los posts rechazados por la IA
        return communityRepository.findAllApprovedOrderByCreatedAtDesc();
    }

    public Comment addComment(CommentDTO dto) {
        CommunityPost post = communityRepository.findById(dto.getPostId())
                .orElseThrow(() -> new RuntimeException("Publicación no encontrada"));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(dto.getContent());

        return commentRepository.save(comment);
    }

    // Agrega este método a tu clase CommunityService
    public List<CommentDTO> getCommentsByPostId(Long postId) {
        // Buscamos los comentarios en el repositorio
        List<Comment> comments = commentRepository.findByPostId(postId);

        // Convertimos las entidades a DTOs para el frontend
        return comments.stream().map(c -> {
            CommentDTO dto = new CommentDTO();
            dto.setId(c.getId());
            dto.setContent(c.getContent());
            dto.setUserName(c.getUser().getName());

            // 🟢 Lógica: Intentar obtener foto de la mascota asociada al post,
            // o una foto de perfil si tuvieras el campo en User
            CommunityPost post = c.getPost();
            if (post.getLostPetSource() != null) {
                dto.setUserPhotoUrl(post.getLostPetSource().getPet().getPhotoUrl());
            } else {
                dto.setUserPhotoUrl(null); // O un avatar genérico
            }

            dto.setCreatedAt(c.getCreatedAt());
            return dto;
        }).toList();
    }

    @Transactional
    public boolean toggleReaction(ReactionDTO dto) {
        CommunityPost post = communityRepository.findById(dto.getPostId())
                .orElseThrow(() -> new RuntimeException("Publicación no encontrada"));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Optional<Reaction> existingReaction = reactionRepository.findByPostAndUser(post, user);

        if (existingReaction.isPresent()) {
            reactionRepository.delete(existingReaction.get());
            return false;
        } else {
            Reaction newReaction = new Reaction();
            newReaction.setPost(post);
            newReaction.setUser(user);
            reactionRepository.save(newReaction);
            return true;
        }
    }

    public void sendContactAlert(ContactRequest request) {

        CommunityPost post = communityRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("La publicación ya no existe."));

        User recipientAuthor = post.getUser();

        String title;
        if ("SIGHTING".equals(post.getPostType())) {
            title = "💬 ¡Alguien reclama tu avistamiento!";
        } else {
            title = "🔔 Información sobre tu mascota perdida";
        }

        String messageBody = String.format(
                "%s te escribió: \"%s\".\n📞 %s\n📧 %s",
                request.getName(),
                request.getMessage(),
                request.getPhone(),
                request.getEmail() != null ? request.getEmail() : "Sin correo"
        );

        notificationService.createSystemNotification(
                recipientAuthor.getId(),
                title,
                messageBody,
                "CONTACT_MESSAGE",
                "HIGH"
        );
    }
}