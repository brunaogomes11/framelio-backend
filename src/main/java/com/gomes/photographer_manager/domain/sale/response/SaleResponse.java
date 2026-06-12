package com.gomes.photographer_manager.domain.sale.response;

import com.gomes.photographer_manager.domain.sale.Sale;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SaleResponse(

        @Schema(description = "Identificador único ULID da venda")
        String id,

        @Schema(description = "ID ULID do evento vinculado")
        String eventId,

        @Schema(description = "ID ULID do cliente vinculado")
        String clientId,

        @Schema(description = "ID ULID do fotógrafo responsável")
        String photographerId,

        @Schema(description = "Valor monetário da venda")
        BigDecimal amount,

        @Schema(description = "Status atual do pagamento")
        String paymentStatus,

        @Schema(description = "Data do pagamento")
        LocalDate paymentDate,

        @Schema(description = "Descrição da venda")
        String description,

        @Schema(description = "Número da fatura")
        String invoiceNumber,

        @Schema(description = "Data e hora de criação da venda")
        LocalDateTime createdAt
) {
    public SaleResponse(Sale sale) {
        this(
                sale.getId(),
                sale.getEventId(),
                sale.getClientId(),
                sale.getPhotographerId(),
                sale.getAmount(),
                sale.getPaymentStatus().name(),
                sale.getPaymentDate(),
                sale.getDescription(),
                sale.getInvoiceNumber(),
                sale.getCreatedAt()
        );
    }
}
