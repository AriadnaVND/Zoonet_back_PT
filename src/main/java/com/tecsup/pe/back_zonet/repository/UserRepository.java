package com.tecsup.pe.back_zonet.repository;

import com.tecsup.pe.back_zonet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    // Método para filtrar usuarios por nombre o correo (ignorando mayúsculas y minúsculas)
    List<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
}