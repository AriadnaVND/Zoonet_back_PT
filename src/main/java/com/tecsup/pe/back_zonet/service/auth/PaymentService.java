package com.tecsup.pe.back_zonet.service.auth;

import com.tecsup.pe.back_zonet.entity.Subscription;
import com.tecsup.pe.back_zonet.entity.User;
import com.tecsup.pe.back_zonet.repository.SubscriptionRepository;
import com.tecsup.pe.back_zonet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class PaymentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    public Subscription makePremiumPayment(Long userId) {
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
