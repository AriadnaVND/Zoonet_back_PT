package com.tecsup.pe.back_zonet.controller.pet;

import com.tecsup.pe.back_zonet.dto.LostPetDTO;
import com.tecsup.pe.back_zonet.entity.LostPet;
import com.tecsup.pe.back_zonet.service.pet.LostPetService;
import com.tecsup.pe.back_zonet.exception.UserLimitExceededException;
import com.tecsup.pe.back_zonet.exception.PetNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets/lost")
public class LostPetController {

    @Autowired
    private LostPetService lostPetService;

    /**
     * 🟢 POST /api/pets/lost
     * Reportar mascota perdida (Alerta de Emergencia del Dueño).
     * El cuerpo solo necesita petId, descripción, horas perdido y ubicación (la foto se obtiene de Pet).
     */
    @PostMapping
    public ResponseEntity<?> reportLostPet(@RequestBody LostPetDTO request) {
        try {
            LostPet lostPet = lostPetService.reportAsLost(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(lostPet);
        } catch (UserLimitExceededException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (PetNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al reportar mascota: " + e.getMessage());
        }
    }

    /**
     * 🟢 GET /api/pets/lost
     * Ver mascotas reportadas activas (solo la entidad LostPet, no el feed de Comunidad).
     */
    @GetMapping
    public ResponseEntity<List<LostPet>> getAllLostPets() {
        List<LostPet> lostPets = lostPetService.getAllActiveLostPets();
        return ResponseEntity.ok(lostPets);
    }

    /**
     * PUT /api/pets/lost/{reportId}/found
     * Marcar un reporte como encontrado.
     */
    @PutMapping("/{reportId}/found")
    public ResponseEntity<?> markAsFound(@PathVariable Long reportId) {
        try {
            LostPet foundPet = lostPetService.markAsFound(reportId);
            return ResponseEntity.ok(foundPet);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}