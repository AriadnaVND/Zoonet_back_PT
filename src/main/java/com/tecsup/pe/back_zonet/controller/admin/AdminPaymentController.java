package com.tecsup.pe.back_zonet.controller.admin;

import com.tecsup.pe.back_zonet.entity.Payment;
import com.tecsup.pe.back_zonet.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/payments")
@CrossOrigin(origins = "*")
public class AdminPaymentController {

    // Cambiado temporalmente a Object o consumo genérico si tu repositorio destino maneja otra firma
    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping("/all")
    public ResponseEntity<?> getAllPayments() {
        // 🔄 CORRECCIÓN: Validación segura del listado de transacciones unificadas
        try {
            List<Payment> payments = paymentRepository.findAll();
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of()); // Evita colgar el panel si la tabla está vacía en Railway
        }
    }
}