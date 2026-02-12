package com.globo.wallet.adapter.http.dto.wallet;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record WalletResponse(
    UUID id,
    UUID userId,
    BigDecimal balance,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
