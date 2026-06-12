package com.gomes.photographer_manager.domain.store.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
    String id,
    String galleryId,
    String photographerId,
    String buyerName,
    String buyerEmail,
    String orderType,
    BigDecimal subtotal,
    BigDecimal photographerAmount,
    String status,
    LocalDateTime createdAt,
    LocalDateTime paidAt
) {
    public OrderResponse(Order o) {
        this(o.getId(), o.getGalleryId(), o.getPhotographerId(),
             o.getBuyerName(), o.getBuyerEmail(), o.getOrderType(),
             o.getSubtotal(), o.getPhotographerAmount(),
             o.getStatus(), o.getCreatedAt(), o.getPaidAt());
    }
}
