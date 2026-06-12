package com.gomes.photographer_manager.domain.photographer;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Estatísticas resumidas do painel do fotógrafo")
public record DashboardStatsResponse(

        @Schema(description = "Quantidade de eventos no mês atual (exceto cancelados)")
        int eventsThisMonth,

        @Schema(description = "Receita de vendas pagas no mês atual")
        BigDecimal revenueThisMonth,

        @Schema(description = "Quantidade de entregas pendentes (eventos agendados)")
        int pendingDeliveries,

        @Schema(description = "Quantidade de novos clientes no mês atual")
        int newClients
) {
}
