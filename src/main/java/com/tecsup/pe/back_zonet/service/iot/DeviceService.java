package com.tecsup.pe.back_zonet.service.iot;

import com.tecsup.pe.back_zonet.dto.DeviceStatusDTO;
import com.tecsup.pe.back_zonet.entity.Pet;
import com.tecsup.pe.back_zonet.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeviceService {

    @Autowired
    private PetRepository petRepository;

    /**
     * Simula el cambio de estado del dispositivo (Collar).
     */
    public DeviceStatusDTO changeDeviceStatus(Long petId, String action) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada con ID: " + petId));

        String newStatus;
        String message;

        // Lógica simple de cambio de estado
        switch (action.toLowerCase()) {
            case "connect":
                newStatus = "CONNECTED";
                message = "Collar de " + pet.getName() + " conectado exitosamente.";
                break;
            case "disconnect":
                newStatus = "DISCONNECTED";
                message = "Collar desconectado.";
                break;
            case "search":
                newStatus = "SEARCHING";
                message = "Buscando collar cercano vía Bluetooth...";
                break;
            default:
                throw new RuntimeException("Acción no válida. Use: connect, disconnect, search");
        }

        // Guardamos el estado en la base de datos
        pet.setDeviceStatus(newStatus);
        petRepository.save(pet);

        return new DeviceStatusDTO(pet.getId(), pet.getName(), newStatus, message);
    }

    /**
     * Obtiene el estado actual (para saber si pintar el botón verde o rojo al entrar).
     */
    public DeviceStatusDTO getDeviceStatus(Long petId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada con ID: " + petId));

        // Si es null (mascotas antiguas), asumimos desconectado
        String currentStatus = (pet.getDeviceStatus() != null) ? pet.getDeviceStatus() : "DISCONNECTED";

        return new DeviceStatusDTO(
                pet.getId(),
                pet.getName(),
                currentStatus,
                "Estado actual recuperado"
        );
    }
}