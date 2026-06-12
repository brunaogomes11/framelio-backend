package com.gomes.photographer_manager.domain.store.balance;

import java.math.BigDecimal;

public record BalanceResponse(BigDecimal availableAmount, BigDecimal pendingAmount, BigDecimal totalEarned) {}
