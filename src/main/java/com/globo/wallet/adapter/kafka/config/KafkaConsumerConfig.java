package com.globo.wallet.adapter.kafka.config;

import com.globo.wallet.adapter.kafka.dto.CreditRefundEventDTO;
import com.globo.wallet.adapter.kafka.dto.DebitAmountEventDTO;
import com.globo.wallet.adapter.kafka.dto.DebitSubscriptionPlanEventDTO;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    private Map<String, Object> baseConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "${spring.kafka.consumer.group-id}");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class.getName());
        return props;
    }

    @Bean
    public ConsumerFactory<String, DebitSubscriptionPlanEventDTO> debitSubscriptionPlanConsumerFactory() {
        Map<String, Object> props = baseConfigs();
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JacksonDebitSubscriptionPlanEventDTODeserializer.class.getName());
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new ErrorHandlingDeserializer<>(new JacksonDebitSubscriptionPlanEventDTODeserializer()));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DebitSubscriptionPlanEventDTO> debitSubscriptionPlanKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, DebitSubscriptionPlanEventDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(debitSubscriptionPlanConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, DebitAmountEventDTO> debitAmountConsumerFactory() {
        Map<String, Object> props = baseConfigs();
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JacksonDebitAmountEventDTODeserializer.class.getName());
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new ErrorHandlingDeserializer<>(new JacksonDebitAmountEventDTODeserializer()));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DebitAmountEventDTO> debitAmountKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, DebitAmountEventDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(debitAmountConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, CreditRefundEventDTO> creditRefundConsumerFactory() {
        Map<String, Object> props = baseConfigs();
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JacksonCreditRefundEventDTODeserializer.class.getName());
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new ErrorHandlingDeserializer<>(new JacksonCreditRefundEventDTODeserializer()));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CreditRefundEventDTO> creditRefundKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CreditRefundEventDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(creditRefundConsumerFactory());
        return factory;
    }
}
