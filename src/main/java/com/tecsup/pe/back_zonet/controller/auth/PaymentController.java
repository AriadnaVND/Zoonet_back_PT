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

    @PostMapping("/premium/{userId}")
    public ResponseEntity<Subscription> makePremium(@PathVariable Long userId) {
        Subscription subscription = paymentService.makePremiumPayment(userId);
        return ResponseEntity.ok(subscription);
    }
}
