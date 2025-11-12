package com.tecsup.pe.back_zonet.service.auth;

import com.tecsup.pe.back_zonet.entity.Subscription;
import com.tecsup.pe.back_zonet.entity.User;
import com.tecsup.pe.back_zonet.repository.SubscriptionRepository;
import com.tecsup.pe.back_zonet.repository.UserRepository;
import com.tecsup.pe.back_zonet.dto.PaymentRequest; // 💡 AGREGADO
import com.tecsup.pe.back_zonet.dto.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service
public class PaymentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    /**
     * ❌ Eliminado: Ya no se necesita una redirección, el frontend manejará el formulario y llamará a process.
     public PaymentResponse createPaymentRedirect(Long userId) {
     userRepository.findById(userId)
     .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
     // Simulación: URL que lleva a la "Pasarela de Pago" de la imagen.
     String dummyPaymentUrl = "https://pasarela-zoonet.com/checkout?user=" + userId + "&amount=15.00";
     return new PaymentResponse(dummyPaymentUrl, "PREMIUM", userId);
     }
     */

    /**
     * 🟢 NUEVO: Procesa la simulación de pago con los datos de tarjeta y valida formato.
     */
    @Transactional
    public Subscription processPremiumPayment(Long userId, PaymentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 1. SIMULACIÓN DE VALIDACIÓN DEL FORMATO DE LA TARJETA (según la solicitud)

        // Número de tarjeta: Asumimos 16 dígitos exactos
        if (request.getCardNumber() == null || !request.getCardNumber().matches("\\d{16}")) {
            throw new RuntimeException("Número de tarjeta inválido. Debe tener 16 dígitos exactos.");
        }
        // CVV: 3 dígitos exactos
        if (request.getCvv() == null || !request.getCvv().matches("\\d{3}")) {
            throw new RuntimeException("CVV inválido. Debe tener 3 dígitos exactos.");
        }
        // Fecha: (MM/YY) - Se valida el formato de dos dígitos para mes y año
        if (request.getExpirationMonth() == null || request.getExpirationYear() == null ||
                !request.getExpirationMonth().matches("\\d{1,2}") || !request.getExpirationYear().matches("\\d{2}")) {
            throw new RuntimeException("Fecha de expiración inválida. Use formato MM/YY.");
        }

        // 2. SIMULACIÓN DE CONFIRMACIÓN DE PAGO (si las validaciones de formato pasan, se simula el éxito)

        // 3. Persistir la suscripción
        user.setPlan("PREMIUM");
        userRepository.save(user);

        Subscription sub = new Subscription();
        sub.setUser(user);
        sub.setPlan("PREMIUM");
        sub.setStartDate(LocalDate.now());
        sub.setEndDate(LocalDate.now().plusMonths(1));

        return subscriptionRepository.save(sub);
    }

    /**
     * 🟢 Paso 3 (Confirmación - Mantenido): Se mantiene el método original.
     */
    @Transactional
    public Subscription completePremiumPayment(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setPlan("PREMIUM");
        userRepository.save(user);

        Subscription sub = new Subscription();
        sub.setUser(user);
        sub.setPlan("PREMIUM");
        sub.setStartDate(LocalDate.now());
        sub.setEndDate(LocalDate.now().plusMonths(1));

        return subscriptionRepository.save(sub);
    }
}