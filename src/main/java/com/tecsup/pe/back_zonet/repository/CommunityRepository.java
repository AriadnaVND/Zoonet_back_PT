package com.tecsup.pe.back_zonet.repository;

import com.tecsup.pe.back_zonet.entity.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommunityRepository extends JpaRepository<CommunityPost, Long> {
    // Para obtener el feed de la comunidad ordenado por el más reciente
    List<CommunityPost> findAllByOrderByCreatedAtDesc();
}