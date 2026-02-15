package com.globo.wallet.adapter.integration.subscription;

import java.util.UUID;

import com.globo.wallet.adapter.integration.subscription.dto.UpdateSubscriptionStatusRequest;
import com.globo.wallet.adapter.integration.subscription.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "subscription-service", url = "${integrations.subscription-service.url}")
public interface SubscriptionClient {

    @GetMapping("/users/{userId}")
    UserResponse getUserById(@PathVariable("userId") UUID userId);

    @PutMapping("/subscriptions/status")
    void updateSubscriptionStatus(@RequestBody UpdateSubscriptionStatusRequest request);
}
