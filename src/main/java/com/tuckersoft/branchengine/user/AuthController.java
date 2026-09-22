package com.tuckersoft.branchengine.user;

import com.tuckersoft.branchengine.user.dto.AuthResponse;
import com.tuckersoft.branchengine.user.dto.LoginRequest;
import com.tuckersoft.branchengine.user.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Las dos unicas rutas abiertas sin token. */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegisterRequest peticion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registrar(peticion));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> entrar(@Valid @RequestBody LoginRequest peticion) {
        return ResponseEntity.ok(userService.entrar(peticion));
    }
}
