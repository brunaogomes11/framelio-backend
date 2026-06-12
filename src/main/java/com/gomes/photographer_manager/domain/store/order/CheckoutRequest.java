package com.gomes.photographer_manager.domain.store.order;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CheckoutRequest(
        @NotBlank String buyerName,
        @NotBlank @Email String buyerEmail,
        String buyerCpf,
        @NotNull String orderType,   // INDIVIDUAL | FULL_ALBUM
        List<String> photoIds,       // required if INDIVIDUAL
        @NotBlank String paymentMethod, // PIX | CARD
        String cardToken,
        String paymentMethodId,
        String paymentType,
        Integer installments
) {}
