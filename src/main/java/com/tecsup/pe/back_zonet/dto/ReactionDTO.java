package com.tecsup.pe.back_zonet.dto;

import lombok.Data;

@Data
public class ReactionDTO {
    private Long postId;
    private Long userId;
    // Opcional: Se podría usar un campo 'reactionType' si hubiera más que solo 'Like'
}