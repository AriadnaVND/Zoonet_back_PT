package com.tecsup.pe.back_zonet.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;

    @JsonIgnore
    private String password;

    private String plan; // FREE o PREMIUM
    private boolean active = true;

    // 🟢 NUEVO CAMPO - Token para notificaciones PUSH (Firebase Cloud Messaging)
    @Column(name = "fcm_token")
    private String fcmToken;

    // ----------------------------------------------------
    // ✅ RELACIONES PARA ELIMINACIÓN EN CASCADA COMPLETA
    // ----------------------------------------------------

    // 1. Mascotas (Pet)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Pet> pets;

    // 2. Publicaciones de Comunidad (CommunityPost)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CommunityPost> posts;

    // 3. Comentarios (Comment)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Comment> comments;

    // 4. Reacciones (Reaction)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Reaction> reactions;

    // 5. Notificaciones (Notification)
    @OneToMany(mappedBy = "recipientUser", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Notification> notifications;

    // 6. Zonas Seguras (SafeZone)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SafeZone> safeZones;

    // 7. Suscripción (Subscription)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Subscription subscription;
}
