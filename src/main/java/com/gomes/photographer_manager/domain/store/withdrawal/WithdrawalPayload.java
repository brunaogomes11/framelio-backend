package com.gomes.photographer_manager.domain.store.withdrawal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record WithdrawalPayload(
    @NotNull @DecimalMin("20.00") BigDecimal amount,
    @NotBlank String pixKey,
    @NotBlank String pixKeyType
) {}
