package com.tuckersoft.branchengine.config;

import com.tuckersoft.branchengine.entity.User;
import com.tuckersoft.branchengine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.display-name:Colin Ritman}")
    private String adminName;

    @Value("${app.admin.email:colin@tuckersoft.co.uk}")
    private String adminEmail;

    @Value("${app.admin.password:colin1984}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .displayName(adminName)
                    .role("ROLE_ADMIN")
                    .createdAt(Instant.now())
                    .build();
            userRepository.save(admin);
            log.info("Usuario administrador inicializado exitosamente: {}", adminEmail);
        }
    }
}
