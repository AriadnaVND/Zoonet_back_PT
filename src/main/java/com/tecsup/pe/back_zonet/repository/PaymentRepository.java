package com.tecsup.pe.back_zonet.repository;

import com.tecsup.pe.back_zonet.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
// 🟢 AL EXTENDER DE JPAREPOSITORY, LE DAMOS A TU CONTROLLER EL MÉTODO findAll() AUTOMÁTICAMENTE
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Aquí puedes mantener tus métodos personalizados existentes de la app móvil si los tenías
}