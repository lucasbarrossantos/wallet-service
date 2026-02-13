package com.globo.wallet.adapter.datasource.database.adapter;

import com.globo.wallet.core.domain.Wallet;
import com.globo.wallet.core.domain.Transaction;
import com.globo.wallet.core.domain.enums.TransactionType;
import com.globo.wallet.core.port.in.WalletTransactionPort;
import com.globo.wallet.core.port.out.transaction.TransactionRepositoryPort;
import com.globo.wallet.core.port.out.wallet.WalletRepositoryPort;
import com.globo.wallet.core.exception.InsufficientBalanceException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class WalletTransactionAdapter implements WalletTransactionPort {
    private final WalletRepositoryPort walletRepositoryPort;
    private final TransactionRepositoryPort transactionRepositoryPort;

    public WalletTransactionAdapter(WalletRepositoryPort walletRepositoryPort, TransactionRepositoryPort transactionRepositoryPort) {
        this.walletRepositoryPort = walletRepositoryPort;
        this.transactionRepositoryPort = transactionRepositoryPort;
    }

    @Override
    public void debit(Wallet wallet, BigDecimal amount, String description, TransactionType type) {

        if (wallet.getBalance().compareTo(amount) < 0) {
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
