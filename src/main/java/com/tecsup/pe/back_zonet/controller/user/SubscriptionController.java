package com.tecsup.pe.back_zonet.controller.user;

import com.tecsup.pe.back_zonet.entity.Subscription;
import com.tecsup.pe.back_zonet.entity.User;
import com.tecsup.pe.back_zonet.repository.SubscriptionRepository;
import com.tecsup.pe.back_zonet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @PostMapping("/{userId}")
    public ResponseEntity<?> selectPlan(
            @PathVariable Long userId,
            @RequestBody Map<String, String> body
    ) {
        String planType = body.get("planType");
        if (planType == null) {
            return ResponseEntity.badRequest().body("Debe especificar planType: free o premium");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (planType.equalsIgnoreCase("free")) {
            user.setPlan("FREE");
            userRepository.save(user);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Plan gratuito activado. Bienvenido a Zoonet Free.");
            return ResponseEntity.ok(response);

        } else if (planType.equalsIgnoreCase("premium")) {
            user.setPlan("PREMIUM");
            userRepository.save(user);

            // Crear suscripción
            Subscription sub = new Subscription();
            sub.setUser(user);
            sub.setPlan("PREMIUM");
            sub.setStartDate(LocalDate.now());
            sub.setEndDate(LocalDate.now().plusMonths(1));
            subscriptionRepository.save(sub);

            Map<String, String> response = new HashMap<>();
            response.put("redirectUrl", "https://pasarela-pagos.com/checkout?userId=" + userId + "&plan=premium");
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest().body("Plan inválido");
    }
}
