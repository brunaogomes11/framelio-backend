package com.gomes.photographer_manager.domain.sale.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Dados para criação ou atualização de uma venda")
public record SaleRequest(

        @Schema(description = "ID ULID do evento vinculado à venda")
        String eventId,

        @Schema(description = "ID ULID do cliente vinculado à venda")
        String clientId,

        @NotNull
        @DecimalMin("0.01")
        @Schema(description = "Valor monetário da venda")
        BigDecimal amount,

        @Schema(description = "Status do pagamento (PENDING, PAID, OVERDUE)")
        String paymentStatus,

        @Schema(description = "Data do pagamento")
        LocalDate paymentDate,

        @Size(max = 500)
        @Schema(description = "Descrição da venda")
        String description,

        @Schema(description = "Número da fatura")
        String invoiceNumber
) {
}
