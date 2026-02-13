package com.globo.wallet.adapter.kafka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.globo.wallet.adapter.kafka.dto.DebitAmountEventDTO;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class JacksonDebitAmountEventDTODeserializer implements Deserializer<DebitAmountEventDTO> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {}

    @Override
    public DebitAmountEventDTO deserialize(String topic, byte[] data) {
        try {
            if (data == null) return null;
            return objectMapper.readValue(data, DebitAmountEventDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize DebitAmountEventDTO", e);
        }
    }

    @Override
    public void close() {}
}
