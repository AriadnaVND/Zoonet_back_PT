package com.tecsup.pe.back_zonet.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // ❗ AÑADIR
import com.fasterxml.jackson.annotation.JsonIgnore; // ⚠️ AÑADIDO
import java.util.List; // ⚠️ AÑADIDO

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

    // 🟢 CORRECCIÓN CLAVE: Si se elimina la Pet, se eliminan los LostPet asociados.
    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Oculta la lista en el JSON de Pet para evitar ciclos de serialización
    private List<LostPet> lostPetReports;
}