package com.gomes.photographer_manager.domain.photographer.response;

import com.gomes.photographer_manager.domain.rating.response.RatingResponse;

import java.util.List;

public record PhotographerPortfolioResponse(
        PublicPhotographerDTO photographer,
        List<PortfolioPhotoDTO> photos,
        List<RatingResponse> reviews
) {}
