package com.gomes.photographer_manager.domain.rating;

import com.gomes.photographer_manager.domain.rating.request.RatingRequest;
import com.gomes.photographer_manager.domain.rating.response.RatingResponse;
import com.gomes.photographer_manager.domain.rating.response.RatingStatsResponse;
import com.gomes.photographer_manager.domain.usuario.User;
import com.gomes.photographer_manager.domain.usuario.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;

    public RatingService(RatingRepository ratingRepository, UserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public RatingResponse create(String photographerId, String clientEmail, RatingRequest request) {
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
        if (client.getId().equals(photographerId)) {
            throw new IllegalArgumentException("Fotógrafo não pode avaliar a si mesmo.");
        }
        if (ratingRepository.existsByPhotographerIdAndClientId(photographerId, client.getId())) {
            throw new IllegalStateException("Você já avaliou este fotógrafo.");
        }
        User photographer = userRepository.findById(photographerId)
                .orElseThrow(() -> new EntityNotFoundException("Fotógrafo não encontrado"));

        Rating rating = new Rating();
        rating.setPhotographer(photographer);
        rating.setClient(client);
        rating.setStars(request.stars());
        rating.setComment(request.comment());
        return new RatingResponse(ratingRepository.save(rating));
    }

    @Transactional
    public RatingResponse update(String ratingId, String clientEmail, RatingRequest request) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new EntityNotFoundException("Avaliação não encontrada"));
        if (!rating.getClient().getUsername().equals(clientEmail)) {
            throw new IllegalArgumentException("Você não pode editar a avaliação de outro usuário.");
        }
        rating.setStars(request.stars());
        rating.setComment(request.comment());
        return new RatingResponse(ratingRepository.save(rating));
    }

    public List<RatingResponse> listByPhotographer(String photographerId) {
        return ratingRepository.findByPhotographerId(photographerId)
                .stream().map(RatingResponse::new).toList();
    }

    public RatingStatsResponse statsForPhotographer(String photographerId) {
        return new RatingStatsResponse(
                ratingRepository.findAverageByPhotographerId(photographerId),
                ratingRepository.countByPhotographerId(photographerId)
        );
    }
}
