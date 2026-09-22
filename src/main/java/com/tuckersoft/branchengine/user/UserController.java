package com.tuckersoft.branchengine.user;

import com.tuckersoft.branchengine.user.dto.RoleUpdateRequest;
import com.tuckersoft.branchengine.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** Cualquier autenticado. */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> yo() {
        return ResponseEntity.ok(userService.yo());
    }

    /** Solo ROLE_ADMIN: la regla vive en el SecurityFilterChain. */
    @GetMapping
    public ResponseEntity<List<UserResponse>> listar() {
        return ResponseEntity.ok(userService.listar());
    }

    /** Solo ROLE_ADMIN. */
    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponse> cambiarRol(@PathVariable Long id,
                                                   @Valid @RequestBody RoleUpdateRequest peticion) {
        return ResponseEntity.ok(userService.cambiarRol(id, peticion));
    }
}
