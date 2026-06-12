package com.gomes.photographer_manager.domain.chat;

import jakarta.validation.constraints.NotBlank;

public record SendMessageRequest(
        @NotBlank String content
) {
}
