package com.tecsup.pe.back_zonet.controller.location;

import com.tecsup.pe.back_zonet.dto.ZoneDTO;
import com.tecsup.pe.back_zonet.entity.SafeZone;
import com.tecsup.pe.back_zonet.service.location.SafeZoneService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/location/safezones")
public class SafeZoneController {

    private final SafeZoneService safeZoneService;

    public SafeZoneController(SafeZoneService safeZoneService) {
        this.safeZoneService = safeZoneService;
    }

    // Crear zona segura
    @PostMapping
    public SafeZone createSafeZone(@RequestBody ZoneDTO dto) {
        SafeZone zone = new SafeZone();
        zone.setUserId(dto.getUserId());
        zone.setName(dto.getName());
        zone.setLatitude(dto.getLatitude());
        zone.setLongitude(dto.getLongitude());
        zone.setRadius(dto.getRadius());
        zone.setAddress(dto.getAddress()); // NUEVO
        return safeZoneService.createSafeZone(zone);
    }

    // Obtener zonas de un usuario
    @GetMapping("/{userId}")
    public List<ZoneDTO> getZones(@PathVariable Long userId) {
        return safeZoneService.getSafeZonesByUser(userId).stream().map(z -> {
            ZoneDTO dto = new ZoneDTO();
            dto.setId(z.getId());
            dto.setUserId(z.getUserId());
            dto.setName(z.getName());
            dto.setLatitude(z.getLatitude());
            dto.setLongitude(z.getLongitude());
            dto.setRadius(z.getRadius());
            dto.setAddress(z.getAddress()); // NUEVO
            return dto;
        }).collect(Collectors.toList());
    }

    // Actualizar zona
    @PutMapping("/{id}")
    public SafeZone updateZone(@PathVariable Long id, @RequestBody ZoneDTO dto) {
        SafeZone zone = new SafeZone();
        zone.setId(id);
        zone.setUserId(dto.getUserId());
        zone.setName(dto.getName());
        zone.setLatitude(dto.getLatitude());
        zone.setLongitude(dto.getLongitude());
        zone.setRadius(dto.getRadius());
        zone.setAddress(dto.getAddress()); // NUEVO
        return safeZoneService.updateSafeZone(zone);
    }

    // Eliminar zona
    @DeleteMapping("/{id}")
    public void deleteZone(@PathVariable Long id) {
        safeZoneService.deleteSafeZone(id);
    }
}
