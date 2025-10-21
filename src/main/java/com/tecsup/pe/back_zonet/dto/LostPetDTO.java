package com.tecsup.pe.back_zonet.dto;

import lombok.Data;

@Data
public class LostPetDTO {
    private Long petId; // ID de la mascota que se perdió
    private String description; // Descripción del dueño (Ej: "Visto por última vez con un collar rojo.")
    private Integer hoursLost; // "Hace X Horas"
    private String lastSeenLocation; // Ubicación textual (Ej: "Brooklyn Bridge Park")
    private double lastSeenLatitude;
    private double lastSeenLongitude;
}