package com.gomes.photographer_manager.domain.event.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Dados para criação ou atualização de um evento")
public record EventRequest(

        @NotBlank
        @Schema(description = "Título do evento", example = "Casamento João e Maria")
        String title,

        @NotNull
        @Schema(description = "Data e hora do evento", example = "2026-06-15T14:30:00")
        LocalDateTime dateTime,

        @NotBlank
        @Schema(description = "Local do evento", example = "Espaço de Eventos Jardins")
        String location,

        @Schema(description = "ID ULID do cliente vinculado ao evento")
        String clientId,

        @Schema(description = "ID ULID da equipe vinculada ao evento")
        String teamId,

        @Schema(description = "Status do evento. Quando não informado, assume SCHEDULED",
                allowableValues = {"SCHEDULED", "IN_PROGRESS", "COMPLETED", "CANCELLED"})
        String status,

        @DecimalMin("0.0")
        @Schema(description = "Valor monetário do evento", example = "2500.00")
        BigDecimal value,

        @Schema(description = "Observações adicionais sobre o evento")
        String notes
) {
}
