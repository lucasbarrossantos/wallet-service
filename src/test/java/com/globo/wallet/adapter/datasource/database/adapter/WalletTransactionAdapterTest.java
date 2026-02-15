package com.globo.wallet.adapter.datasource.database.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.globo.wallet.core.domain.Transaction;
import com.globo.wallet.core.domain.Wallet;
import com.globo.wallet.core.domain.enums.TransactionType;
import com.globo.wallet.core.exception.InsufficientBalanceException;
import com.globo.wallet.core.port.out.transaction.TransactionRepositoryPort;
import com.globo.wallet.core.port.out.wallet.WalletRepositoryPort;

import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WalletTransactionAdapterTest {

    @Mock
    private WalletRepositoryPort walletRepositoryPort;

    @Mock
    private TransactionRepositoryPort transactionRepositoryPort;

    @InjectMocks
    private WalletTransactionAdapter adapter;

    private Wallet wallet;
    private BigDecimal amount;
    private String description;

    @BeforeEach
    void setUp() {
        UUID walletId = UUID.randomUUID();
        wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(new BigDecimal("100.00"));
        amount = new BigDecimal("50.00");
        description = "Test transaction";
    }

    @Test
    void debit_shouldUpdateBalanceAndSave_whenSufficientBalance() {
        when(walletRepositoryPort.save(any(Wallet.class))).thenReturn(wallet);
        when(transactionRepositoryPort.save(any(Transaction.class))).thenReturn(new Transaction());

        adapter.debit(wallet, amount, description, TransactionType.DEBIT);

        assertThat(wallet.getBalance()).isEqualTo(new BigDecimal("50.00"));
        verify(walletRepositoryPort).save(wallet);
        verify(transactionRepositoryPort).save(any(Transaction.class));
    }

    @Test
    void debit_shouldThrowInsufficientBalanceException_whenInsufficientBalance() {
        BigDecimal largeAmount = new BigDecimal("150.00");

        assertThatThrownBy(() -> adapter.debit(wallet, largeAmount, description, TransactionType.DEBIT))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessage("Saldo insuficiente");

        verify(walletRepositoryPort, never()).save(any(Wallet.class));
        verify(transactionRepositoryPort, never()).save(any(Transaction.class));
    }

    @Test
    void credit_shouldUpdateBalanceAndSave() {
        when(walletRepositoryPort.save(any(Wallet.class))).thenReturn(wallet);
        when(transactionRepositoryPort.save(any(Transaction.class))).thenReturn(new Transaction());

        adapter.credit(wallet, amount, description, TransactionType.CREDIT);

        assertThat(wallet.getBalance()).isEqualTo(new BigDecimal("150.00"));
        verify(walletRepositoryPort).save(wallet);
        verify(transactionRepositoryPort).save(any(Transaction.class));
    }
}
