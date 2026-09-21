package com.tuckersoft.branchengine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DecisionRequest(
        @NotNull(message = "El playthroughId es obligatorio")
        Long playthroughId,

        @NotNull(message = "El rawInput es obligatorio")
        @Size(min = 10, message = "El rawInput debe tener al menos 10 caracteres")
        String rawInput,

        @NotBlank(message = "El impactLevel es obligatorio")
        @Pattern(regexp = "LEVE|MODERADO|GRAVE|CRITICO", message = "El impactLevel debe ser LEVE, MODERADO, GRAVE o CRITICO")
        String impactLevel
) {}
