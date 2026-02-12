package com.globo.wallet.adapter.http.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.globo.wallet.core.domain.enums.TransactionType;

public record TransactionResponse(
    UUID id,
    UUID walletId,
    BigDecimal amount,
    TransactionType type,
    String description,
    LocalDateTime createdAt,
    BigDecimal newBalance
) {}
