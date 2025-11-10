package com.tecsup.pe.back_zonet.controller.location;

import com.tecsup.pe.back_zonet.dto.ZoneDTO;
import com.tecsup.pe.back_zonet.entity.SafeZone;
import com.tecsup.pe.back_zonet.entity.User; // ⚠️ NUEVO
import com.tecsup.pe.back_zonet.repository.UserRepository; // ⚠️ NUEVO
import com.tecsup.pe.back_zonet.service.location.SafeZoneService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/location/safezones")
public class SafeZoneController {

    private final SafeZoneService safeZoneService;
    private final UserRepository userRepository; // ⚠️ INYECTAR REPOSITORIO

    public SafeZoneController(SafeZoneService safeZoneService, UserRepository userRepository) { // ⚠️ CONSTRUCTOR MODIFICADO
        this.safeZoneService = safeZoneService;
        this.userRepository = userRepository;
    }

    // Método de ayuda para obtener el usuario
    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));
    }

    // Crear zona segura
    @PostMapping
    public SafeZone createSafeZone(@RequestBody ZoneDTO dto) {
        User user = findUser(dto.getUserId()); // ⚠️ BUSCAR USER A PARTIR DEL ID DEL DTO

        SafeZone zone = new SafeZone();
        zone.setUser(user); // ⚠️ USAR setUser(User)
        zone.setName(dto.getName());
        zone.setLatitude(dto.getLatitude());
        zone.setLongitude(dto.getLongitude());
        zone.setRadius(dto.getRadius());
        zone.setAddress(dto.getAddress());
        return safeZoneService.createSafeZone(zone);
    }

    // Obtener zonas de un usuario
    @GetMapping("/{userId}")
    public List<ZoneDTO> getZones(@PathVariable Long userId) {
        // El servicio usa directamente el userId, no necesita cambio de objeto User aquí.
        return safeZoneService.getSafeZonesByUser(userId).stream().map(z -> {
            ZoneDTO dto = new ZoneDTO();
            dto.setId(z.getId());
            dto.setUserId(z.getUser().getId()); // ⚠️ USAR getUser().getId()
            dto.setName(z.getName());
            dto.setLatitude(z.getLatitude());
            dto.setLongitude(z.getLongitude());
            dto.setRadius(z.getRadius());
            dto.setAddress(z.getAddress());
            return dto;
        }).collect(Collectors.toList());
    }

    // Actualizar zona
    @PutMapping("/{id}")
    public SafeZone updateZone(@PathVariable Long id, @RequestBody ZoneDTO dto) {
        User user = findUser(dto.getUserId()); // ⚠️ BUSCAR USER A PARTIR DEL ID DEL DTO

        SafeZone zone = new SafeZone();
        zone.setId(id);
        zone.setUser(user); // ⚠️ USAR setUser(User)
        zone.setName(dto.getName());
        zone.setLatitude(dto.getLatitude());
        zone.setLongitude(dto.getLongitude());
        zone.setRadius(dto.getRadius());
        zone.setAddress(dto.getAddress());
        return safeZoneService.updateSafeZone(zone);
    }

    // Eliminar zona
    @DeleteMapping("/{id}")
    public void deleteZone(@PathVariable Long id) {
        safeZoneService.deleteSafeZone(id);
    }
}