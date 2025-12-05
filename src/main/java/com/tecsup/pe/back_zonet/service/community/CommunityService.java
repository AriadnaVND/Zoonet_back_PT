package com.tecsup.pe.back_zonet.service.community;

import com.tecsup.pe.back_zonet.dto.CommentDTO;
import com.tecsup.pe.back_zonet.dto.ContactRequest; // 🟢 NUEVO
import com.tecsup.pe.back_zonet.dto.ReactionDTO;
import com.tecsup.pe.back_zonet.entity.Comment;
import com.tecsup.pe.back_zonet.entity.CommunityPost;
import com.tecsup.pe.back_zonet.entity.Reaction;
import com.tecsup.pe.back_zonet.entity.User;
import com.tecsup.pe.back_zonet.repository.CommentRepository;
import com.tecsup.pe.back_zonet.repository.CommunityRepository;
import com.tecsup.pe.back_zonet.repository.ReactionRepository;
import com.tecsup.pe.back_zonet.repository.UserRepository;
import com.tecsup.pe.back_zonet.service.notification.NotificationService; // 🟢 NUEVO
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
    private NotificationService notificationService; // 🟢 INYECCIÓN NUEVA

    public CommunityPost save(CommunityPost post) {
        return communityRepository.save(post);
    }

    public List<CommunityPost> getAllPosts() {
        return communityRepository.findAllByOrderByCreatedAtDesc();
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

    // ------------------------------------------------------------
    // 🟢 NUEVO MÉTODO: Alerta de contacto al autor del post
    // ------------------------------------------------------------
    public void sendContactAlert(ContactRequest request) {

        // 1. Buscar el post
        CommunityPost post = communityRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("La publicación ya no existe."));

        // 2. Dueño de la publicación
        User recipientAuthor = post.getUser();

        // 3. Título dinámico
        String title;
        if ("SIGHTING".equals(post.getPostType())) {
            title = "💬 ¡Alguien reclama tu avistamiento!";
        } else {
            title = "🔔 Información sobre tu mascota perdida";
        }

        // 4. Mensaje con detalles de contacto
        String messageBody = String.format(
                "%s te escribió: \"%s\".\n📞 %s\n📧 %s",
                request.getName(),
                request.getMessage(),
                request.getPhone(),
                request.getEmail() != null ? request.getEmail() : "Sin correo"
        );

        // 5. Enviar notificación Push
        notificationService.createSystemNotification(
                recipientAuthor.getId(),
                title,
                messageBody,
                "CONTACT_MESSAGE",
                "HIGH"
        );
    }
}
