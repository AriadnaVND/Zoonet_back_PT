package com.tecsup.pe.back_zonet.service.admin;

import com.tecsup.pe.back_zonet.entity.SupportTicket;
import com.tecsup.pe.back_zonet.repository.SupportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AdminSupportService {

    @Autowired
    private SupportRepository supportTicketRepo;

    /**
     * Obtiene la lista global de incidencias para el panel web del administrador
     */
    public List<SupportTicket> getAllTicketsForAdmin() {
        return supportTicketRepo.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Resuelve y cierra un ticket de soporte utilizando los campos unificados de la entidad
     */
    public void answerTicket(Long ticketId) {
        supportTicketRepo.findById(ticketId).ifPresent(t -> {
            // 🔄 CORRECCIÓN: Cambiado de setEstado("CLOSED") a setStatus("CLOSED")
            // para alinearse con los atributos reales en inglés de tu entidad base
            t.setStatus("CLOSED");
            supportTicketRepo.save(t);
        });
    }
}