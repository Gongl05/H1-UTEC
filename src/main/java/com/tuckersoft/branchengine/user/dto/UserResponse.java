package com.tuckersoft.branchengine.user.dto;

import com.tuckersoft.branchengine.user.User;

import java.time.Instant;

/** DTO publico del usuario. Sin contrasena, ni siquiera codificada. */
public record UserResponse(
        Long id,
        String email,
        String displayName,
        String role,
        Instant createdAt) {

    public static UserResponse de(User usuario) {
        return new UserResponse(usuario.getId(), usuario.getEmail(), usuario.getDisplayName(),
                usuario.getRole(), usuario.getCreatedAt());
    }
}
