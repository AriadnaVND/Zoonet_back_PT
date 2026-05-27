package com.tecsup.pe.back_zonet.service.admin;

import com.tecsup.pe.back_zonet.entity.User;
import com.tecsup.pe.back_zonet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AdminUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> obtenerTodosLosUsuarios() {
        return userRepository.findAll();
    }

    public void cambiarEstadoUsuario(Long id, boolean estado) {
        userRepository.findById(id).ifPresent(user -> {
            user.setActive(estado);
            userRepository.save(user);
        });
    }

    public User crearUsuario(User user) {
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    public User actualizarUsuario(Long id, User datos) {
        return userRepository.findById(id).map(user -> {
            // 🔄 CORRECCIÓN: Cambiado de setNombre/getNombre a setName/getName de la entidad User
            user.setName(datos.getName());
            user.setEmail(datos.getEmail());
            user.setPlan(datos.getPlan());
            user.setActive(datos.isActive());

            if (datos.getPassword() != null && !datos.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(datos.getPassword()));
            }

            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public List<User> buscarUsuarios(String termino) {
        return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(termino, termino);
    }
}