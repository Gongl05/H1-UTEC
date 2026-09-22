package com.tuckersoft.branchengine.config;

import com.tuckersoft.branchengine.user.User;
import com.tuckersoft.branchengine.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Crea al administrador al arrancar, leyendo las credenciales del .env.
 *
 * Si ya existe un usuario con ese email no hace nada: ni lo pisa ni le cambia el rol.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.display-name}")
    private String nombreAdmin;

    @Value("${app.admin.email}")
    private String emailAdmin;

    @Value("${app.admin.password}")
    private String passwordAdmin;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(emailAdmin)) {
            log.info("El administrador {} ya existe: no se toca.", emailAdmin);
            return;
        }

        User admin = new User();
        admin.setEmail(emailAdmin);
        admin.setDisplayName(nombreAdmin);
        admin.setPassword(passwordEncoder.encode(passwordAdmin));
        admin.setRole(User.ROLE_ADMIN);
        admin.setCreatedAt(Instant.now());

        userRepository.save(admin);
        log.info("Administrador creado: {}", emailAdmin);
    }
}
