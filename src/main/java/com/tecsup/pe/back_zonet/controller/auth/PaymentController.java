package com.tecsup.pe.back_zonet.controller.auth;

import com.tecsup.pe.back_zonet.entity.Subscription;
import com.tecsup.pe.back_zonet.service.auth.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * 🟢 Endpoint de Confirmación de Pago (Webhook Simulado)
     * Resuelve el error de makePremiumPayment al usar el nuevo flujo.
     */
    @PostMapping("/confirm/{userId}") // 💡 CORRECCIÓN: Ruta para la confirmación del pago
    public ResponseEntity<Subscription> confirmPremiumPayment(@PathVariable Long userId) { // 💡 CORRECCIÓN: Nombre de método
        // 💡 CORRECCIÓN: Llama al método que completa la transacción
        Subscription subscription = paymentService.completePremiumPayment(userId);
        return ResponseEntity.ok(subscription);
    }
}