package com.gomes.photographer_manager.domain.gallery;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GalleryRepository extends JpaRepository<Gallery, String> {
    List<Gallery> findByPhotographerIdOrderByCreatedAtDesc(String photographerId);
    List<Gallery> findByEventIdOrderByCreatedAtDesc(String eventId);
    List<Gallery> findByPhotographerIdAndVisibleTrueOrderByCreatedAtDesc(String photographerId);
    List<Gallery> findByPhotographerIdAndPortfolioTrueOrderByCreatedAtDesc(String photographerId);
    List<Gallery> findByEventIdInAndVisibleTrueOrderByCreatedAtDesc(List<String> eventIds);
    Optional<Gallery> findByShareTokenAndStoreEnabledTrue(String shareToken);
    Optional<Gallery> findByShareTokenAndShareEnabledTrue(String shareToken);
}
