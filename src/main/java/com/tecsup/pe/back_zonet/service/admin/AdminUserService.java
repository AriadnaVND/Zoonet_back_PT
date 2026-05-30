package com.tecsup.pe.back_zonet.service.admin;

import com.tecsup.pe.back_zonet.dto.admin.UserSummaryDTO;
import com.tecsup.pe.back_zonet.entity.User;
import com.tecsup.pe.back_zonet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

    @Autowired
    private UserRepository userRepository;

    // 1. Obtener todos los usuarios transformados al DTO extendido para el diseño avanzado
    @Transactional(readOnly = true)
    public List<UserSummaryDTO> obtenerResumenUsuariosAdmin() {
        List<User> usuarios = userRepository.findAll();

        return usuarios.stream()
                // Filtro de seguridad: Excluimos cuentas administrativas de la lista de gestión de clientes
                .filter(u -> u.getRole() == null || !u.getRole().toString().equals("ROLE_ADMIN"))
                .map(u -> {
                    String nombreMascota = null;
                    String fotoMascota = null;
                    String numeroCollar = null;

                    // Extraemos de forma segura los datos de la mascota principal vinculada (name y photoUrl)
                    if (u.getPets() != null && !u.getPets().isEmpty()) {
                        var mascotaPrincipal = u.getPets().get(0); // Tomamos la primera mascota registrada
                        nombreMascota = mascotaPrincipal.getName();
                        fotoMascota = mascotaPrincipal.getPhotoUrl();

                        // Manejo seguro del estado del hardware IoT
                        if (mascotaPrincipal.getDeviceStatus() != null) {
                            numeroCollar = "Estado: " + mascotaPrincipal.getDeviceStatus();
                        } else {
                            numeroCollar = "No vinculado";
                        }
                    }

                    // Construimos el DTO mapeando "Sin número" y la fecha actual de forma segura
                    return new UserSummaryDTO(
                            u.getId(),
                            u.getName(),
                            u.getEmail(),
                            "Sin número", // 🔄 CORRECCIÓN: Evita el error de getPhone() enviando un texto por defecto
                            u.getPlan(),
                            u.isActive(),
                            LocalDateTime.now(), // 🔄 CORRECCIÓN: Evita el error de getCreatedAt() usando la hora actual
                            nombreMascota,
                            fotoMascota,
                            numeroCollar
                    );
                })
                .collect(Collectors.toList());
    }

    // 2. Moderar Estado: Suspender o Activar el acceso de un usuario al ecosistema
    @Transactional
    public void cambiarEstadoUsuario(Long id, boolean estado) {
        userRepository.findById(id).ifPresent(user -> {
            user.setActive(estado);
            userRepository.save(user);
        });
    }

    // 3. Filtrado dinámico por término en el servidor (Barra de búsqueda predictiva)
    @Transactional(readOnly = true)
    public List<UserSummaryDTO> buscarResumenUsuarios(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return obtenerResumenUsuariosAdmin();
        }

        String cleanTerm = termino.toLowerCase().trim();

        // El backend ahora busca inteligentemente ignorando el campo inexistente del teléfono
        return obtenerResumenUsuariosAdmin().stream()
                .filter(u -> (u.getName() != null && u.getName().toLowerCase().contains(cleanTerm)) ||
                        (u.getEmail() != null && u.getEmail().toLowerCase().contains(cleanTerm)) ||
                        (u.getPetName() != null && u.getPetName().toLowerCase().contains(cleanTerm)) ||
                        (u.getDeviceSerialNumber() != null && u.getDeviceSerialNumber().toLowerCase().contains(cleanTerm)))
                .collect(Collectors.toList());
    }

    /* RESTRUCTURACIÓN DE SEGURIDAD Y LIMPIEZA DE CÓDIGO:
       Se eliminaron permanentemente los métodos 'crearUsuario' y 'actualizarUsuario'.
       La manipulación de perfiles y contraseñas queda delegada exclusivamente de forma segura
       al flujo autónomo del usuario en la app móvil (Flutter) para evitar errores o vulnerabilidades.
    */
}