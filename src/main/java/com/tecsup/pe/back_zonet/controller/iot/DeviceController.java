package com.tecsup.pe.back_zonet.controller.iot;

import com.tecsup.pe.back_zonet.dto.DeviceStatusDTO;
import com.tecsup.pe.back_zonet.service.iot.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    /**
     * 🟢 POST /api/devices/{petId}/action
     * Simula acciones del hardware (Botones: Conectar, Desconectar).
     * Body JSON esperado: { "action": "connect" }
     */
    @PostMapping("/{petId}/action")
    public ResponseEntity<DeviceStatusDTO> handleDeviceAction(
            @PathVariable Long petId,
            @RequestBody Map<String, String> body
    ) {
        String action = body.get("action");
        if (action == null || action.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            DeviceStatusDTO status = deviceService.changeDeviceStatus(petId, action);

            // Pequeña simulación de espera para "search" (opcional)
            if ("search".equalsIgnoreCase(action)) {
                try { Thread.sleep(1000); } catch (InterruptedException e) {}
            }

            return ResponseEntity.ok(status);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new DeviceStatusDTO(petId, null, "ERROR", e.getMessage()));
        }
    }

    /**
     * 🟢 GET /api/devices/{petId}/status
     * Se llama al abrir la pantalla para ver si el botón debe estar verde (conectado) o rojo.
     */
    @GetMapping("/{petId}/status")
    public ResponseEntity<DeviceStatusDTO> getStatus(@PathVariable Long petId) {
        try {
            return ResponseEntity.ok(deviceService.getDeviceStatus(petId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}