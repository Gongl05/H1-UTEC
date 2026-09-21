package com.tuckersoft.branchengine.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NodeRequest(
        @NotBlank(message = "El nodeCode es obligatorio")
        @Size(min = 3, max = 40, message = "El nodeCode debe tener entre 3 y 40 caracteres")
        String nodeCode,

        @NotBlank(message = "El título es obligatorio")
        @Size(min = 3, max = 80, message = "El título debe tener entre 3 y 80 caracteres")
        String title,

        @NotBlank(message = "El sceneText es obligatorio")
        @Size(min = 10, message = "El sceneText debe tener al menos 10 caracteres")
        String sceneText,

        @NotNull(message = "La branchCapacity es obligatoria")
        @Min(value = 1, message = "La branchCapacity debe ser mayor a 0")
        Integer branchCapacity,

        String primaryBranchCode,
        String glitchBranchCode
) {}
