package com.tecsup.pe.back_zonet.service.pet;

import com.tecsup.pe.back_zonet.dto.LostPetDTO;
import com.tecsup.pe.back_zonet.entity.LostPet;
import com.tecsup.pe.back_zonet.entity.Pet;
import com.tecsup.pe.back_zonet.entity.CommunityPost;
import com.tecsup.pe.back_zonet.entity.User; // 💡 NECESARIO: Para la lógica de comunidad y owner.
import com.tecsup.pe.back_zonet.exception.PetNotFoundException;
import com.tecsup.pe.back_zonet.exception.UserLimitExceededException;
import com.tecsup.pe.back_zonet.repository.LostPetRepository;
import com.tecsup.pe.back_zonet.repository.PetRepository;
import com.tecsup.pe.back_zonet.repository.CommunityRepository;
import com.tecsup.pe.back_zonet.repository.UserRepository; // 💡 NECESARIO: Para obtener todos los usuarios (comunidad).
import com.tecsup.pe.back_zonet.service.notification.NotificationService; // 💡 NECESARIO: Servicio de Notificaciones.
import com.tecsup.pe.back_zonet.util.RoleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors; // 💡 NECESARIO: Para el filtro de la comunidad.

@Service
public class LostPetService {

    private static final int FREE_REPORT_LIMIT = 3;

    @Autowired
    private LostPetRepository lostPetRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private CommunityRepository communityRepository; // Para crear el post en la comunidad

    @Autowired
    private RoleValidator roleValidator;

    // 💡 AGREGADO: Inyección del repositorio de usuarios para la comunidad
    @Autowired
    private UserRepository userRepository;

    // 💡 AGREGADO: Inyección del nuevo servicio de notificaciones
    @Autowired
    private NotificationService notificationService;

    /**
     * 🟢 Reporta una mascota como perdida (Alerta de Emergencia del Dueño).
     * Crea un LostPet y un CommunityPost asociado.
     */
    @Transactional
    public LostPet reportAsLost(LostPetDTO request) {

        Pet pet = petRepository.findById(request.getPetId())
                .orElseThrow(() -> new PetNotFoundException("Mascota no encontrada con ID: " + request.getPetId()));

        Long ownerId = pet.getUser().getId(); // Usar ownerId en lugar de userId para claridad en la lógica de notif.

        // 1. Validación de límite de reportes para usuarios Free
        if (roleValidator.isFreeUser(ownerId)) {
            long activeReports = lostPetRepository.countActiveReportsByUserId(ownerId);
            if (activeReports >= FREE_REPORT_LIMIT) {
                throw new UserLimitExceededException("Los usuarios Free solo pueden tener " + FREE_REPORT_LIMIT + " reportes de mascotas perdidas activos.");
            }
        }

        // 2. Crear y guardar el reporte LostPet
        LostPet lostPet = new LostPet();
        lostPet.setPet(pet);
        lostPet.setHoursLost(request.getHoursLost());
        lostPet.setDescription(request.getDescription());
        lostPet.setLastSeenLocation(request.getLastSeenLocation());
        lostPet.setLastSeenLatitude(request.getLastSeenLatitude());
        lostPet.setLastSeenLongitude(request.getLastSeenLongitude());
        lostPet.setFound(false);
        LostPet savedLostPet = lostPetRepository.save(lostPet);

        // 3. Crear el CommunityPost (Alerta Sara M.)
        CommunityPost post = new CommunityPost();
        post.setUser(pet.getUser()); // El dueño es el autor del post
        post.setPostType("LOST_ALERT");
        post.setDescription(request.getDescription());
        post.setImageUrl(pet.getPhotoUrl()); // Se usa la foto que subió al registrar la mascota
        post.setLocationName(request.getLastSeenLocation());
        post.setLatitude(request.getLastSeenLatitude());
        post.setLongitude(request.getLastSeenLongitude());
        post.setLostPetSource(savedLostPet); // Vincula el post al reporte
        communityRepository.save(post);

        // 💡 4A. GENERACIÓN AUTOMÁTICA DE NOTIFICACIÓN DE ALERTA AL DUEÑO
        String ownerTitle = "¡ALERTA DE EMERGENCIA: " + pet.getName() + " PERDIDO!";
        String ownerMessage = "Tu mascota se perdió en " + request.getLastSeenLocation() + ". Revisa el feed de la comunidad.";

        notificationService.createSystemNotification(
                ownerId, // Notificación solo al dueño
                ownerTitle,
                ownerMessage,
                "LOST_ALERT",
                "HIGH" // Prioridad ALTA para la diferenciación Premium/SMS
        );

        // 💡 4B. GENERACIÓN AUTOMÁTICA DE NOTIFICACIÓN A LA COMUNIDAD
        String communityTitle = "🚨 ¡AVISTAMIENTO! Mascota Perdida Cerca 🚨";
        String communityMessage = pet.getName() + " fue visto por última vez en " + request.getLastSeenLocation() + ". Ayuda a buscar.";

        // Obtener todos los usuarios de la comunidad, excluyendo al dueño
        List<User> communityUsers = userRepository.findAll().stream()
                .filter(user -> !user.getId().equals(ownerId)) // Filtra al dueño
                .collect(Collectors.toList());

        // Enviar la notificación a cada usuario de la comunidad
        for (User user : communityUsers) {
            notificationService.createSystemNotification(
                    user.getId(),
                    communityTitle,
                    communityMessage,
                    "COMMUNITY_ALERT",
                    "MEDIUM" // Prioridad media para la comunidad
            );
        }
        // FIN AGREGADO

        return savedLostPet;
    }

    /**
     * Obtiene todos los reportes de mascotas perdidas activos (solo la entidad LostPet).
     */
    public List<LostPet> getAllActiveLostPets() {
        return lostPetRepository.findByFoundFalse();
    }

    // Método para marcar como encontrado... (Se mantiene la lógica anterior)
    @Transactional
    public LostPet markAsFound(Long reportId) {
        LostPet lostPet = lostPetRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado"));

        if (lostPet.isFound()) {
            throw new RuntimeException("Este reporte ya ha sido marcado como encontrado.");
        }

        lostPet.setFound(true);
        lostPetRepository.save(lostPet);

        // 2. ELIMINAR POST DE LA COMUNIDAD Y SUS DEPENDENCIAS
        CommunityPost post = communityRepository.findByLostPetSourceId(lostPet.getId());
        if (post != null) {
            // Al tener cascade = CascadeType.ALL en CommunityPost,
            // borrar el post debería borrar automáticamente comentarios y reacciones
            communityRepository.delete(post);
        }

        // 3. NOTIFICACIÓN A TODA LA COMUNIDAD
        String title = "🎉 ¡Buenas noticias!";
        String message = lostPet.getPet().getName() + " ha sido encontrado. ¡Gracias por tu ayuda!";

        List<User> communityUsers = userRepository.findAll();
        for (User user : communityUsers) {
            notificationService.createSystemNotification(
                    user.getId(), title, message, "FOUND_PET", "MEDIUM"
            );
        }

        return lostPet;
    }
}