package com.gomes.photographer_manager.domain.rating.response;

import com.gomes.photographer_manager.domain.rating.Rating;

import java.time.LocalDateTime;

public record RatingResponse(
        String id,
        String clientName,
        int stars,
        String comment,
        LocalDateTime createdAt
) {
    public RatingResponse(Rating r) {
        this(r.getId(), r.getClient().getName(), r.getStars(), r.getComment(), r.getCreatedAt());
    }
}
