package com.tecsup.pe.back_zonet.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String petName;
    private String photoUrl;
    private String plan; // FREE o PREMIUM
}
