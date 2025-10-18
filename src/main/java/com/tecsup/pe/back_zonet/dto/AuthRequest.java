package com.tecsup.pe.back_zonet.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
}
