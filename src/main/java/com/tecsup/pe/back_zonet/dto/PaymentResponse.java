package com.tecsup.pe.back_zonet.dto;

import lombok.Data;

@Data
public class PaymentResponse {
    // URL a la que el frontend debe redirigir al usuario para completar el pago.
    private String redirectUrl;
    private String planType;
    private Long userId;

    public PaymentResponse(String redirectUrl, String planType, Long userId) {
        this.redirectUrl = redirectUrl;
        this.planType = planType;
        this.userId = userId;
    }
}