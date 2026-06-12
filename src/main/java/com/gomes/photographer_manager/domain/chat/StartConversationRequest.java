package com.gomes.photographer_manager.domain.chat;

import jakarta.validation.constraints.NotBlank;

public record StartConversationRequest(
        @NotBlank String otherUserId,
        String eventId
) {
}
