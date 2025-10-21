package com.tecsup.pe.back_zonet.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // ❗ AÑADIR

@Entity
@Table(name = "lost_pets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LostPet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // ❗ CORRECCIÓN CLAVE (Para Pet)
    private Pet pet;

    private LocalDateTime reportDate = LocalDateTime.now();
    private Integer hoursLost;
    private String description;
    private String lastSeenLocation;
    private double lastSeenLatitude;
    private double lastSeenLongitude;

    private boolean found = false;

    @OneToOne(mappedBy = "lostPetSource")
    @JsonIgnoreProperties({"lostPetSource", "hibernateLazyInitializer", "handler"}) // ❗ CORRECCIÓN (Para ciclo CommunityPost)
    private CommunityPost communityPost;
}