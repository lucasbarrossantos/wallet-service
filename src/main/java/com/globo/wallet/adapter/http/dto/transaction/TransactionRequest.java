package com.globo.wallet.adapter.http.dto.transaction;

import java.math.BigDecimal;

import com.globo.wallet.core.domain.enums.TransactionType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record TransactionRequest(
    @NotNull(message = "{transaction_type_required}")
    TransactionType type,
    @NotNull(message = "{amount_required}")
    @DecimalMin(value = "0.01", message = "{min_amount_error}")
    BigDecimal amount,
    String description
) {}
