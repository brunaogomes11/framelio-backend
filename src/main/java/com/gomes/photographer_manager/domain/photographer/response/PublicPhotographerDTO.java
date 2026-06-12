package com.gomes.photographer_manager.domain.photographer.response;

import java.util.List;

public record PublicPhotographerDTO(
        String photographerId,
        String name,
        String bio,
        String profilePhoto,
        List<String> categories,
        double averageRating,
        long reviewCount,
        String portfolioGradient
) {}
