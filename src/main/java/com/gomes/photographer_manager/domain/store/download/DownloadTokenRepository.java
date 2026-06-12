package com.gomes.photographer_manager.domain.store.download;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DownloadTokenRepository extends JpaRepository<DownloadToken, String> {
    Optional<DownloadToken> findByToken(String token);
    Optional<DownloadToken> findByOrderId(String orderId);
}
