package com.gomes.photographer_manager.domain.sale.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Estatísticas financeiras das vendas do fotógrafo")
public record SaleStatsResponse(

        @Schema(description = "Soma do valor de todas as vendas")
        BigDecimal totalRevenue,

        @Schema(description = "Soma do valor das vendas pagas")
        BigDecimal paidRevenue,

        @Schema(description = "Soma do valor das vendas pendentes")
        BigDecimal pendingRevenue,

        @Schema(description = "Soma do valor das vendas vencidas")
        BigDecimal overdueRevenue,

        @Schema(description = "Quantidade total de vendas")
        int totalSales
) {
}
