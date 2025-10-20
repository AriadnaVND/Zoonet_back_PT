package com.tecsup.pe.back_zonet.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
