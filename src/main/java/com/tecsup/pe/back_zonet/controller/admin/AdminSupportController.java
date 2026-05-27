package com.tecsup.pe.back_zonet.controller.admin;

import com.tecsup.pe.back_zonet.entity.Notification;
import com.tecsup.pe.back_zonet.entity.SupportTicket;
import com.tecsup.pe.back_zonet.repository.NotificationRepository;
import com.tecsup.pe.back_zonet.repository.SupportRepository;
import com.tecsup.pe.back_zonet.service.admin.AdminSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/support")
@CrossOrigin(origins = "*")
public class AdminSupportController {

    @Autowired
    private SupportRepository supportTicketRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AdminSupportService adminSupportService;

    @GetMapping("/tickets")
    public List<SupportTicket> getAllTickets() {
        return supportTicketRepository.findAll();
    }

    @PostMapping("/tickets/{id}/resolve-and-notify")
    public ResponseEntity<?> resolveAndNotify(@PathVariable Long id) {
        SupportTicket ticket = supportTicketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        ticket.setStatus("CLOSED");
        supportTicketRepository.save(ticket);

        Notification aviso = new Notification();

        // 🔄 CORRECCIÓN: Se obtiene el ID a través del objeto User relacionado (ticket.getUser().getId())
        if (ticket.getUser() != null) {
            aviso.setRecipientUser(ticket.getUser());
        }

        aviso.setMessage("Soporte Zoonet: Tu ticket #" + ticket.getId() + " sobre '" + ticket.getSubject() + "' ha sido resuelto.");
        aviso.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(aviso);

        return ResponseEntity.ok(java.util.Map.of("message", "Usuario notificado y ticket cerrado con éxito"));
    }

    @PutMapping("/tickets/{id}/status")
    public ResponseEntity<?> updateTicketStatus(@PathVariable Long id) {
        adminSupportService.answerTicket(id);
        return ResponseEntity.ok(java.util.Map.of("message", "Estado de ticket actualizado a CLOSED"));
    }
}