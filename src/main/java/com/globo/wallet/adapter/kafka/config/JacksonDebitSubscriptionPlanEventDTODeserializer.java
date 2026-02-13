package com.globo.wallet.adapter.kafka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.globo.wallet.adapter.kafka.dto.DebitSubscriptionPlanEventDTO;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class JacksonDebitSubscriptionPlanEventDTODeserializer implements Deserializer<DebitSubscriptionPlanEventDTO> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {}

    @Override
    public DebitSubscriptionPlanEventDTO deserialize(String topic, byte[] data) {
        try {
            if (data == null) return null;
            return objectMapper.readValue(data, DebitSubscriptionPlanEventDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize DebitSubscriptionPlanEventDTO", e);
        }
    }

    @Override
    public void close() {}
}
