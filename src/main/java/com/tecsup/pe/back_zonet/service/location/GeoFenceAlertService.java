package com.tecsup.pe.back_zonet.service.location;

import com.tecsup.pe.back_zonet.entity.Location;
import com.tecsup.pe.back_zonet.entity.Pet;
import com.tecsup.pe.back_zonet.entity.SafeZone;
import com.tecsup.pe.back_zonet.repository.SafeZoneRepository;
import com.tecsup.pe.back_zonet.service.notification.NotificationService;
import com.tecsup.pe.back_zonet.util.DistanceCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GeoFenceAlertService {

    @Autowired
    private SafeZoneRepository safeZoneRepository;

    @Autowired
    private NotificationService notificationService;

    // 💡 Método llamado desde TrackerService después de guardar la ubicación
    public void checkPetLocation(Pet pet, Location newLocation) {

        // 1. Obtener todas las zonas seguras del dueño
        List<SafeZone> safeZones = safeZoneRepository.findByUserId(pet.getUser().getId());

        if (safeZones.isEmpty()) {
            return;
        }

        // 2. Verificar si la mascota está a salvo en al menos una zona
        boolean isSafe = false;
        double minDistanceKm = Double.MAX_VALUE;

        for (SafeZone zone : safeZones) {
            double distanceKm = DistanceCalculator.calculateDistance(
                    newLocation.getLatitude(),
                    newLocation.getLongitude(),
                    zone.getLatitude(),
                    zone.getLongitude()
            );

            // Convertir radio de SafeZone (metros) a kilómetros
            double radiusKm = zone.getRadius() / 1000.0;

            minDistanceKm = Math.min(minDistanceKm, distanceKm); // Registrar la distancia mínima

            if (distanceKm <= radiusKm) {
                isSafe = true;
                break; // Está a salvo en al menos una zona, salir
            }
        }

        // 3. Disparar la alerta si no está en NINGUNA zona segura (Alerta de Zona de Riesgo)
        if (!isSafe) {
            String title = "🚨 Zona de Riesgo Detectada 🚨";
            String message = String.format(
                    "¡Alerta! %s ha salido de la geocerca. Última ubicación a %.2f km de su zona más cercana.",
                    pet.getName(),
                    minDistanceKm
            );

            notificationService.createSystemNotification(
                    pet.getUser().getId(),
                    title,
                    message,
                    "ZONE_RISK", // Nuevo tipo de notificación
                    "HIGH"
            );
        }
    }
}