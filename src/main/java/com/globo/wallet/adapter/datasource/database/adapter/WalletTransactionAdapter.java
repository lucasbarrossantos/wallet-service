package com.globo.wallet.adapter.datasource.database.adapter;

import com.globo.wallet.core.domain.Wallet;
import com.globo.wallet.core.domain.Transaction;
import com.globo.wallet.core.domain.enums.TransactionType;
import com.globo.wallet.core.port.in.WalletTransactionPort;
import com.globo.wallet.core.port.out.transaction.TransactionRepositoryPort;
import com.globo.wallet.core.port.out.wallet.WalletRepositoryPort;
import com.globo.wallet.core.exception.InsufficientBalanceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class WalletTransactionAdapter implements WalletTransactionPort {

    private final WalletRepositoryPort walletRepositoryPort;
    private final TransactionRepositoryPort transactionRepositoryPort;

    @Override
    public void debit(Wallet wallet, BigDecimal amount, String description, TransactionType type) {

        if (wallet.getBalance().compareTo(amount) < 0) {
            log.error("Saldo insuficiente para débito - walletId: {}, required: {}, available: {}", wallet.getId(), amount, wallet.getBalance());
            throw new InsufficientBalanceException("Saldo insuficiente");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepositoryPort.save(wallet);
        Transaction transaction = new Transaction();
        transaction.setWalletId(wallet.getId());
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setDescription(description);
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepositoryPort.save(transaction);
    }

    @Override
    public void credit(Wallet wallet, BigDecimal amount, String description, TransactionType type) {

        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepositoryPort.save(wallet);
        Transaction transaction = new Transaction();
        transaction.setWalletId(wallet.getId());
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setDescription(description);
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepositoryPort.save(transaction);
    }
}
