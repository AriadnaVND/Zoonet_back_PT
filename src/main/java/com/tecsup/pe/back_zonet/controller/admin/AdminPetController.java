package com.tecsup.pe.back_zonet.controller.admin;

import com.tecsup.pe.back_zonet.entity.Pet;
import com.tecsup.pe.back_zonet.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/pets")
@CrossOrigin(origins = "*")
public class AdminPetController {

    @Autowired
    private PetRepository petRepo;

    @GetMapping("/all")
    public ResponseEntity<List<Pet>> getAllPets() {
        return ResponseEntity.ok(petRepo.findAll());
    }

    @GetMapping("/alerts")
    public ResponseEntity<?> getDeviceAlerts() {
        return ResponseEntity.ok(java.util.Map.of("message", "Alertas de dispositivos críticos enviadas"));
    }
}