package com.globo.wallet.adapter.kafka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.globo.wallet.adapter.kafka.dto.CreditRefundEventDTO;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class JacksonCreditRefundEventDTODeserializer implements Deserializer<CreditRefundEventDTO> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {}

    @Override
    public CreditRefundEventDTO deserialize(String topic, byte[] data) {
        try {
            if (data == null) return null;
            return objectMapper.readValue(data, CreditRefundEventDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize CreditRefundEventDTO", e);
        }
    }

    @Override
    public void close() {}
}
