package com.gomes.photographer_manager.domain.chat;

import com.gomes.photographer_manager.domain.usuario.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public ChatService(ConversationRepository conversationRepository,
                       MessageRepository messageRepository,
                       UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ConversationResponse startConversation(String currentUserId, String otherUserId, String eventId) {
        Conversation existing = conversationRepository
                .findByParticipant1IdOrParticipant2IdOrderByCreatedAtDesc(currentUserId, currentUserId).stream()
                .filter(conversation -> isBetween(conversation, currentUserId, otherUserId))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            return toResponse(existing, currentUserId);
        }

        Conversation conversation = conversationRepository.save(
                new Conversation(currentUserId, otherUserId, eventId));
        return toResponse(conversation, currentUserId);
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> findMyConversations(String userId) {
        return conversationRepository
                .findByParticipant1IdOrParticipant2IdOrderByCreatedAtDesc(userId, userId).stream()
                .map(conversation -> toResponse(conversation, userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> findMessages(String conversationId, int page, int size, String currentUserId) {
        getConversation(conversationId);
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId, pageable).stream()
                .map(message -> new MessageResponse(
                        message,
                        resolveName(message.getSenderId()),
                        message.getSenderId().equals(currentUserId)))
                .toList();
    }

    @Transactional
    public MessageResponse sendMessage(String conversationId, String content, String senderId) {
        Conversation conversation = getConversation(conversationId);
        if (!isParticipant(conversation, senderId)) {
            throw new IllegalArgumentException("Remetente não participa desta conversa");
        }
        Message message = messageRepository.save(new Message(conversationId, senderId, content));
        return new MessageResponse(message, resolveName(senderId), true);
    }

    @Transactional
    public void markAsRead(String messageId, String readerId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Mensagem não encontrada para ID: " + messageId));
        Conversation conversation = getConversation(message.getConversationId());
        if (!isParticipant(conversation, readerId)) {
            throw new IllegalArgumentException("Leitor não participa desta conversa");
        }
        message.setRead(true);
        messageRepository.save(message);
    }

    private Conversation getConversation(String conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversa não encontrada para ID: " + conversationId));
    }

    private ConversationResponse toResponse(Conversation conversation, String userId) {
        int unreadCount = messageRepository
                .countByConversationIdAndReadFalseAndSenderIdNot(conversation.getId(), userId);

        String otherParticipantId = conversation.getParticipant1Id().equals(userId)
                ? conversation.getParticipant2Id()
                : conversation.getParticipant1Id();
        String name = resolveName(otherParticipantId);

        Message latest = messageRepository
                .findTopByConversationIdOrderBySentAtDesc(conversation.getId())
                .orElse(null);
        String lastMessage = latest != null ? latest.getContent() : null;
        LocalDateTime lastMessageAt = latest != null ? latest.getSentAt() : null;

        return new ConversationResponse(conversation, unreadCount, name, lastMessage, lastMessageAt);
    }

    private String resolveName(String userId) {
        return userRepository.findById(userId)
                .map(user -> user.getName())
                .orElse("Usuário");
    }

    private boolean isParticipant(Conversation conversation, String userId) {
        return conversation.getParticipant1Id().equals(userId)
                || conversation.getParticipant2Id().equals(userId);
    }

    private boolean isBetween(Conversation conversation, String userA, String userB) {
        return isParticipant(conversation, userA) && isParticipant(conversation, userB);
    }
}
