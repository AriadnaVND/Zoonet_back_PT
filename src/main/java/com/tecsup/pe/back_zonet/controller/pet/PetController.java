package com.tecsup.pe.back_zonet.controller.pet;

import com.tecsup.pe.back_zonet.entity.Pet;
import com.tecsup.pe.back_zonet.entity.User;
import com.tecsup.pe.back_zonet.entity.Subscription;
import com.tecsup.pe.back_zonet.service.pet.PetService;
import com.tecsup.pe.back_zonet.service.auth.PaymentService;
import com.tecsup.pe.back_zonet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private PaymentService paymentService; // <--- Inyectamos el servicio de pagos

    private static final String UPLOAD_DIR = "uploads/";

    /**
     * 📸 Registrar mascota y plan del usuario
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
            petService.save(pet);

            // Actualizar plan del usuario y crear suscripción si es PREMIUM
            if (planType.equalsIgnoreCase("premium")) {
                Subscription subscription = paymentService.makePremiumPayment(userId);
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body("Mascota registrada y plan PREMIUM activado. Suscripción creada con ID: " + subscription.getId());
            } else {
                user.setPlan("FREE");
                userRepository.save(user);
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body("Mascota registrada con plan FREE");
            }

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar la foto: " + e.getMessage());
        }
    }
}
