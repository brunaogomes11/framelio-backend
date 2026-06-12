package com.gomes.photographer_manager.domain.chat;

import java.time.LocalDateTime;

public record MessageResponse(
        String id,
        String conversationId,
        String senderId,
        String senderName,
        String content,
        boolean read,
        LocalDateTime sentAt,
        boolean mine
) {
    public MessageResponse(Message message, String senderName, boolean mine) {
        this(
                message.getId(),
                message.getConversationId(),
                message.getSenderId(),
                senderName,
                message.getContent(),
                message.isRead(),
                message.getSentAt(),
                mine
        );
    }

    public MessageResponse(Message message) {
        this(message, null, false);
    }
}
