package com.tecsup.pe.back_zonet.repository;

import com.tecsup.pe.back_zonet.entity.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityRepository extends JpaRepository<CommunityPost, Long> {

    List<CommunityPost> findAllByOrderByCreatedAtDesc();

    @Query("SELECT cp FROM CommunityPost cp " +
            "LEFT JOIN FETCH cp.lostPetSource lps " +
            "LEFT JOIN FETCH lps.pet " +
            "ORDER BY cp.createdAt DESC")
    List<CommunityPost> findAllWithDetailsOrderByCreatedAtDesc();

    // ← NUEVO: trae solo posts que NO están rechazados por la IA
    @Query("SELECT cp FROM CommunityPost cp " +
            "LEFT JOIN FETCH cp.lostPetSource lps " +
            "LEFT JOIN FETCH lps.pet " +
            "WHERE cp.id NOT IN (" +
            "SELECT m.postId FROM AdminModerationLog m WHERE m.status = 'REJECTED'" +
            ") ORDER BY cp.createdAt DESC")
    List<CommunityPost> findAllApprovedWithDetailsOrderByCreatedAtDesc();

    @Query("SELECT cp FROM CommunityPost cp WHERE cp.lostPetSource.id = :lostPetId")
    CommunityPost findByLostPetSourceId(@Param("lostPetId") Long lostPetId);
}