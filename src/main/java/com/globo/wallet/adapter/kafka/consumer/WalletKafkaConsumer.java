package com.globo.wallet.adapter.kafka.consumer;

import com.globo.wallet.adapter.kafka.dto.CreditRefundEventDTO;
import com.globo.wallet.adapter.kafka.dto.DebitAmountEventDTO;
import com.globo.wallet.adapter.kafka.dto.DebitSubscriptionPlanEventDTO;
import com.globo.wallet.core.usecase.transaction.ProcessKafkaTransactionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WalletKafkaConsumer {

    private final ProcessKafkaTransactionUseCase processKafkaTransactionUseCase;
    private static final String TRACE_ID_HEADER = "traceId";

    @KafkaListener(
            topics = "${spring.kafka.topics.debitSubscriptionPlan}",
            groupId = "wallet-group",
            containerFactory = "debitSubscriptionPlanKafkaListenerContainerFactory")
    public void processDebitPlan(DebitSubscriptionPlanEventDTO event, @Header(name = TRACE_ID_HEADER, required = false) String traceId) {
        log.info("Received debit subscription plan event from Kafka - userId: {}, plan: {}, subscriptionId: {}, traceId: {}", event.getUserId(), event.getPlan(), event.getSubscriptionId(), traceId);
        setTraceId(traceId);
        processKafkaTransactionUseCase.processDebitSubscriptionPlan(event);
        clearTraceId();
    }

    @KafkaListener(
            topics = "${spring.kafka.topics.debitAmount}",
            groupId = "wallet-group",
            containerFactory = "debitAmountKafkaListenerContainerFactory")
    public void processDebitAmount(DebitAmountEventDTO event, @Header(name = TRACE_ID_HEADER, required = false) String traceId) {
        log.info("Received debit amount event from Kafka - userId: {}, amount: {}, traceId: {}", event.getUserId(), event.getAmount(), traceId);
        setTraceId(traceId);
        processKafkaTransactionUseCase.processDebitAmount(event);
        clearTraceId();
    }

    @KafkaListener(
            topics = "${spring.kafka.topics.creditRefund}",
            groupId = "wallet-group",
            containerFactory = "creditRefundKafkaListenerContainerFactory")
    public void processCreditRefund(CreditRefundEventDTO event, @Header(name = TRACE_ID_HEADER, required = false) String traceId) {
        log.info("Received credit refund event from Kafka - userId: {}, amount: {}, traceId: {}", event.getUserId(), event.getAmount(), traceId);
        setTraceId(traceId);
        processKafkaTransactionUseCase.processCreditRefund(event);
        clearTraceId();
    }

    private void setTraceId(String traceId) {
        if (traceId != null) {
            MDC.put(TRACE_ID_HEADER, traceId);
        }
    }

    private void clearTraceId() {
        MDC.remove(TRACE_ID_HEADER);
    }
}
