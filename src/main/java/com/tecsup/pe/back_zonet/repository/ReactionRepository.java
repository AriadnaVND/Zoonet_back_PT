package com.tecsup.pe.back_zonet.repository;

import com.tecsup.pe.back_zonet.entity.CommunityPost;
import com.tecsup.pe.back_zonet.entity.Reaction;
import com.tecsup.pe.back_zonet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    // 💡 Búsqueda por par (Usuario, Post) para implementar el toggle
    Optional<Reaction> findByPostAndUser(CommunityPost post, User user);

    // Opcional: Contar las reacciones para un post específico
    long countByPostId(Long postId);
}