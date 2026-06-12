package com.gomes.photographer_manager.domain.store.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByPhotographerIdOrderByCreatedAtDesc(String photographerId);
    List<Order> findByGalleryIdOrderByCreatedAtDesc(String galleryId);
    Optional<Order> findByMpPaymentId(String mpPaymentId);
    Optional<Order> findByMpOrderId(String mpOrderId);
}
