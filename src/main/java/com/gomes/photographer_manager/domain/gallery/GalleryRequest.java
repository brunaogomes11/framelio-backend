package com.gomes.photographer_manager.domain.gallery;

import jakarta.validation.constraints.NotBlank;

public record GalleryRequest(
        @NotBlank String title,
        String description,
        String eventId,
        Boolean visible
) {
}
