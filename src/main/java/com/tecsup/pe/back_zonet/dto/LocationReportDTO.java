package com.tecsup.pe.back_zonet.dto;

import lombok.Data;

@Data
public class LocationReportDTO {
    private Long petId;
    private double latitude;
    private double longitude;

    // 💡 NUEVO CAMPO
    private Double batteryLevel;
}