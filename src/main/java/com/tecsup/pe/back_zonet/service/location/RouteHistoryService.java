package com.tecsup.pe.back_zonet.service.location;

import com.tecsup.pe.back_zonet.dto.RouteHistoryDTO;
import com.tecsup.pe.back_zonet.entity.Location;
import com.tecsup.pe.back_zonet.repository.RouteRepository;
import com.tecsup.pe.back_zonet.util.DistanceCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class RouteHistoryService {

    @Autowired
    private RouteRepository routeRepository;

    private static final double CALORIES_PER_KM = 0.05; // Estimación simple

    public RouteHistoryDTO getHistoryByPetIdAndPeriod(Long petId, String period) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate;

        // 1. Determinar el rango de fechas
        switch (period.toLowerCase()) {
            case "mes":
                startDate = endDate.minusMonths(1);
                break;
            case "año":
                startDate = endDate.minusYears(1);
                break;
            case "semana":
            default:
                startDate = endDate.minusWeeks(1);
                break;
        }

        // 2. Obtener los puntos de localización ordenados cronológicamente
        List<Location> points = routeRepository.findByPetIdAndTimestampBetweenOrderByTimestampAsc(
                petId, startDate, endDate
        );

        if (points.size() < 2) {
            return new RouteHistoryDTO(0.0, 0L, 0L, 0);
        }

        // 3. Calcular métricas
        double totalDistanceKm = 0.0;
        long totalTimeMinutes = 0;
        int totalRoutes = 0;

        Location prevPoint = points.get(0);
        // Inicializa prevTimestamp con el primer punto para el cálculo de tiempo
        LocalDateTime prevTimestamp = prevPoint.getTimestamp();
        double minDistanceForNewRouteKm = 0.5;

        // Una "ruta" es un conjunto de puntos consecutivos.
        // Simplificamos: si hay movimiento > 500m, cuenta como inicio de "ruta"
        totalRoutes = 1; // Asume que la primera ruta ya empezó

        for (int i = 1; i < points.size(); i++) {
            Location currentPoint = points.get(i);

            // a) Distancia
            double segmentDistance = DistanceCalculator.calculateDistance(
                    prevPoint.getLatitude(), prevPoint.getLongitude(),
                    currentPoint.getLatitude(), currentPoint.getLongitude()
            );

            totalDistanceKm += segmentDistance;

            // b) Tiempo (solo si hay puntos consecutivos)
            long minutesBetween = ChronoUnit.MINUTES.between(prevTimestamp, currentPoint.getTimestamp());
            totalTimeMinutes += minutesBetween;

            // c) Conteo de rutas
            if (segmentDistance > minDistanceForNewRouteKm && minutesBetween > 5) {
                // Si el dispositivo se movió significativamente después de una pausa, cuenta como nueva "ruta"
                totalRoutes++;
            }

            prevPoint = currentPoint;
            prevTimestamp = currentPoint.getTimestamp();
        }

        // 4. Estimar calorías (simple)
        long totalCalories = (long) (totalDistanceKm * CALORIES_PER_KM);

        // 5. Devolver el DTO con los resultados
        return new RouteHistoryDTO(
                Math.round(totalDistanceKm * 100.0) / 100.0,
                totalTimeMinutes,
                totalCalories,
                totalRoutes
        );
    }
}
