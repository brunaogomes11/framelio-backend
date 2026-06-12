package com.gomes.photographer_manager.domain.event.response;

import com.gomes.photographer_manager.domain.event.Event;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Dados de retorno de um evento")
public record EventResponse(

        @Schema(description = "Identificador único ULID do evento")
        String id,

        @Schema(description = "Título do evento")
        String title,

        @Schema(description = "Data e hora do evento")
        LocalDateTime dateTime,

        @Schema(description = "Local do evento")
        String location,

        @Schema(description = "ID ULID do fotógrafo responsável pelo evento")
        String photographerId,

        @Schema(description = "ID ULID do cliente vinculado ao evento")
        String clientId,

        @Schema(description = "ID ULID da equipe vinculada ao evento")
        String teamId,

        @Schema(description = "Status atual do evento")
        String status,

        @Schema(description = "Valor monetário do evento")
        BigDecimal value,

        @Schema(description = "Observações adicionais sobre o evento")
        String notes
) {
    public EventResponse(Event event) {
        this(
                event.getId(),
                event.getTitle(),
                event.getDateTime(),
                event.getLocation(),
                event.getPhotographerId(),
                event.getClientId(),
                event.getTeamId(),
                event.getStatus().name(),
                event.getValue(),
                event.getNotes()
        );
    }
}
