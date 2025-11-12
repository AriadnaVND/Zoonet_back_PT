package com.tecsup.pe.back_zonet.controller.location;

import com.tecsup.pe.back_zonet.dto.LocationReportDTO;
import com.tecsup.pe.back_zonet.entity.Location;
import com.tecsup.pe.back_zonet.service.location.TrackerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/location/tracker")
public class TrackerController {

    @Autowired
    private TrackerService trackerService;

    /**
     * 🟢 POST /api/location/tracker/report
     * Endpoint para que el dispositivo (o la simulación) reporte la ubicación.
     */
    @PostMapping("/report")
    public ResponseEntity<Location> updatePetLocation(@RequestBody LocationReportDTO report) {
        try {
            Location location = trackerService.updateLocation(report);
            return ResponseEntity.status(HttpStatus.CREATED).body(location);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * 🟢 GET /api/location/tracker/current/{petId}
     * Obtiene la última ubicación registrada para una mascota (usado por el Frontend).
     */
    @GetMapping("/current/{petId}")
    public ResponseEntity<Location> getCurrentLocation(@PathVariable Long petId) {
        try {
            Location location = trackerService.getLastLocationByPetId(petId);
            return ResponseEntity.ok(location);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
