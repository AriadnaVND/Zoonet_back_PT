package com.tecsup.pe.back_zonet.service.community;

import com.tecsup.pe.back_zonet.dto.CommentDTO;
import com.tecsup.pe.back_zonet.dto.ReactionDTO;
import com.tecsup.pe.back_zonet.entity.Comment;
import com.tecsup.pe.back_zonet.entity.CommunityPost;
import com.tecsup.pe.back_zonet.entity.Reaction;
import com.tecsup.pe.back_zonet.entity.User;
import com.tecsup.pe.back_zonet.repository.CommentRepository;
import com.tecsup.pe.back_zonet.repository.CommunityRepository;
import com.tecsup.pe.back_zonet.repository.ReactionRepository; // Importado
import com.tecsup.pe.back_zonet.repository.UserRepository;
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
    private ReactionRepository reactionRepository; // Inyectado

    @Autowired
    private UserRepository userRepository;

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

    /**
     * 💡 Lógica de negocio para añadir o remover una reacción (Toggle).
     * Si la reacción existe, la borra (unlike). Si no existe, la crea (like).
     * @return true si se añade la reacción, false si se elimina.
     */
    @Transactional
    public boolean toggleReaction(ReactionDTO dto) {
        CommunityPost post = communityRepository.findById(dto.getPostId())
                .orElseThrow(() -> new RuntimeException("Publicación no encontrada"));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 1. Verificar si la reacción ya existe (toggle)
        Optional<Reaction> existingReaction = reactionRepository.findByPostAndUser(post, user);

        if (existingReaction.isPresent()) {
            // 2. Si existe, la eliminamos (Unlike)
            reactionRepository.delete(existingReaction.get());
            return false; // Reacción eliminada
        } else {
            // 3. Si no existe, la creamos (Like)
            Reaction newReaction = new Reaction();
            newReaction.setPost(post);
            newReaction.setUser(user);
            reactionRepository.save(newReaction);
            return true; // Reacción añadida
        }
    }
}