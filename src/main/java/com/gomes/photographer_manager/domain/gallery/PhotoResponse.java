package com.gomes.photographer_manager.domain.gallery;

import java.time.LocalDateTime;

public record PhotoResponse(
        String id,
        String galleryId,
        String url,
        String caption,
        int displayOrder,
        boolean portfolio,
        int portfolioOrder,
        LocalDateTime createdAt
) {
    public PhotoResponse(Photo photo, String url) {
        this(
                photo.getId(),
                photo.getGalleryId(),
                url,
                photo.getCaption(),
                photo.getDisplayOrder(),
                photo.isPortfolio(),
                photo.getPortfolioOrder(),
                photo.getCreatedAt()
        );
    }
}
