package com.tecsup.pe.back_zonet.service.pet;

import com.tecsup.pe.back_zonet.dto.LostPetDTO;
import com.tecsup.pe.back_zonet.entity.LostPet;
import com.tecsup.pe.back_zonet.entity.Pet;
import com.tecsup.pe.back_zonet.entity.CommunityPost;
import com.tecsup.pe.back_zonet.exception.PetNotFoundException;
import com.tecsup.pe.back_zonet.exception.UserLimitExceededException;
import com.tecsup.pe.back_zonet.repository.LostPetRepository;
import com.tecsup.pe.back_zonet.repository.PetRepository;
import com.tecsup.pe.back_zonet.repository.CommunityRepository;
import com.tecsup.pe.back_zonet.util.RoleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * 🟢 Reporta una mascota como perdida (Alerta de Emergencia del Dueño).
     * Crea un LostPet y un CommunityPost asociado.
     */
    @Transactional
    public LostPet reportAsLost(LostPetDTO request) {

        Pet pet = petRepository.findById(request.getPetId())
                .orElseThrow(() -> new PetNotFoundException("Mascota no encontrada con ID: " + request.getPetId()));

        Long userId = pet.getUser().getId();

        // 1. Validación de límite de reportes para usuarios Free
        if (roleValidator.isFreeUser(userId)) {
            long activeReports = lostPetRepository.countActiveReportsByUserId(userId);
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
                .orElseThrow(() -> new RuntimeException("Reporte de mascota perdida no encontrado"));

        if (lostPet.isFound()) {
            throw new RuntimeException("Este reporte ya ha sido marcado como encontrado.");
        }

        lostPet.setFound(true);
        lostPetRepository.save(lostPet);

        // Opcional: Marcar el CommunityPost asociado como "Inactivo" o eliminarlo
        if (lostPet.getCommunityPost() != null) {
            // Ejemplo: communityRepository.delete(lostPet.getCommunityPost());
        }

        return lostPet;
    }
}