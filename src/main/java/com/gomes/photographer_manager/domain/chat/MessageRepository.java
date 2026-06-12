package com.gomes.photographer_manager.domain.chat;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, String> {
    List<Message> findByConversationIdOrderBySentAtAsc(String conversationId, Pageable pageable);
    int countByConversationIdAndReadFalseAndSenderIdNot(String conversationId, String senderId);
    Optional<Message> findTopByConversationIdOrderBySentAtDesc(String conversationId);
    int countByConversationIdInAndReadFalseAndSenderIdNot(List<String> conversationIds, String senderId);
}
