package com.tecsup.pe.back_zonet.repository;

import com.tecsup.pe.back_zonet.entity.AdminModerationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ModerationRepository extends JpaRepository<AdminModerationLog, Long> {
    List<AdminModerationLog> findByStatus(String status);
    long countByStatus(String status);
    boolean existsByPostId(Long postId);
    void deleteByPostId(Long postId); // ← NUEVO
}