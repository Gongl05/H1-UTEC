package com.tuckersoft.branchengine.user.dto;

/** Respuesta comun de registro y login. Nunca incluye la contrasena. */
public record AuthResponse(
        String token,
        String type,
        String email,
        String displayName,
        String role) {

    public static AuthResponse de(String token, com.tuckersoft.branchengine.user.User usuario) {
        return new AuthResponse(token, "Bearer", usuario.getEmail(), usuario.getDisplayName(), usuario.getRole());
    }
}
