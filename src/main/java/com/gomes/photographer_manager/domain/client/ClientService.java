package com.gomes.photographer_manager.domain.client;

import com.gomes.photographer_manager.domain.chat.Conversation;
import com.gomes.photographer_manager.domain.chat.ConversationRepository;
import com.gomes.photographer_manager.domain.chat.MessageRepository;
import com.gomes.photographer_manager.domain.event.Event;
import com.gomes.photographer_manager.domain.event.EventRepository;
import com.gomes.photographer_manager.domain.event.EventStatus;
import com.gomes.photographer_manager.domain.gallery.Gallery;
import com.gomes.photographer_manager.domain.gallery.GalleryRepository;
import com.gomes.photographer_manager.domain.gallery.PhotoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientService {

    private final EventRepository eventRepository;
    private final GalleryRepository galleryRepository;
    private final PhotoRepository photoRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ClientService(EventRepository eventRepository,
                         GalleryRepository galleryRepository,
                         PhotoRepository photoRepository,
                         ConversationRepository conversationRepository,
                         MessageRepository messageRepository) {
        this.eventRepository = eventRepository;
        this.galleryRepository = galleryRepository;
        this.photoRepository = photoRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional(readOnly = true)
    public ClientStatsResponse getStats(String clientId) {
        int completedSessions = eventRepository.countByClientIdAndStatus(clientId, EventStatus.COMPLETED);
        int photosReceived = countPhotosReceived(clientId);
        int unreadMessages = countUnreadMessages(clientId);
        return new ClientStatsResponse(completedSessions, photosReceived, unreadMessages);
    }

    private int countPhotosReceived(String clientId) {
        List<String> eventIds = eventRepository.findByClientId(clientId).stream()
                .map(Event::getId)
                .toList();
        if (eventIds.isEmpty()) {
            return 0;
        }
        long total = galleryRepository.findByEventIdInAndVisibleTrueOrderByCreatedAtDesc(eventIds).stream()
                .map(Gallery::getId)
                .mapToLong(photoRepository::countByGalleryId)
                .sum();
        return (int) total;
    }

    private int countUnreadMessages(String clientId) {
        List<String> conversationIds = conversationRepository
                .findByParticipant1IdOrParticipant2IdOrderByCreatedAtDesc(clientId, clientId).stream()
                .map(Conversation::getId)
                .toList();
        if (conversationIds.isEmpty()) {
            return 0;
        }
        return messageRepository.countByConversationIdInAndReadFalseAndSenderIdNot(conversationIds, clientId);
    }
}
