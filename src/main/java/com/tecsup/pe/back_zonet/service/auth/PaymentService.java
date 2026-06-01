package com.tecsup.pe.back_zonet.service.auth;

import com.tecsup.pe.back_zonet.entity.Payment; // 🟢 Importación necesaria
import com.tecsup.pe.back_zonet.entity.Subscription;
import com.tecsup.pe.back_zonet.entity.User;
import com.tecsup.pe.back_zonet.repository.PaymentRepository; // 🟢 Importación necesaria
import com.tecsup.pe.back_zonet.repository.SubscriptionRepository;
import com.tecsup.pe.back_zonet.repository.UserRepository;
import com.tecsup.pe.back_zonet.dto.PaymentRequest;
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

    @Autowired
    private PaymentRepository paymentRepository; // 🟢 Inyección añadida

    /**
     * Procesa la simulación de pago, guarda la suscripción y registra el pago.
     */
    @Transactional
    public Subscription processPremiumPayment(Long userId, PaymentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 1. VALIDACIONES DE FORMATO
        if (request.getCardNumber() == null || !request.getCardNumber().matches("\\d{16}")) {
            throw new RuntimeException("Número de tarjeta inválido. Debe tener 16 dígitos exactos.");
        }
        if (request.getCvv() == null || !request.getCvv().matches("\\d{3}")) {
            throw new RuntimeException("CVV inválido. Debe tener 3 dígitos exactos.");
        }
        if (request.getExpirationMonth() == null || request.getExpirationYear() == null ||
                !request.getExpirationMonth().matches("\\d{1,2}") || !request.getExpirationYear().matches("\\d{2}")) {
            throw new RuntimeException("Fecha de expiración inválida. Use formato MM/YY.");
        }

        // 2. PERSISTIR SUSCRIPCIÓN
        user.setPlan("PREMIUM");
        userRepository.save(user);

        Subscription sub = new Subscription();
        sub.setUser(user);
        sub.setPlan("PREMIUM");
        sub.setStartDate(LocalDate.now());
        sub.setEndDate(LocalDate.now().plusMonths(1));
        Subscription savedSub = subscriptionRepository.save(sub);

        // 3. 🟢 GUARDAR EL PAGO (Esto hará que aparezca en tu AdminPanel)
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setAmount(39.99); // Monto fijo como ejemplo
        payment.setStatus("COMPLETED"); // Cambia a "PENDING" si quieres aprobación manual
        payment.setPaymentDate(LocalDate.now());

        paymentRepository.save(payment);

        return savedSub;
    }

    /**
     * Confirmación de pago (Mantenido igual pero añadiendo registro de Payment también).
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
        Subscription savedSub = subscriptionRepository.save(sub);

        // 🟢 Registro de pago en confirmación
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setAmount(39.99);
        payment.setStatus("COMPLETED");
        payment.setPaymentDate(LocalDate.now());
        paymentRepository.save(payment);

        return savedSub;
    }
}