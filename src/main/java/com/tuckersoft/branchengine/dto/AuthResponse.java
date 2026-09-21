package com.tuckersoft.branchengine.dto;

public record AuthResponse(
        String token,
        String type,
        String email,
        String displayName,
        String role
) {
    public AuthResponse(String token, String email, String displayName, String role) {
        this(token, "Bearer", email, displayName, role);
    }
}
