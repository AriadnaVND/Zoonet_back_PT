package com.tecsup.pe.back_zonet.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // ❗ AÑADIR

@Entity
@Table(name = "community_posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunityPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // ❗ CORRECCIÓN
    private User user;

    private String postType;
    private String description;
    private String imageUrl;

    private String locationName;
    private Double latitude;
    private Double longitude;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne
    @JoinColumn(name = "lost_pet_source_id")
    @JsonIgnoreProperties({"communityPost", "hibernateLazyInitializer", "handler"}) // ❗ CORRECCIÓN (Corta ciclo con LostPet)
    private LostPet lostPetSource;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"post", "hibernateLazyInitializer", "handler"}) // ❗ CORRECCIÓN (Corta ciclo con Comment)
    private List<Comment> comments;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"post", "hibernateLazyInitializer", "handler"}) // ❗ CORRECCIÓN (Corta ciclo con Reaction)
    private List<Reaction> reactions;
}