package com.globo.wallet.adapter.integration.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSubscriptionStatusRequest {
    private UUID subscriptionId;
    private String status; // ACTIVE or PAYMENT_FAILED
}
