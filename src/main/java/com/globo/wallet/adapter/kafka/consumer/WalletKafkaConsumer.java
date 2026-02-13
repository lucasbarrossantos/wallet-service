package com.globo.wallet.adapter.kafka.consumer;

import com.globo.wallet.adapter.kafka.dto.CreditRefundEventDTO;
import com.globo.wallet.adapter.kafka.dto.DebitAmountEventDTO;
import com.globo.wallet.adapter.kafka.dto.DebitSubscriptionPlanEventDTO;
import com.globo.wallet.core.usecase.transaction.ProcessKafkaTransactionUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class WalletKafkaConsumer {
    private final ProcessKafkaTransactionUseCase processKafkaTransactionUseCase;

    public WalletKafkaConsumer(ProcessKafkaTransactionUseCase processKafkaTransactionUseCase) {
        this.processKafkaTransactionUseCase = processKafkaTransactionUseCase;
    }

    @KafkaListener(topics = "payment-debit-subscription-plan", groupId = "wallet-group", containerFactory = "debitSubscriptionPlanKafkaListenerContainerFactory")
    public void processDebitPlan(DebitSubscriptionPlanEventDTO event) {
        processKafkaTransactionUseCase.processDebitSubscriptionPlan(event);
    }

    @KafkaListener(topics = "payment-debit-amount", groupId = "wallet-group", containerFactory = "debitAmountKafkaListenerContainerFactory")
    public void processDebitAmount(DebitAmountEventDTO event) {
        processKafkaTransactionUseCase.processDebitAmount(event);
    }

    @KafkaListener(topics = "payment-credit-refund", groupId = "wallet-group", containerFactory = "creditRefundKafkaListenerContainerFactory")
    public void processCreditRefund(CreditRefundEventDTO event) {
        processKafkaTransactionUseCase.processCreditRefund(event);
    }
}
