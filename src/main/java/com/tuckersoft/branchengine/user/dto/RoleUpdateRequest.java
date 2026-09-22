package com.tuckersoft.branchengine.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RoleUpdateRequest(

        @NotBlank(message = "es obligatorio")
        String role) {
}
