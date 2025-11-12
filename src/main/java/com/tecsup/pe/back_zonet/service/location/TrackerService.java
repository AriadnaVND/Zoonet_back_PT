package com.tecsup.pe.back_zonet.service.location;

import com.tecsup.pe.back_zonet.dto.LocationReportDTO;
import com.tecsup.pe.back_zonet.entity.Location;
import com.tecsup.pe.back_zonet.entity.Pet;
import com.tecsup.pe.back_zonet.repository.LocationRepository;
import com.tecsup.pe.back_zonet.repository.PetRepository;
import com.tecsup.pe.back_zonet.util.RoleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TrackerService {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private RoleValidator roleValidator;

    /**
     * 🟢 Recibe y guarda el reporte de ubicación (simulando el dispositivo Find Hub)
     */
    public Location updateLocation(LocationReportDTO report) {
        Pet pet = petRepository.findById(report.getPetId())
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada."));

        // Comprueba si el usuario es Premium para el rastreo en tiempo real
        boolean isPremium = roleValidator.isPremiumUser(pet.getUser().getId());

        Location location = new Location();
        location.setPet(pet);
        location.setLatitude(report.getLatitude());
        location.setLongitude(report.getLongitude());
        location.setTimestamp(LocalDateTime.now());
        // Solo los usuarios Premium tienen la bandera de "tiempo real"
        location.setRealTime(isPremium);

        return locationRepository.save(location);
    }
    /**
     * 🟢 Obtiene la última ubicación de la mascota (para el Dashboard)
     */
    public Location getLastLocationByPetId(Long petId) {
        return locationRepository.findFirstByPetIdOrderByTimestampDesc(petId)
                .orElseThrow(() -> new RuntimeException("No se encontró ubicación para la mascota."));
    }
}
