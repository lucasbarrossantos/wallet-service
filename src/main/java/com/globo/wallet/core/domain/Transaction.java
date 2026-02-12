package com.globo.wallet.core.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.globo.wallet.core.domain.enums.TransactionType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Transaction {
    private UUID id;
    private UUID walletId;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private LocalDateTime createdAt;
}
