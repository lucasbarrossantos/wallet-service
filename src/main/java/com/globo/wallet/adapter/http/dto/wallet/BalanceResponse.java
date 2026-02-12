package com.globo.wallet.adapter.http.dto.wallet;

import java.math.BigDecimal;
import java.util.UUID;

public record BalanceResponse(
    UUID id,
    UUID userId,
    BigDecimal balance
) {}
