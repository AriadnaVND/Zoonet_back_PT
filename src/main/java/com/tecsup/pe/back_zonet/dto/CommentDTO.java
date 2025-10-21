package com.tecsup.pe.back_zonet.dto;

import lombok.Data;

@Data
public class CommentDTO {
    private Long postId; // ID de la publicación a comentar
    private Long userId; // ID del usuario que comenta
    private String content; // Contenido del comentario
}