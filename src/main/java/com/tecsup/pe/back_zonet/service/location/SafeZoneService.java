package com.tecsup.pe.back_zonet.service.location;

import com.tecsup.pe.back_zonet.entity.SafeZone;
import com.tecsup.pe.back_zonet.exception.UserLimitExceededException;
import com.tecsup.pe.back_zonet.repository.SafeZoneRepository;
import com.tecsup.pe.back_zonet.util.RoleValidator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SafeZoneService {

    private final SafeZoneRepository safeZoneRepository;
    private final RoleValidator roleValidator;

    public SafeZoneService(SafeZoneRepository safeZoneRepository, RoleValidator roleValidator) {
        this.safeZoneRepository = safeZoneRepository;
        this.roleValidator = roleValidator;
    }

    public SafeZone createSafeZone(SafeZone zone) {
        Long userId = zone.getUserId();

        // Valida límite de zonas si el usuario es Free
        if (roleValidator.isFreeUser(userId)) {
            List<SafeZone> existingZones = safeZoneRepository.findByUserId(userId);
            if (!existingZones.isEmpty()) {
                throw new UserLimitExceededException("Los usuarios Free solo pueden tener 1 zona segura");
            }
        }

        return safeZoneRepository.save(zone);
    }

    public List<SafeZone> getSafeZonesByUser(Long userId) {
        return safeZoneRepository.findByUserId(userId);
    }

    public SafeZone updateSafeZone(SafeZone zone) {
        return safeZoneRepository.save(zone);
    }

    public void deleteSafeZone(Long zoneId) {
        safeZoneRepository.deleteById(zoneId);
    }
}
