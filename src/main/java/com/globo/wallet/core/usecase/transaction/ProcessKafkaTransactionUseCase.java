package com.globo.wallet.core.usecase.transaction;

import com.globo.wallet.adapter.kafka.dto.CreditRefundEventDTO;
import com.globo.wallet.adapter.kafka.dto.DebitAmountEventDTO;
import com.globo.wallet.adapter.kafka.dto.DebitSubscriptionPlanEventDTO;
import org.springframework.stereotype.Service;

@Service
public interface ProcessKafkaTransactionUseCase {
    void processDebitSubscriptionPlan(DebitSubscriptionPlanEventDTO event);
    void processDebitAmount(DebitAmountEventDTO event);
    void processCreditRefund(CreditRefundEventDTO event);
}
