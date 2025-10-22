package com.tecsup.pe.back_zonet.service.auth;

import com.tecsup.pe.back_zonet.entity.Subscription;
import com.tecsup.pe.back_zonet.entity.User;
import com.tecsup.pe.back_zonet.repository.SubscriptionRepository;
import com.tecsup.pe.back_zonet.repository.UserRepository;
import com.tecsup.pe.back_zonet.dto.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 💡 CORRECCIÓN: Importar Transactional
import java.time.LocalDate;

@Service
public class PaymentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    /**
     * 🟢 Paso 1 (Iniciación): Simula el inicio de la transacción y devuelve la URL.
     */
    public PaymentResponse createPaymentRedirect(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Simulación: URL que lleva a la "Pasarela de Pago" de la imagen.
        String dummyPaymentUrl = "https://pasarela-zoonet.com/checkout?user=" + userId + "&amount=15.00";

        return new PaymentResponse(dummyPaymentUrl, "PREMIUM", userId);
    }

    /**
     * 🟢 Paso 3 (Confirmación): Finaliza la transacción y persiste los datos de suscripción.
     * Se usa después de que la pasarela confirma el pago.
     */
    @Transactional // 💡 CORRECCIÓN: Añadir anotación para garantizar la integridad
    public Subscription completePremiumPayment(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Solo se ejecuta esta parte cuando el pago ha sido CONFIRMADO
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