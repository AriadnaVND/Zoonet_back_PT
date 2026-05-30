package com.tecsup.pe.back_zonet.dto.admin;

import java.time.LocalDateTime;

public class UserSummaryDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String plan;
    private boolean active;
    private LocalDateTime createdAt;

    // Datos de la mascota asociada y hardware IoT para la interfaz avanzada
    private String petName;
    private String petPhoto;
    private String deviceSerialNumber;

    // Constructor vacío obligatorio para serialización / frameworks
    public UserSummaryDTO() {}

    // Constructor completo para mapear los resultados de la consulta JPQL
    public UserSummaryDTO(Long id, String name, String email, String phone, String plan,
                          boolean active, LocalDateTime createdAt, String petName,
                          String petPhoto, String deviceSerialNumber) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.plan = plan;
        this.active = active;
        this.createdAt = createdAt;
        this.petName = petName;
        this.petPhoto = petPhoto;
        this.deviceSerialNumber = deviceSerialNumber;
    }

    // ========================================================
    // GETTERS Y SETTERS
    // ========================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }

    public String getPetPhoto() { return petPhoto; }
    public void setPetPhoto(String petPhoto) { this.petPhoto = petPhoto; }

    public String getDeviceSerialNumber() { return deviceSerialNumber; }
    public void setDeviceSerialNumber(String deviceSerialNumber) { this.deviceSerialNumber = deviceSerialNumber; }
}