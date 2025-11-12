package com.tecsup.pe.back_zonet.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;


@Entity
@Table(name = "locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación a la mascota cuya ubicación estamos registrando
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Pet pet;

    private double latitude;
    private double longitude;
    private LocalDateTime timestamp = LocalDateTime.now();

    // Campo para registrar si el rastreo es en tiempo real (solo Premium)
    private boolean isRealTime = false;
}
