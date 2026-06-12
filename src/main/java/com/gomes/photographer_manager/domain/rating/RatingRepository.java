package com.gomes.photographer_manager.domain.rating;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, String> {

    boolean existsByPhotographerIdAndClientId(String photographerId, String clientId);

    Optional<Rating> findByPhotographerIdAndClientId(String photographerId, String clientId);

    List<Rating> findByPhotographerId(String photographerId);

    long countByPhotographerId(String photographerId);

    @Query("SELECT COALESCE(AVG(r.stars), 0.0) FROM Rating r WHERE r.photographer.id = :photographerId")
    double findAverageByPhotographerId(String photographerId);
}
