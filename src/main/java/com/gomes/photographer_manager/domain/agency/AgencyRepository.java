package com.gomes.photographer_manager.domain.agency;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgencyRepository extends JpaRepository<Agency, String> {

    Optional<Agency> findByUserId(String userId);

    boolean existsByUserId(String userId);
}
