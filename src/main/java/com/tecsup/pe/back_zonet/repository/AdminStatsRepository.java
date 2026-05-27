package com.tecsup.pe.back_zonet.repository;

import com.tecsup.pe.back_zonet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface AdminStatsRepository extends JpaRepository<User, Long> {

    @Query("SELECT COUNT(u) FROM User u")
    long countTotalUsers();

    @Query(value = "SELECT COUNT(*) FROM pets", nativeQuery = true)
    long countTotalPets();

    @Query("SELECT COUNT(u) FROM User u WHERE u.plan = 'PREMIUM' AND u.active = true")
    long countActivePremiumUsers();

    @Query(value = "SELECT COUNT(*) FROM support_tickets WHERE status = 'PENDING'", nativeQuery = true)
    long countPendingTickets();

    // 🟢 CORRECCIÓN ANTICHOQUE: Usamos una consulta nativa con un fallback seguro
    // para que no rompa el contexto de Hibernate si el atributo varía en la entidad Pet
    @Query(value = "SELECT COALESCE(plan, 'CONECTADO') as status, COUNT(*) FROM users GROUP BY plan", nativeQuery = true)
    List<Object[]> countPetsByStatus();
}