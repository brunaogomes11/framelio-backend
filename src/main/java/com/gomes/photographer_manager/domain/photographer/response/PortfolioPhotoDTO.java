package com.gomes.photographer_manager.domain.photographer.response;

public record PortfolioPhotoDTO(
        String photoId,
        String url,
        String galleryId,
        String galleryTitle,
        String eventTitle,
        String eventDate,
        int portfolioOrder,
        String caption
) {}
