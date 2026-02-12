package com.globo.wallet.core.usecase.transaction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.globo.wallet.adapter.integration.subscription.SubscriptionClient;
import com.globo.wallet.core.domain.Transaction;
import com.globo.wallet.core.domain.Wallet;
import com.globo.wallet.core.domain.enums.TransactionType;
import com.globo.wallet.core.exception.InsufficientBalanceException;
import com.globo.wallet.core.exception.InvalidTransactionAmountException;
import com.globo.wallet.core.exception.UserNotFoundException;
import com.globo.wallet.core.exception.WalletNotFoundException;
import com.globo.wallet.core.port.in.transaction.ProcessTransactionPort;
import com.globo.wallet.core.port.out.transaction.TransactionRepositoryPort;
import com.globo.wallet.core.port.out.wallet.WalletRepositoryPort;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessTransactionUseCase implements ProcessTransactionPort {

    private final WalletRepositoryPort walletRepositoryPort;
    private final TransactionRepositoryPort transactionRepositoryPort;
    private final SubscriptionClient subscriptionClient;

    @Override
    @Transactional
    public Transaction execute(UUID userId, Transaction transaction) {
        log.info("Processando transação do tipo {} para o usuário: {}", transaction.getType(), userId);

        Wallet wallet = getValidetedWallet(userId, transaction);
        BigDecimal newBalance = processTransactionAndUpdateBalance(wallet, transaction);

        wallet.setBalance(newBalance);
        wallet.setUpdatedAt(OffsetDateTime.now());
        walletRepositoryPort.save(wallet);

        transaction.setWalletId(wallet.getId());
        transaction.setCreatedAt(OffsetDateTime.now());
        Transaction savedTransaction = transactionRepositoryPort.save(transaction);

        log.info("Transação processada com sucesso. Novo saldo: {}", newBalance);

        return savedTransaction;
    }

    private Wallet getValidetedWallet(UUID userId, Transaction transaction) {
        validateUser(userId);
        Wallet wallet = walletRepositoryPort.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Carteira não encontrada para o usuário: " + userId));
        validateTransactionAmount(transaction.getAmount());
        return wallet;
    }

    private void validateUser(UUID userId) {
        try {
            subscriptionClient.getUserById(userId);
            log.info("Usuário {} validado com sucesso", userId);
        } catch (FeignException.NotFound e) {
            log.error("Usuário não encontrado: {}", userId);
            throw new UserNotFoundException("Usuário não encontrado: " + userId);
        } catch (FeignException e) {
            log.error("Erro ao validar usuário: {}", e.getMessage());
            throw new UserNotFoundException("Erro ao validar usuário: " + userId);
        }
    }

    private void validateTransactionAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionAmountException("O valor da transação deve ser maior que zero");
        }
    }

    private BigDecimal processTransactionAndUpdateBalance(Wallet wallet, Transaction transaction) {
        BigDecimal currentBalance = wallet.getBalance();
        BigDecimal amount = transaction.getAmount();

        if (transaction.getType() == TransactionType.CREDIT) {
            return currentBalance.add(amount);
        } else if (transaction.getType() == TransactionType.DEBIT) {
            BigDecimal newBalance = currentBalance.subtract(amount);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new InsufficientBalanceException(
                        String.format("Saldo insuficiente. Saldo atual: R$ %.2f, Valor do débito: R$ %.2f",
                                currentBalance, amount));
            }
            return newBalance;
        } else {
            throw new InvalidTransactionAmountException("Tipo de transação inválido: " + transaction.getType());
        }
    }
}
