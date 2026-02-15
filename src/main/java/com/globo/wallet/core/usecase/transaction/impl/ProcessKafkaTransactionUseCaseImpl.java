package com.globo.wallet.core.usecase.transaction.impl;

import com.globo.wallet.adapter.integration.subscription.SubscriptionClient;
import com.globo.wallet.adapter.integration.subscription.dto.UpdateSubscriptionStatusRequest;
import com.globo.wallet.adapter.kafka.dto.CreditRefundEventDTO;
import com.globo.wallet.adapter.kafka.dto.DebitAmountEventDTO;
import com.globo.wallet.adapter.kafka.dto.DebitSubscriptionPlanEventDTO;
import com.globo.wallet.core.domain.Wallet;
import com.globo.wallet.core.domain.enums.TransactionType;
import com.globo.wallet.core.exception.BusinessException;
import com.globo.wallet.core.exception.InsufficientBalanceException;
import com.globo.wallet.core.exception.WalletNotFoundException;
import com.globo.wallet.core.port.in.GetWalletByUserIdPort;
import com.globo.wallet.core.port.in.WalletTransactionPort;
import com.globo.wallet.core.usecase.transaction.ProcessKafkaTransactionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static com.globo.wallet.adapter.kafka.dto.SubscriptionStatus.ACTIVE;
import static com.globo.wallet.adapter.kafka.dto.SubscriptionStatus.PAYMENT_FAILED;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessKafkaTransactionUseCaseImpl implements ProcessKafkaTransactionUseCase {

    private final GetWalletByUserIdPort getWalletByUserIdPort;
    private final WalletTransactionPort walletTransactionPort;
    private final SubscriptionClient subscriptionClient;

    @Override
    @Transactional
    public void processDebitSubscriptionPlan(DebitSubscriptionPlanEventDTO event) {
        log.info("Processing debit subscription plan event: {}", event);

        Wallet wallet = getWalletOrThrow(event.getUserId());
        BigDecimal amount = getPlanAmount(event.getPlan());
        String status = PAYMENT_FAILED.name();

        try {

            if (wallet.getBalance().compareTo(amount) < 0) {
                log.error("Insufficient balance for userId: {}, required: {}, available: {}", event.getUserId(), amount, wallet.getBalance());
                throw new InsufficientBalanceException("Saldo insuficiente para o plano: " + event.getPlan());
            }

            walletTransactionPort.debit(wallet, amount, event.getDescription(), TransactionType.DEBIT);

            if (event.getSubscriptionId() == null) {
                log.warn("Subscription ID is null for userId: {}, cannot update subscription status", event.getUserId());
                return;
            }

            status = ACTIVE.name();
        } finally {
            updateSubscriptionStatus(event.getSubscriptionId(), status, wallet, amount);
        }
    }

    @Override
    @Transactional
    public void processDebitAmount(DebitAmountEventDTO event) {
        log.info("Processing debit amount event: {}", event);

        Wallet wallet = getWalletOrThrow(event.getUserId());
        BigDecimal amount = BigDecimal.valueOf(event.getAmount());

        if (wallet.getBalance().compareTo(amount) < 0) {
            log.error("Saldo insuficiente para débito: {}. Saldo atual: {}", amount, wallet.getBalance());
            throw new InsufficientBalanceException("Saldo insuficiente para débito: " + event.getAmount());
        }

        walletTransactionPort.debit(wallet, amount, event.getDescription(), TransactionType.DEBIT);
        updateSubscriptionStatus(
                event.getSubscriptionId(),
                ACTIVE.name(),
                wallet,
                amount
        );
    }

    @Override
    @Transactional
    public void processCreditRefund(CreditRefundEventDTO event) {
        log.info("Processing credit refund event: {}", event);

        Wallet wallet = getWalletOrThrow(event.getUserId());
        BigDecimal amount = BigDecimal.valueOf(event.getAmount());
        walletTransactionPort.credit(wallet, amount, event.getDescription(), TransactionType.CREDIT);
        updateSubscriptionStatus(
                event.getSubscriptionId(),
                ACTIVE.name(),
                wallet,
                amount
        );
    }

    private Wallet getWalletOrThrow(UUID userId) {

        Wallet wallet = getWalletByUserIdPort.execute(userId);

        if (wallet == null) {
            log.error("Wallet not found for userId: {}", userId);
            throw new WalletNotFoundException("Carteira não encontrada para o usuário: " + userId);
        }

        return wallet;
    }

    private BigDecimal getPlanAmount(String plan) {
        return switch (plan) {
            case "BASIC" -> new BigDecimal("19.90");
            case "PREMIUM" -> new BigDecimal("39.90");
            case "FAMILY" -> new BigDecimal("59.90");
            default -> throw new BusinessException("Plano inválido: " + plan);
        };
    }

    private void updateSubscriptionStatus(
            UUID subscriptionId,
            String status,
            Wallet wallet,
            BigDecimal amount) {
        try {
            subscriptionClient.updateSubscriptionStatus(new UpdateSubscriptionStatusRequest(subscriptionId, status));
            log.info("Subscription status updated to {} for subscriptionId {}", status, subscriptionId);
        } catch (Exception ex) {
            log.error("Failed to update subscription status for subscriptionId {}: {}", subscriptionId, ex.getMessage());
            walletTransactionPort.credit(wallet, amount,
                    "Reembolso por falha na atualização do status da assinatura",
                    TransactionType.CREDIT);
        }
    }
}