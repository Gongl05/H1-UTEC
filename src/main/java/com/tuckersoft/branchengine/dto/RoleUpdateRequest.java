package com.tuckersoft.branchengine.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleUpdateRequest(
        @NotBlank(message = "El rol es obligatorio")
        String role
) {}
