package com.tuckersoft.branchengine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlaythroughRequest(
        @NotBlank(message = "El playerTag es obligatorio")
        @Size(min = 2, max = 40, message = "El playerTag debe tener entre 2 y 40 caracteres")
        String playerTag,

        @NotBlank(message = "El startNodeCode es obligatorio")
        String startNodeCode
) {}
