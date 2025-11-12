package com.tecsup.pe.back_zonet.controller.auth;

import com.tecsup.pe.back_zonet.entity.Subscription;
import com.tecsup.pe.back_zonet.service.auth.PaymentService;
import com.tecsup.pe.back_zonet.dto.PaymentRequest; // 💡 AGREGADO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * 🟢 NUEVO ENDPOINT: POST /api/payment/process/{userId}
     * Simula la pasarela de pago recibiendo los datos de la tarjeta y valida formato.
     */
    @PostMapping("/process/{userId}")
    public ResponseEntity<?> processPayment(@PathVariable Long userId, @RequestBody PaymentRequest request) {
        try {
            Subscription subscription = paymentService.processPremiumPayment(userId, request);
            return ResponseEntity.ok(subscription);
        } catch (RuntimeException e) {
            // Devuelve error de validación de formato (ej. 16 dígitos, 3 CVV)
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    /**
     * 🟢 Endpoint de Confirmación de Pago (Webhook Simulado) - Mantenido.
     */
    @PostMapping("/confirm/{userId}")
    public ResponseEntity<Subscription> confirmPremiumPayment(@PathVariable Long userId) {
        Subscription subscription = paymentService.completePremiumPayment(userId);
        return ResponseEntity.ok(subscription);
    }
}