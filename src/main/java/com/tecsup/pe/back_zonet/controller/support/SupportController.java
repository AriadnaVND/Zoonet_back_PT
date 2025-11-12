package com.tecsup.pe.back_zonet.controller.support;

import com.tecsup.pe.back_zonet.dto.SupportTicketRequest;
import com.tecsup.pe.back_zonet.entity.SupportTicket;
import com.tecsup.pe.back_zonet.service.support.SupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/support/tickets")
public class SupportController {

    @Autowired
    private SupportService supportService;

    /**
     * 🟢 POST /api/support/tickets
     * Crear ticket de soporte.
     */
    @PostMapping
    public ResponseEntity<?> createTicket(@RequestBody SupportTicketRequest request) {
        try {
            SupportTicket ticket = supportService.createTicket(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 🟢 GET /api/support/tickets/{userId}
     * Ver tickets de usuario.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getTicketsByUserId(@PathVariable Long userId) {
        try {
            List<SupportTicket> tickets = supportService.getTicketsByUserId(userId);
            return ResponseEntity.ok(tickets);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}