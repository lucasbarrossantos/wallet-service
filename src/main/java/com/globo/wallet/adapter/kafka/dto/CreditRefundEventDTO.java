package com.globo.wallet.adapter.kafka.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@ToString
@Getter
@Setter
public class CreditRefundEventDTO {
    private UUID userId;
    private BigDecimal amount;
    private String description;
    private UUID subscriptionId;
}
