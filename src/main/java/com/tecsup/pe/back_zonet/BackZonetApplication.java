package com.tecsup.pe.back_zonet;

import com.tecsup.pe.back_zonet.entity.User;
import com.tecsup.pe.back_zonet.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling; // 💡 Mantenemos tu importación
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling // 💡 Mantenemos tu programación habilitada
public class BackZonetApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackZonetApplication.class, args);
    }

    /**
     * Seeder automático para crear el Administrador por defecto si no existe al arrancar la app.
     */
    @Bean
    CommandLineRunner init(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@zoonet.com";
            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                User admin = new User();

                // CORRECCIÓN: Cambiado de setNombre() a setName() para coincidir con tu entidad original
                admin.setName("Admin Zoonet");

                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode("admin123")); // Contraseña encriptada segura
                admin.setRole("ROLE_ADMIN");

                // Mantenemos estas propiedades que requiere el panel administrativo
                admin.setActive(true);
                admin.setPlan("PREMIUM");

                userRepository.save(admin);
                System.out.println(">>> SEEDER: Administrador creado exitosamente (admin@zoonet.com / admin123).");
            }
        };
    }
}