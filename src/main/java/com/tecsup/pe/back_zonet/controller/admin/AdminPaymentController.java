package com.tecsup.pe.back_zonet.controller.admin;

import com.tecsup.pe.back_zonet.entity.Payment;
import com.tecsup.pe.back_zonet.repository.PaymentRepository;
// 🟢 IMPORTACIÓN CORREGIDA SEGÚN LA RUTA QUE ME DISTE
import com.tecsup.pe.back_zonet.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/payments")
@CrossOrigin(origins = "*")
public class AdminPaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllPayments() {
        try {
            List<Payment> payments = paymentRepository.findAll();
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    @PostMapping("/remind/{userId}")
    public ResponseEntity<?> sendPaymentReminder(@PathVariable Long userId) {
        try {
            // Usamos createSystemNotification que ya tienes en tu servicio
            notificationService.createSystemNotification(
                    userId,
                    "Recordatorio de Pago",
                    "Tienes un pago pendiente en Zoonet. ¡Activa tu plan Premium ahora!",
                    "PAYMENT_REMINDER",
                    "HIGH"
            );

            return ResponseEntity.ok(Map.of("message", "Notificación enviada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ... tu método approve sigue igual
}