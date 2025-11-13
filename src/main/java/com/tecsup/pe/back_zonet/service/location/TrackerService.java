package com.tecsup.pe.back_zonet.service.location;

import com.tecsup.pe.back_zonet.dto.LocationReportDTO;
import com.tecsup.pe.back_zonet.entity.Location;
import com.tecsup.pe.back_zonet.entity.Pet;
import com.tecsup.pe.back_zonet.repository.LocationRepository;
import com.tecsup.pe.back_zonet.repository.PetRepository;
import com.tecsup.pe.back_zonet.service.notification.NotificationService;
import com.tecsup.pe.back_zonet.util.RoleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TrackerService {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private RoleValidator roleValidator;

    @Autowired
    private GeoFenceAlertService geoFenceAlertService;

    @Autowired
    private NotificationService notificationService;

    /**
     * 🟢 Recibe y guarda el reporte de ubicación (simulando el dispositivo Find Hub)
     */
    @Transactional
    public Location updateLocation(LocationReportDTO report) {
        Pet pet = petRepository.findById(report.getPetId())
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada."));

        boolean isPremium = roleValidator.isPremiumUser(pet.getUser().getId());

        Location location = new Location();
        location.setPet(pet);
        location.setLatitude(report.getLatitude());
        location.setLongitude(report.getLongitude());
        location.setTimestamp(LocalDateTime.now());
        location.setRealTime(isPremium);

        // 💡 Guardar el nuevo campo de batería
        location.setBatteryLevel(report.getBatteryLevel());

        Location savedLocation = locationRepository.save(location);

        // 1. Notificación de Ubicación Actualizada
        String title = "📍 Ubicación De " + pet.getName() + " Actualizada";
        String message = "Tu mascota está en Lat: " + String.format("%.4f", location.getLatitude()) + ", Lon: " + String.format("%.4f", location.getLongitude());

        notificationService.createSystemNotification(
                pet.getUser().getId(),
                title,
                message,
                "LOCATION",
                "MEDIUM"
        );

        // 2. Chequeo de Geo-cerca (Solo Premium)
        if (isPremium) {
            geoFenceAlertService.checkPetLocation(pet, savedLocation);
        }

        // 3. Chequeo de Alerta de Batería
        if (report.getBatteryLevel() != null && report.getBatteryLevel() <= 20) {
            notificationService.createSystemNotification(
                    pet.getUser().getId(),
                    "🔋 Batería Baja: " + pet.getName(),
                    "¡Carga el Rastreador! La batería está al " + report.getBatteryLevel().intValue() + "%",
                    "LOW_BATTERY",
                    "HIGH"
            );
        }

        return savedLocation;
    }

    /**
     * 🟢 Obtiene la última ubicación de la mascota (para el Dashboard)
     */
    public Location getLastLocationByPetId(Long petId) {
        return locationRepository.findFirstByPetIdOrderByTimestampDesc(petId)
                .orElseThrow(() -> new RuntimeException("No se encontró ubicación para la mascota."));
    }
}