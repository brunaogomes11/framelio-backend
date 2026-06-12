package com.gomes.photographer_manager.domain.store.balance;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BalanceRepository extends JpaRepository<PhotographerBalance, String> {
    Optional<PhotographerBalance> findByPhotographerId(String photographerId);
}
