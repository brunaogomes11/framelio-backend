package com.gomes.photographer_manager.domain.store.order;

public record CheckoutResponse(
        String orderId,
        String mercadoPagoOrderId,
        String paymentId,
        String status,
        String statusDetail,
        String paymentMethod,
        String ticketUrl,
        String qrCode,
        String qrCodeBase64,
        String downloadToken
) {}
