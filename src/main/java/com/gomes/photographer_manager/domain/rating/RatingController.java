package com.gomes.photographer_manager.domain.rating;

import com.gomes.photographer_manager.domain.rating.request.RatingRequest;
import com.gomes.photographer_manager.domain.rating.response.RatingResponse;
import com.gomes.photographer_manager.domain.rating.response.RatingStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Avaliações")
@RestController
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @Operation(summary = "Avaliar fotógrafo (apenas CLIENT)")
    @PostMapping("/photographers/{photographerId}/ratings")
    public ResponseEntity<RatingResponse> create(
            @PathVariable String photographerId,
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody RatingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ratingService.create(photographerId, principal.getUsername(), request));
    }

    @Operation(summary = "Editar avaliação (somente autor)")
    @PutMapping("/ratings/{id}")
    public ResponseEntity<RatingResponse> update(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody RatingRequest request) {
        return ResponseEntity.ok(ratingService.update(id, principal.getUsername(), request));
    }

    @Operation(summary = "Listar avaliações de um fotógrafo")
    @GetMapping("/photographers/{photographerId}/ratings")
    public ResponseEntity<List<RatingResponse>> list(@PathVariable String photographerId) {
        return ResponseEntity.ok(ratingService.listByPhotographer(photographerId));
    }

    @Operation(summary = "Stats de avaliações de um fotógrafo")
    @GetMapping("/photographers/{photographerId}/ratings/stats")
    public ResponseEntity<RatingStatsResponse> stats(@PathVariable String photographerId) {
        return ResponseEntity.ok(ratingService.statsForPhotographer(photographerId));
    }
}
