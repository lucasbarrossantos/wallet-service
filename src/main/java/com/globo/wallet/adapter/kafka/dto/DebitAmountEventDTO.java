package com.globo.wallet.adapter.kafka.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@ToString
@Getter
@Setter
public class DebitAmountEventDTO {
    private UUID userId;
    private Double amount;
    private String description;
}
