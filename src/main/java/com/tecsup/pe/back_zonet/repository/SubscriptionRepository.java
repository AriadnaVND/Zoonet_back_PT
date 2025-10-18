package com.tecsup.pe.back_zonet.repository;

import com.tecsup.pe.back_zonet.entity.Subscription;
import com.tecsup.pe.back_zonet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUser(User user);
}
