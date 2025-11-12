package com.tecsup.pe.back_zonet.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
public class SafeZone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ⚠️ CAMBIO CLAVE: Se reemplaza 'Long userId' por la relación ManyToOne con User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // user_id es la columna de la clave foránea
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user; // Referencia al objeto User

    private String name;
    private double latitude;
    private double longitude;
    private double radius;
    private String address;

    // Getters y setters corregidos para manejar el objeto User
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getRadius() { return radius; }
    public void setRadius(double radius) { this.radius = radius; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}