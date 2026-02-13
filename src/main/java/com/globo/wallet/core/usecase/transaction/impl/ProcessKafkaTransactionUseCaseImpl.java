package com.globo.wallet.core.usecase.transaction.impl;

import com.globo.wallet.adapter.kafka.dto.CreditRefundEventDTO;
import com.globo.wallet.adapter.kafka.dto.DebitAmountEventDTO;
import com.globo.wallet.adapter.kafka.dto.DebitSubscriptionPlanEventDTO;
import com.globo.wallet.core.domain.Wallet;
import com.globo.wallet.core.domain.enums.TransactionType;
import com.globo.wallet.core.exception.BusinessException;
import com.globo.wallet.core.exception.InsufficientBalanceException;
import com.globo.wallet.core.exception.WalletNotFoundException;
import com.globo.wallet.core.port.in.WalletQueryPort;
import com.globo.wallet.core.port.in.WalletTransactionPort;
import com.globo.wallet.core.usecase.transaction.ProcessKafkaTransactionUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class ProcessKafkaTransactionUseCaseImpl implements ProcessKafkaTransactionUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessKafkaTransactionUseCaseImpl.class);

    private final WalletQueryPort walletQueryPort;
    private final WalletTransactionPort walletTransactionPort;

    public ProcessKafkaTransactionUseCaseImpl(WalletQueryPort walletQueryPort, WalletTransactionPort walletTransactionPort) {
        this.walletQueryPort = walletQueryPort;
        this.walletTransactionPort = walletTransactionPort;
    }

    @Override
    @Transactional
    public void processDebitSubscriptionPlan(DebitSubscriptionPlanEventDTO event) {
        log.info("Processing debit subscription plan event: {}", event);

        Wallet wallet = getWalletOrThrow(event.getUserId());
        BigDecimal amount = getPlanAmount(event.getPlan());

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente para o plano: " + event.getPlan());
        }

        walletTransactionPort.debit(wallet, amount, event.getDescription(), TransactionType.DEBIT);
    }

    @Override
    @Transactional
    public void processDebitAmount(DebitAmountEventDTO event) {
        log.info("Processing debit amount event: {}", event);

        Wallet wallet = getWalletOrThrow(event.getUserId());
        BigDecimal amount = BigDecimal.valueOf(event.getAmount());

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente para débito: " + event.getAmount());
        }

        walletTransactionPort.debit(wallet, amount, event.getDescription(), TransactionType.DEBIT);
    }

    @Override
    @Transactional
    public void processCreditRefund(CreditRefundEventDTO event) {
        log.info("Processing credit refund event: {}", event);

        Wallet wallet = getWalletOrThrow(event.getUserId());
        BigDecimal amount = BigDecimal.valueOf(event.getAmount());
        walletTransactionPort.credit(wallet, amount, event.getDescription(), TransactionType.CREDIT);
    }

    private Wallet getWalletOrThrow(UUID userId) {

        Wallet wallet = walletQueryPort.findByUserId(userId);

        if (wallet == null) {
            throw new WalletNotFoundException("Carteira não encontrada para o usuário: " + userId);
        }

        return wallet;
    }

    private BigDecimal getPlanAmount(String plan) {
        return switch (plan) {
            case "BASIC" -> new BigDecimal("9.90");
            case "PREMIUM" -> new BigDecimal("19.90");
            case "FAMILY" -> new BigDecimal("29.90");
            default -> throw new BusinessException("Plano inválido: " + plan);
        };
    }
}