package com.gomes.photographer_manager.domain.chat;

import java.time.LocalDateTime;

public record ConversationResponse(
        String id,
        String participant1Id,
        String participant2Id,
        String eventId,
        LocalDateTime createdAt,
        int unreadCount,
        String name,
        String lastMessage,
        LocalDateTime lastMessageAt
) {
    public ConversationResponse(Conversation conversation, int unreadCount,
                                String name, String lastMessage, LocalDateTime lastMessageAt) {
        this(
                conversation.getId(),
                conversation.getParticipant1Id(),
                conversation.getParticipant2Id(),
                conversation.getEventId(),
                conversation.getCreatedAt(),
                unreadCount,
                name,
                lastMessage,
                lastMessageAt
        );
    }
}
