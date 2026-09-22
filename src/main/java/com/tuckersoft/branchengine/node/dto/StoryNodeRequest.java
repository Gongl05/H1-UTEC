package com.tuckersoft.branchengine.node.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * currentBranches no aparece: lo fija el service en 0. Que el cliente pudiera
 * mandarlo seria una forma de saltarse la capacidad del nodo.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StoryNodeRequest(

        @NotBlank(message = "es obligatorio")
        @Size(min = 3, max = 40, message = "debe tener entre 3 y 40 caracteres")
        String nodeCode,

        @NotBlank(message = "es obligatorio")
        @Size(min = 3, max = 80, message = "debe tener entre 3 y 80 caracteres")
        String title,

        @NotBlank(message = "es obligatorio")
        @Size(min = 10, message = "debe tener al menos 10 caracteres")
        String sceneText,

        @NotNull(message = "es obligatoria")
        @Min(value = 1, message = "debe ser mayor a 0")
        Integer branchCapacity,

        String primaryBranchCode,

        String glitchBranchCode) {
}
