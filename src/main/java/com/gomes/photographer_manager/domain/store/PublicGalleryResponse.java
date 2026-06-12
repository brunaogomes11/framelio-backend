package com.gomes.photographer_manager.domain.store;

import java.math.BigDecimal;
import java.util.List;

public record PublicGalleryResponse(
    String id,
    String title,
    String description,
    BigDecimal pricePerPhoto,
    BigDecimal priceFullAlbum,
    List<PublicPhotoResponse> photos
) {}
