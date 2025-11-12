package com.tecsup.pe.back_zonet.service.support;

import com.tecsup.pe.back_zonet.dto.SupportTicketRequest;
import com.tecsup.pe.back_zonet.entity.SupportTicket;
import com.tecsup.pe.back_zonet.entity.User;
import com.tecsup.pe.back_zonet.repository.SupportRepository;
import com.tecsup.pe.back_zonet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupportService {

    @Autowired
    private SupportRepository supportRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 🟢 Crea un nuevo ticket de soporte.
     */
    public SupportTicket createTicket(SupportTicketRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + request.getUserId()));

        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new RuntimeException("La descripción del problema no puede estar vacía.");
        }

        SupportTicket ticket = new SupportTicket();
        ticket.setUser(user);
        ticket.setSubject(request.getSubject() != null ? request.getSubject() : "Problema de la aplicación");
        ticket.setDescription(request.getDescription().trim());

        return supportRepository.save(ticket);
    }

    /**
     * 🟢 Obtiene todos los tickets de soporte para un usuario.
     */
    public List<SupportTicket> getTicketsByUserId(Long userId) {
        // Asegura que el usuario exista
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + userId);
        }
        return supportRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}