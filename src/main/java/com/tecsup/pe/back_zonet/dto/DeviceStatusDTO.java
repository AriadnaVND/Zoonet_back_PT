package com.tecsup.pe.back_zonet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceStatusDTO {
    private Long petId;
    private String petName;
    private String status; // Valores: CONNECTED, DISCONNECTED, SEARCHING
    private String message;
}