package com.tuckersoft.branchengine.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LoginRequest(

        @NotBlank(message = "es obligatorio")
        String email,

        @NotBlank(message = "es obligatoria")
        String password) {
}
