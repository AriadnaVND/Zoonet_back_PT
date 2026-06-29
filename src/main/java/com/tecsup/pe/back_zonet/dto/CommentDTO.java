package com.tecsup.pe.back_zonet.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDTO {
    private Long id;
    private Long postId; // ID de la publicación a comentar
    private Long userId; // ID del usuario que comenta
    private String userName;
    private String content; // Contenido del comentario
    private LocalDateTime createdAt;
    private String userPhotoUrl;
}