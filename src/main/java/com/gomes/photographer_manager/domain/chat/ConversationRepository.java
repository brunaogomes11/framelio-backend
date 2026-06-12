package com.gomes.photographer_manager.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, String> {
    List<Conversation> findByParticipant1IdOrParticipant2IdOrderByCreatedAtDesc(String participant1Id, String participant2Id);
}
