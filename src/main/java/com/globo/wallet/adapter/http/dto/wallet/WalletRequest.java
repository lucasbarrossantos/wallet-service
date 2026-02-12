package com.globo.wallet.adapter.http.dto.wallet;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record WalletRequest(
    @NotNull(message = "{user_id_required}")
    UUID userId,
    
    @NotNull(message = "{initial_balance_required}")
    @DecimalMin(value = "0.10", message = "{min_balance_error}")
    BigDecimal balance
) {}
