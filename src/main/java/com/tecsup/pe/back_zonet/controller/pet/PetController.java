package com.tecsup.pe.back_zonet.controller.pet;

import com.tecsup.pe.back_zonet.entity.Pet;
import com.tecsup.pe.back_zonet.entity.User;
import com.tecsup.pe.back_zonet.exception.PetNotFoundException;
import com.tecsup.pe.back_zonet.service.pet.PetService;
import com.tecsup.pe.back_zonet.service.auth.PaymentService;
import com.tecsup.pe.back_zonet.repository.UserRepository;
import com.tecsup.pe.back_zonet.repository.PetRepository;
import com.tecsup.pe.back_zonet.service.location.TrackerService; // AGREGADO: Importar TrackerService
import com.tecsup.pe.back_zonet.dto.LocationReportDTO; // AGREGADO: Importar LocationReportDTO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    @Autowired
    private PetService petService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentService paymentService; // Mantenido

    @Autowired
    private PetRepository petRepository;

    // 🟢 NUEVO: Inyectar TrackerService
    @Autowired
    private TrackerService trackerService;

    private static final String UPLOAD_DIR = "uploads/";

    /**
     * 📸 Registrar mascota y plan del usuario
     * 💡 Lógica actualizada: Para el plan PREMIUM, ya no llama a un redirect, sino que indica
     * el endpoint al que debe llamar el frontend para procesar el pago (con datos de tarjeta).
     */
    @PostMapping(
            value = "/{userId}/register",
            consumes = {"multipart/form-data"}
    )
    public ResponseEntity<?> registerPet(
            @PathVariable Long userId,
            @RequestParam("petName") String petName,
            @RequestParam("planType") String planType, // "free" o "premium"
            @RequestParam("photo") MultipartFile photo
    ) {
        try {
            // Buscar al usuario por su ID
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado con ID: " + userId);
            }

            // Validar que haya foto
            if (photo == null || photo.isEmpty()) {
                return ResponseEntity.badRequest().body("Debe seleccionar una foto");
            }

            // Crear carpeta si no existe
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            // Guardar el archivo con nombre único
            String fileName = System.currentTimeMillis() + "_" + photo.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.write(filePath, photo.getBytes());

            // Crear y guardar la mascota
            Pet pet = new Pet();
            pet.setName(petName);
            pet.setPhotoUrl("/" + UPLOAD_DIR + fileName);
            pet.setUser(user);
            petService.save(pet); // <--- Mascota registrada

            // 🟢 SIMULACIÓN DE REPORTE INICIAL - Se ejecuta la lógica para guardar la ubicación inicial y generar notificaciones
            LocationReportDTO initialReport = new LocationReportDTO();
            initialReport.setPetId(pet.getId());
            initialReport.setLatitude(-12.04398);
            initialReport.setLongitude(-76.95291);
            // ✅ CORRECCIÓN: Batería al 100% para evitar la alerta LOW_BATTERY
            initialReport.setBatteryLevel(100.0);

            // Dispara el guardado de ubicación y las notificaciones
            // Solo se disparará la notificación de LOCATION (Ubicación Actualizada)
            // ya que la alerta LOW_BATTERY solo ocurre si el nivel es <= 20.
            trackerService.updateLocation(initialReport);

            // 💡 LÓGICA DE PAGO ACTUALIZADA: Devolver un mensaje de éxito/instrucción
            if (planType.equalsIgnoreCase("premium")) {
                // Devolver la instrucción para que el frontend pida los datos de tarjeta y llame al endpoint de proceso.
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(Map.of(
                                "message", "Mascota registrada. Procesa el pago para activar PREMIUM.",
                                "planType", "PENDING_PREMIUM",
                                "petId", pet.getId(),
                                "nextStepEndpoint", "/api/payment/process/" + userId
                        ));
            } else {
                // Si es FREE, actualiza el plan y devuelve el mensaje estructurado
                user.setPlan("FREE");
                userRepository.save(user);

                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(Map.of("message", "Mascota registrada con plan FREE", "planType", "FREE", "petId", pet.getId()));
            }

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar la foto: " + e.getMessage());
        }
    }

    /**
     * 🟢 NUEVO ENDPOINT: GET /api/pets/user/{userId}
     * Obtiene la primera mascota registrada del usuario (para la foto del Home).
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Pet> getPetByUserId(@PathVariable Long userId) {

        Pet pet = petRepository.findByUserId(userId).stream().findFirst()
                .orElseThrow(() -> new PetNotFoundException("No se encontró una mascota registrada para el usuario: " + userId));

        // Retorna el objeto Pet, incluyendo el 'photoUrl' real.
        return ResponseEntity.ok(pet);
    }
}