package com.gomes.photographer_manager.domain.store;

import java.math.BigDecimal;

public record EnableStoreRequest(BigDecimal pricePerPhoto, BigDecimal priceFullAlbum) {}
