package com.gomes.photographer_manager.domain.gallery.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PhotoOrderItem(
        @NotBlank String photoId,
        @NotNull Integer order
) {}
