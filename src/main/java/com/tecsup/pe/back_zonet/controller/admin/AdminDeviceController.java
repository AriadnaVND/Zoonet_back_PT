package com.tecsup.pe.back_zonet.controller.admin;

import com.tecsup.pe.back_zonet.entity.User;
import com.tecsup.pe.back_zonet.entity.Pet;
import com.tecsup.pe.back_zonet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/devices")
@CrossOrigin(origins = "*")
public class AdminDeviceController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/status-report")
    public ResponseEntity<List<Map<String, Object>>> getDeviceNetworkStatus() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> report = new ArrayList<>();

        for (User user : users) {
            if (user.getPets() != null) {
                for (Pet pet : user.getPets()) {
                    // SE HA MODIFICADO: Ahora el "status" lee directamente el valor de la base de datos
                    // desde la entidad Pet, en lugar de estar fijado como "CONECTADO".
                    report.add(Map.of(
                            "petName", pet.getName() != null ? pet.getName() : "Sin nombre",
                            "ownerEmail", user.getEmail(),
                            "deviceId", "ZN-IOT-" + pet.getId(),
                            "status", pet.getDeviceStatus() != null ? pet.getDeviceStatus() : "DISCONNECTED"
                    ));
                }
            }
        }
        return ResponseEntity.ok(report);
    }

    @PostMapping("/{deviceId}/reboot")
    public ResponseEntity<?> forceRebootDevice(@PathVariable String deviceId) {
        return ResponseEntity.ok(Map.of(
                "message", "Comando de reinicio enviado exitosamente al dispositivo " + deviceId,
                "timestamp", LocalDateTime.now(),
                "status", "SUCCESS"
        ));
    }
}