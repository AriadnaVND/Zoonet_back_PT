package com.tecsup.pe.back_zonet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteHistoryDTO {
    private Double totalDistanceKm;
    private Long totalTimeMinutes;
    private Long totalCalories; // Basado en estimación de distancia
    private Integer totalRoutes; //Número de rutas
}
