package com.gomes.photographer_manager.domain.store.withdrawal;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalRepository extends JpaRepository<WithdrawalRequest, String> {}
