package com.tecsup.pe.back_zonet.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // ❗ AÑADIR

@Entity
@Table(name = "pets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // nombre de la mascota
    private String photoUrl; // URL o nombre del archivo de la foto

    @ManyToOne(fetch = FetchType.LAZY) // Asumiendo que es LAZY
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // ❗ CORRECCIÓN CLAVE
    private User user;
}