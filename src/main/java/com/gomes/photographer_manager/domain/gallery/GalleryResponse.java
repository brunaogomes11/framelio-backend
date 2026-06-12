package com.gomes.photographer_manager.domain.gallery;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GalleryResponse(
    String id,
    String eventId,
    String eventTitle,
    String photographerId,
    String title,
    String description,
    boolean visible,
    LocalDateTime createdAt,
    long photoCount,
    String coverUrl,
    String shareToken,
    BigDecimal pricePerPhoto,
    BigDecimal priceFullAlbum,
    boolean storeEnabled,
    boolean portfolio,
    boolean shareEnabled
) {
    public GalleryResponse(Gallery gallery, long photoCount, String coverUrl, String eventTitle) {
        this(gallery.getId(), gallery.getEventId(), eventTitle, gallery.getPhotographerId(),
             gallery.getTitle(), gallery.getDescription(), gallery.isVisible(),
             gallery.getCreatedAt(), photoCount, coverUrl,
             gallery.getShareToken(), gallery.getPricePerPhoto(),
             gallery.getPriceFullAlbum(), gallery.isStoreEnabled(), gallery.isPortfolio(),
             gallery.isShareEnabled());
    }

    public GalleryResponse(Gallery gallery) {
        this(gallery, 0L, null, null);
    }
}
