package com.gomes.photographer_manager.domain.gallery;

import com.github.f4b6a3.ulid.UlidCreator;
import com.gomes.photographer_manager.config.storage.StorageService;
import com.gomes.photographer_manager.domain.event.Event;
import com.gomes.photographer_manager.domain.event.EventRepository;
import com.gomes.photographer_manager.domain.usuario.User;
import com.gomes.photographer_manager.domain.usuario.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GalleryService {

    private final GalleryRepository galleryRepository;
    private final PhotoRepository photoRepository;
    private final EventRepository eventRepository;
    private final StorageService storageService;
    private final UserRepository userRepository;

    public GalleryService(GalleryRepository galleryRepository,
                          PhotoRepository photoRepository,
                          EventRepository eventRepository,
                          StorageService storageService,
                          UserRepository userRepository) {
        this.galleryRepository = galleryRepository;
        this.photoRepository = photoRepository;
        this.eventRepository = eventRepository;
        this.storageService = storageService;
        this.userRepository = userRepository;
    }

    @Transactional
    public GalleryResponse create(GalleryRequest request, String photographerId) {
        Gallery gallery = new Gallery(
                photographerId,
                request.eventId(),
                request.title(),
                request.description(),
                request.visible() != null ? request.visible() : true
        );
        return new GalleryResponse(galleryRepository.save(gallery));
    }

    @Transactional(readOnly = true)
    public List<GalleryResponse> findByPhotographer(String photographerId) {
        return galleryRepository.findByPhotographerIdOrderByCreatedAtDesc(photographerId).stream()
                .map(this::enrich)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GalleryResponse> findVisibleByClient(String clientId) {
        List<String> eventIds = eventRepository.findByClientId(clientId).stream()
                .map(Event::getId)
                .toList();
        if (eventIds.isEmpty()) {
            return List.of();
        }
        return galleryRepository.findByEventIdInAndVisibleTrueOrderByCreatedAtDesc(eventIds).stream()
                .map(this::enrich)
                .toList();
    }

    private GalleryResponse enrich(Gallery gallery) {
        long photoCount = photoRepository.countByGalleryId(gallery.getId());

        String coverUrl = photoRepository.findFirstByGalleryIdOrderByDisplayOrderAsc(gallery.getId())
                .map(photo -> storageService.getUrl(photo.getStoragePath()))
                .orElse(null);

        String eventTitle = null;
        if (gallery.getEventId() != null) {
            eventTitle = eventRepository.findById(gallery.getEventId())
                    .map(Event::getTitle)
                    .orElse(null);
        }

        return new GalleryResponse(gallery, photoCount, coverUrl, eventTitle);
    }

    @Transactional(readOnly = true)
    public List<GalleryResponse> findByEvent(String eventId) {
        return galleryRepository.findByEventIdOrderByCreatedAtDesc(eventId).stream()
                .map(GalleryResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public GalleryResponse findById(String id) {
        return new GalleryResponse(getEntity(id));
    }

    @Transactional
    public GalleryResponse update(String id, GalleryRequest request, String photographerId) {
        Gallery gallery = getOwnedEntity(id, photographerId);
        gallery.setTitle(request.title());
        gallery.setDescription(request.description());
        gallery.setEventId(request.eventId());
        gallery.setVisible(request.visible());
        return new GalleryResponse(galleryRepository.save(gallery));
    }

    @Transactional
    public GalleryResponse toggleVisibility(String id, String photographerId) {
        Gallery gallery = getOwnedEntity(id, photographerId);
        gallery.setVisible(!gallery.isVisible());
        return new GalleryResponse(galleryRepository.save(gallery));
    }

    @Transactional
    public GalleryResponse togglePortfolio(String id, String photographerId) {
        Gallery gallery = getOwnedEntity(id, photographerId);
        gallery.setPortfolio(!gallery.isPortfolio());
        return new GalleryResponse(galleryRepository.save(gallery));
    }

    @Transactional
    public GalleryResponse toggleShare(String id, boolean shareEnabled, String photographerId) {
        Gallery gallery = getOwnedEntity(id, photographerId);
        gallery.setShareEnabled(shareEnabled);
        if (shareEnabled && gallery.getShareToken() == null) {
            gallery.setShareToken(UlidCreator.getUlid().toString());
        }
        return new GalleryResponse(galleryRepository.save(gallery));
    }

    @Transactional(readOnly = true)
    public GalleryShareResponse findByShareToken(String shareToken) {
        Gallery gallery = galleryRepository.findByShareTokenAndShareEnabledTrue(shareToken)
                .orElseThrow(() -> new EntityNotFoundException("Galeria compartilhada não encontrada"));

        User photographer = userRepository.findById(gallery.getPhotographerId())
                .orElseThrow(() -> new EntityNotFoundException("Fotógrafo não encontrado"));

        List<GalleryShareResponse.SharedPhoto> photos =
                photoRepository.findByGalleryIdOrderByDisplayOrderAsc(gallery.getId()).stream()
                        .map(photo -> new GalleryShareResponse.SharedPhoto(
                                photo.getId(),
                                "/photos/" + photo.getId() + "/view",
                                photo.getCaption()))
                        .toList();

        return new GalleryShareResponse(
                gallery.getId(),
                gallery.getTitle(),
                gallery.getDescription(),
                photographer.getName(),
                photographer.getProfilePhoto(),
                photos);
    }

    @Transactional
    public void delete(String id, String photographerId) {
        Gallery gallery = getOwnedEntity(id, photographerId);
        List<Photo> photos = photoRepository.findByGalleryIdOrderByDisplayOrderAsc(gallery.getId());
        for (Photo photo : photos) {
            storageService.delete(photo.getStoragePath());
        }
        photoRepository.deleteByGalleryId(gallery.getId());
        galleryRepository.delete(gallery);
    }

    @Transactional(readOnly = true)
    public Gallery getEntity(String id) {
        return galleryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Galeria não encontrada para ID: " + id));
    }

    private Gallery getOwnedEntity(String id, String photographerId) {
        Gallery gallery = getEntity(id);
        if (!gallery.getPhotographerId().equals(photographerId)) {
            throw new IllegalArgumentException("Galeria não pertence ao fotógrafo autenticado");
        }
        return gallery;
    }
}
