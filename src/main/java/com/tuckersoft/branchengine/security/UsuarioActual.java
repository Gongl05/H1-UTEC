package com.tuckersoft.branchengine.security;

import com.tuckersoft.branchengine.common.exception.NotFoundException;
import com.tuckersoft.branchengine.user.User;
import com.tuckersoft.branchengine.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * El usuario dueno del token.
 *
 * Se resuelve aqui, en un bean, y no con llamadas estaticas a SecurityContextHolder
 * repartidas por los services: asi los tests unitarios pueden sustituirlo por un
 * mock sin levantar Spring ni la base de datos.
 */
@Component
@RequiredArgsConstructor
public class UsuarioActual {

    private final UserRepository userRepository;

    public User obtener() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !autenticacion.isAuthenticated()) {
            throw new NotFoundException("No hay un usuario autenticado en el contexto.");
        }
        return userRepository.findByEmail(autenticacion.getName())
                .orElseThrow(() -> new NotFoundException("El usuario del token ya no existe."));
    }

    public boolean esAdmin(User usuario) {
        return User.ROLE_ADMIN.equals(usuario.getRole());
    }
}
