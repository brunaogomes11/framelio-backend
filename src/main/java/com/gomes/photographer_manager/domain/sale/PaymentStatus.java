package com.gomes.photographer_manager.domain.sale;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status de pagamento de uma venda")
public enum PaymentStatus {
    PENDING,
    PAID,
    OVERDUE
}
