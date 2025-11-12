package com.tecsup.pe.back_zonet.repository;

import com.tecsup.pe.back_zonet.entity.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Import necesario
import java.util.List;

public interface CommunityRepository extends JpaRepository<CommunityPost, Long> {

    // Método original (No alterado para mantener la compatibilidad con otros servicios)
    List<CommunityPost> findAllByOrderByCreatedAtDesc();

    // ✅ NUEVO MÉTODO PARA AI MATCHING: Carga las relaciones LostPetSource y Pet
    // Esto resuelve el error de Lazy Loading (LazyInitializationException) al acceder
    // a los detalles de la mascota fuera de la sesión de Hibernate.
    @Query("SELECT cp FROM CommunityPost cp " +
            "LEFT JOIN FETCH cp.lostPetSource lps " +
            "LEFT JOIN FETCH lps.pet " +
            "ORDER BY cp.createdAt DESC")
    List<CommunityPost> findAllWithDetailsOrderByCreatedAtDesc();
}