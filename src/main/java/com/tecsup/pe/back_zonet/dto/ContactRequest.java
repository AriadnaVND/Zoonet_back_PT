package com.tecsup.pe.back_zonet.dto;

import lombok.Data;

@Data
public class ContactRequest {
    private Long postId;       // ID de la publicación (Perdido o Avistamiento)
    private String name;       // Nombre de la persona que escribe
    private String phone;      // Teléfono de contacto
    private String email;      // Correo electrónico
    private String message;    // Mensaje (Ej: "Es mi perro" o "Lo encontré")
}