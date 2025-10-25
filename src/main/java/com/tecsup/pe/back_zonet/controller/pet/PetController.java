package com.tecsup.pe.back_zonet.controller.pet;

import com.tecsup.pe.back_zonet.entity.Pet;
import com.tecsup.pe.back_zonet.entity.User;
import com.tecsup.pe.back_zonet.exception.PetNotFoundException;
import com.tecsup.pe.back_zonet.service.pet.PetService;
import com.tecsup.pe.back_zonet.service.auth.PaymentService;
import com.tecsup.pe.back_zonet.repository.UserRepository;
import com.tecsup.pe.back_zonet.dto.PaymentResponse; // 💡 CORRECCIÓN: Añadir import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.tecsup.pe.back_zonet.repository.PetRepository;

import java.util.HashMap;
import java.util.Map; // 💡 CORRECCIÓN: Añadir import

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
    private PaymentService paymentService;

    @Autowired
    private PetRepository petRepository;

    private static final String UPLOAD_DIR = "uploads/";

    /**
     * 📸 Registrar mascota y plan del usuario
     * 💡 Lógica actualizada: Para el plan PREMIUM, ahora devuelve una URL de redirección
     * de la pasarela de pagos (PaymentResponse).
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

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("petId", pet.getId());
            responseData.put("planType", planType);

            // 💡 LÓGICA DE PAGO ACTUALIZADA: Devolver la URL de la pasarela
            if (planType.equalsIgnoreCase("premium")) {
                // Inicia la transacción y obtiene el objeto de respuesta de pago (PaymentResponse)
                PaymentResponse paymentResponse = paymentService.createPaymentRedirect(userId); // 💡 CORRECCIÓN: Usar el tipo PaymentResponse

                // El frontend recibirá el objeto con la URL de redirección
                return ResponseEntity.ok(paymentResponse);
            } else {
                // Si es FREE, actualiza el plan y devuelve el mensaje estructurado
                user.setPlan("FREE");
                userRepository.save(user);

                // Si es FREE, devuelve un mensaje simple con estructura JSON
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(Map.of("message", "Mascota registrada con plan FREE", "planType", "FREE"));
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