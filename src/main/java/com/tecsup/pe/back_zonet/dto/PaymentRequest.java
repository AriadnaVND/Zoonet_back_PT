package com.tecsup.pe.back_zonet.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private String cardNumber; // Número de tarjeta (ej. 16 dígitos)
    private String expirationMonth; // Mes (MM)
    private String expirationYear; // Año (YY)
    private String cvv; // CVV (3 dígitos)
}