package com.tuckersoft.branchengine.user;

import com.tuckersoft.branchengine.common.exception.BadRequestException;
import com.tuckersoft.branchengine.common.exception.ConflictException;
import com.tuckersoft.branchengine.common.exception.NotFoundException;
import com.tuckersoft.branchengine.security.JwtService;
import com.tuckersoft.branchengine.security.UsuarioActual;
import com.tuckersoft.branchengine.user.dto.AuthResponse;
import com.tuckersoft.branchengine.user.dto.LoginRequest;
import com.tuckersoft.branchengine.user.dto.RegisterRequest;
import com.tuckersoft.branchengine.user.dto.RoleUpdateRequest;
import com.tuckersoft.branchengine.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Set<String> ROLES_VALIDOS = Set.of(User.ROLE_USER, User.ROLE_ADMIN);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UsuarioActual usuarioActual;

    /**
     * Alta de un analista.
     *
     * El rol se fija aqui y siempre es ROLE_USER. El DTO de entrada ni siquiera tiene
     * campo "role": si el cliente lo manda en el JSON, se descarta.
     */
    @Transactional
    public AuthResponse registrar(RegisterRequest peticion) {
        if (userRepository.existsByEmail(peticion.email())) {
            throw new ConflictException("Ya existe una cuenta registrada con ese email.");
        }

        User usuario = new User();
        usuario.setEmail(peticion.email());
        usuario.setDisplayName(peticion.displayName());
        usuario.setPassword(passwordEncoder.encode(peticion.password()));
        usuario.setRole(User.ROLE_USER);
        usuario.setCreatedAt(Instant.now());

        User guardado = userRepository.save(usuario);
        return AuthResponse.de(jwtService.generar(guardado.getEmail()), guardado);
    }

    /**
     * Login.
     *
     * Una contrasena incorrecta y un email inexistente producen exactamente la misma
     * respuesta 401: no se revela cual de los dos fallo.
     */
    @Transactional(readOnly = true)
    public AuthResponse entrar(LoginRequest peticion) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(peticion.email(), peticion.password()));

        User usuario = userRepository.findByEmail(peticion.email())
                .orElseThrow(() -> new NotFoundException("No existe ese usuario."));

        return AuthResponse.de(jwtService.generar(usuario.getEmail()), usuario);
    }

    @Transactional(readOnly = true)
    public UserResponse yo() {
        return UserResponse.de(usuarioActual.obtener());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listar() {
        return userRepository.findAllByOrderByIdAsc().stream().map(UserResponse::de).toList();
    }

    /**
     * Cambio de rol, solo para administradores.
     *
     * Nadie puede cambiarse el rol a si mismo: si el unico administrador se degradara,
     * la instalacion se quedaria sin ningun administrador y sin forma de recuperarlo.
     */
    @Transactional
    public UserResponse cambiarRol(Long id, RoleUpdateRequest peticion) {
        String rol = peticion.role() == null ? null : peticion.role().trim();
        if (!ROLES_VALIDOS.contains(rol)) {
            throw new BadRequestException("El rol debe ser ROLE_USER o ROLE_ADMIN.");
        }

        User objetivo = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe un usuario con id " + id + "."));

        if (objetivo.getId().equals(usuarioActual.obtener().getId())) {
            throw new BadRequestException("Un administrador no puede cambiar su propio rol.");
        }

        objetivo.setRole(rol);
        return UserResponse.de(userRepository.save(objetivo));
    }
}
