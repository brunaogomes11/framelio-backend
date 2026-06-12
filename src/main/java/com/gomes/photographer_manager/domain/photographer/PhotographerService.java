package com.gomes.photographer_manager.domain.photographer;

import com.gomes.photographer_manager.config.storage.StorageService;
import com.gomes.photographer_manager.domain.event.Event;
import com.gomes.photographer_manager.domain.event.EventRepository;
import com.gomes.photographer_manager.domain.gallery.Gallery;
import com.gomes.photographer_manager.domain.gallery.GalleryRepository;
import com.gomes.photographer_manager.domain.gallery.Photo;
import com.gomes.photographer_manager.domain.gallery.PhotoRepository;
import com.gomes.photographer_manager.domain.photographer.response.PhotographerPortfolioResponse;
import com.gomes.photographer_manager.domain.photographer.response.PortfolioPhotoDTO;
import com.gomes.photographer_manager.domain.photographer.response.PublicPhotographerDTO;
import com.gomes.photographer_manager.domain.rating.RatingRepository;
import com.gomes.photographer_manager.domain.rating.response.RatingResponse;
import com.gomes.photographer_manager.domain.usuario.User;
import com.gomes.photographer_manager.domain.usuario.UserRepository;
import com.gomes.photographer_manager.enums.ProfileEnum;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PhotographerService {

    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;
    private final GalleryRepository galleryRepository;
    private final PhotoRepository photoRepository;
    private final EventRepository eventRepository;
    private final StorageService storageService;

    public PhotographerService(UserRepository userRepository,
                               RatingRepository ratingRepository,
                               GalleryRepository galleryRepository,
                               PhotoRepository photoRepository,
                               EventRepository eventRepository,
                               StorageService storageService) {
        this.userRepository = userRepository;
        this.ratingRepository = ratingRepository;
        this.galleryRepository = galleryRepository;
        this.photoRepository = photoRepository;
        this.eventRepository = eventRepository;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public List<PublicPhotographerDTO> listPublic() {
        return userRepository.findByProfile(ProfileEnum.PHOTOGRAPHER)
                .stream()
                .map(u -> new PublicPhotographerDTO(
                        u.getId(),
                        u.getName(),
                        u.getBio(),
                        u.getProfilePhoto(),
                        u.getCategories(),
                        ratingRepository.findAverageByPhotographerId(u.getId()),
                        ratingRepository.countByPhotographerId(u.getId()),
                        u.getPortfolioGradient()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public PhotographerPortfolioResponse getPortfolio(String photographerId) {
        User photographer = userRepository.findById(photographerId)
                .orElseThrow(() -> new EntityNotFoundException("Fotógrafo não encontrado para ID: " + photographerId));

        double avgRating = ratingRepository.findAverageByPhotographerId(photographerId);
        long reviewCount = ratingRepository.countByPhotographerId(photographerId);

        PublicPhotographerDTO publicDTO = new PublicPhotographerDTO(
                photographer.getId(),
                photographer.getName(),
                photographer.getBio(),
                photographer.getProfilePhoto(),
                photographer.getCategories(),
                avgRating,
                reviewCount,
                photographer.getPortfolioGradient()
        );

        List<Photo> portfolioPhotos = photoRepository.findPortfolioByPhotographerId(photographerId);

        Set<String> galleryIds = portfolioPhotos.stream()
                .map(Photo::getGalleryId)
                .collect(Collectors.toSet());
        Map<String, Gallery> galleriesById = galleryRepository.findAllById(galleryIds)
                .stream()
                .collect(Collectors.toMap(Gallery::getId, gallery -> gallery));

        Set<String> eventIds = galleriesById.values().stream()
                .map(Gallery::getEventId)
                .filter(eventId -> eventId != null)
                .collect(Collectors.toSet());
        Map<String, Event> eventsById = eventIds.isEmpty()
                ? Collections.emptyMap()
                : eventRepository.findAllById(eventIds).stream()
                        .collect(Collectors.toMap(Event::getId, event -> event));

        List<PortfolioPhotoDTO> photos = portfolioPhotos.stream()
                .map(photo -> {
                    Gallery gallery = galleriesById.get(photo.getGalleryId());
                    String galleryTitle = gallery != null ? gallery.getTitle() : "";
                    String eventTitle = "";
                    String eventDate = "";
                    if (gallery != null && gallery.getEventId() != null) {
                        Event event = eventsById.get(gallery.getEventId());
                        if (event != null) {
                            eventTitle = event.getTitle();
                            eventDate = event.getDateTime() != null ? event.getDateTime().toString() : "";
                        }
                    }
                    return new PortfolioPhotoDTO(
                            photo.getId(),
                            storageService.getUrl(photo.getStoragePath()),
                            photo.getGalleryId(),
                            galleryTitle,
                            eventTitle,
                            eventDate,
                            photo.getPortfolioOrder(),
                            photo.getCaption()
                    );
                })
                .toList();

        List<RatingResponse> reviews = ratingRepository.findByPhotographerId(photographerId)
                .stream()
                .map(RatingResponse::new)
                .toList();

        return new PhotographerPortfolioResponse(publicDTO, photos, reviews);
    }
}
