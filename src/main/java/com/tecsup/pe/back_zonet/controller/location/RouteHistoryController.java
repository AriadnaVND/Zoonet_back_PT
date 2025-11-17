package com.tecsup.pe.back_zonet.controller.location;

import com.tecsup.pe.back_zonet.dto.RouteHistoryDTO;
import com.tecsup.pe.back_zonet.service.location.RouteHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/location/routes")
public class RouteHistoryController {

    @Autowired
    private RouteHistoryService routeHistoryService;

    /**
     * 🟢 GET /api/location/routes/{petId}
     * Obtiene el historial de rutas y métricas para una mascota en un período.
     * @param petId ID de la mascota.
     * @param period "semana", "mes", "año". Por defecto "semana".
     * @return RouteHistoryDTO con distancia, tiempo, calorías y número de rutas.
     */
    @GetMapping("/{petId}")
    public ResponseEntity<RouteHistoryDTO> getRouteHistory(
            @PathVariable Long petId,
            @RequestParam(required = false, defaultValue = "semana") String period) {

        RouteHistoryDTO history = routeHistoryService.getHistoryByPetIdAndPeriod(petId, period);
        return ResponseEntity.ok(history);
    }
}
