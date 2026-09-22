package com.tuckersoft.branchengine.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Un campo "role" en el cuerpo se ignora deliberadamente: el rol lo fija el service,
 * nunca el cliente. Aceptarlo seria una escalada de privilegios.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RegisterRequest(

        @NotBlank(message = "es obligatorio")
        @Email(message = "debe tener formato de email")
        String email,

        @NotBlank(message = "es obligatoria")
        @Size(min = 6, message = "debe tener al menos 6 caracteres")
        String password,

        @NotBlank(message = "es obligatorio")
        @Size(min = 3, max = 60, message = "debe tener entre 3 y 60 caracteres")
        String displayName) {
}
