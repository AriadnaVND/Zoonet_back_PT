package com.tecsup.pe.back_zonet.dto;

import lombok.Data;

@Data
public class SupportTicketRequest {
    private Long userId;
    private String subject; // Opcional, si el frontend lo implementa
    private String description; // El campo principal del formulario
}