package com.globo.wallet.adapter.kafka.consumer;

import com.globo.wallet.adapter.kafka.dto.CreditRefundEventDTO;
import com.globo.wallet.adapter.kafka.dto.DebitAmountEventDTO;
import com.globo.wallet.adapter.kafka.dto.DebitSubscriptionPlanEventDTO;
import com.globo.wallet.core.usecase.transaction.ProcessKafkaTransactionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WalletKafkaConsumer {

    private final ProcessKafkaTransactionUseCase processKafkaTransactionUseCase;

    @KafkaListener(
            topics = "${spring.kafka.topics.debitSubscriptionPlan}",
            groupId = "wallet-group",
            containerFactory = "debitSubscriptionPlanKafkaListenerContainerFactory")
    public void processDebitPlan(DebitSubscriptionPlanEventDTO event) {
        processKafkaTransactionUseCase.processDebitSubscriptionPlan(event);
    }

    @KafkaListener(
            topics = "${spring.kafka.topics.debitAmount}",
            groupId = "wallet-group",
            containerFactory = "debitAmountKafkaListenerContainerFactory")
    public void processDebitAmount(DebitAmountEventDTO event) {
        processKafkaTransactionUseCase.processDebitAmount(event);
    }

    @KafkaListener(
            topics = "${spring.kafka.topics.creditRefund}",
            groupId = "wallet-group",
            containerFactory = "creditRefundKafkaListenerContainerFactory")
    public void processCreditRefund(CreditRefundEventDTO event) {
        processKafkaTransactionUseCase.processCreditRefund(event);
    }
}
