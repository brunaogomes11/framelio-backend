package com.gomes.photographer_manager.domain.event.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Estatísticas agregadas de eventos de um fotógrafo")
public record EventStatsResponse(

        @Schema(description = "Total de eventos do fotógrafo")
        int totalEvents,

        @Schema(description = "Quantidade de eventos com status SCHEDULED")
        int scheduledCount,

        @Schema(description = "Quantidade de eventos com status COMPLETED")
        int completedCount,

        @Schema(description = "Quantidade de eventos com status CANCELLED")
        int cancelledCount,

        @Schema(description = "Receita esperada (soma dos valores de eventos SCHEDULED e IN_PROGRESS)")
        BigDecimal expectedRevenue
) {
}
