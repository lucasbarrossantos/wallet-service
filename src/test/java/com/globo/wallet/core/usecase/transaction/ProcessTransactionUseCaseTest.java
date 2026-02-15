package com.globo.wallet.core.usecase.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.globo.wallet.adapter.integration.subscription.SubscriptionClient;
import com.globo.wallet.core.domain.Transaction;
import com.globo.wallet.core.domain.Wallet;
import com.globo.wallet.core.domain.enums.TransactionType;
import com.globo.wallet.core.exception.InsufficientBalanceException;
import com.globo.wallet.core.exception.InvalidTransactionAmountException;
import com.globo.wallet.core.exception.UserNotFoundException;
import com.globo.wallet.core.exception.WalletNotFoundException;
import com.globo.wallet.core.port.out.transaction.TransactionRepositoryPort;
import com.globo.wallet.core.port.out.wallet.WalletRepositoryPort;

import feign.FeignException;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessTransactionUseCaseTest {

    @Mock
    private WalletRepositoryPort walletRepositoryPort;

    @Mock
    private TransactionRepositoryPort transactionRepositoryPort;

    @Mock
    private SubscriptionClient subscriptionClient;

    @InjectMocks
    private ProcessTransactionUseCase useCase;

    private UUID userId;
    private UUID walletId;
    private Wallet wallet;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        walletId = UUID.randomUUID();
        wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setUserId(userId);
        wallet.setBalance(new BigDecimal("100.00"));

        transaction = new Transaction();
        transaction.setAmount(new BigDecimal("50.00"));
        transaction.setDescription("Test transaction");
    }

    @Test
    void execute_shouldProcessCreditTransactionSuccessfully() {
        transaction.setType(TransactionType.CREDIT);

        when(subscriptionClient.getUserById(userId)).thenReturn(null);
        when(walletRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(walletRepositoryPort.save(any(Wallet.class))).thenReturn(wallet);
        when(transactionRepositoryPort.save(any(Transaction.class))).thenReturn(transaction);

        Transaction result = useCase.execute(userId, transaction);

        assertThat(result).isNotNull();
        verify(walletRepositoryPort).save(any(Wallet.class));
        verify(transactionRepositoryPort).save(any(Transaction.class));
    }

    @Test
    void execute_shouldProcessDebitTransactionSuccessfully() {
        transaction.setType(TransactionType.DEBIT);

        when(subscriptionClient.getUserById(userId)).thenReturn(null);
        when(walletRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(walletRepositoryPort.save(any(Wallet.class))).thenReturn(wallet);
        when(transactionRepositoryPort.save(any(Transaction.class))).thenReturn(transaction);

        Transaction result = useCase.execute(userId, transaction);

        assertThat(result).isNotNull();
        verify(walletRepositoryPort).save(any(Wallet.class));
        verify(transactionRepositoryPort).save(any(Transaction.class));
    }

    @Test
    void execute_shouldThrowInsufficientBalanceException_whenDebitExceedsBalance() {
        transaction.setType(TransactionType.DEBIT);
        transaction.setAmount(new BigDecimal("150.00"));

        when(subscriptionClient.getUserById(userId)).thenReturn(null);
        when(walletRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> useCase.execute(userId, transaction))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessage("Saldo insuficiente. Saldo atual: R$ 100,00, Valor do débito: R$ 150,00");

        verify(walletRepositoryPort, never()).save(any(Wallet.class));
        verify(transactionRepositoryPort, never()).save(any(Transaction.class));
    }

    @Test
    void execute_shouldThrowInvalidTransactionAmountException_whenAmountIsNull() {
        transaction.setAmount(null);

        when(subscriptionClient.getUserById(userId)).thenReturn(null);
        when(walletRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> useCase.execute(userId, transaction))
                .isInstanceOf(InvalidTransactionAmountException.class)
                .hasMessage("O valor da transação deve ser maior que zero");

        verify(walletRepositoryPort, never()).save(any(Wallet.class));
        verify(transactionRepositoryPort, never()).save(any(Transaction.class));
    }

    @Test
    void execute_shouldThrowInvalidTransactionAmountException_whenAmountIsZero() {
        transaction.setAmount(BigDecimal.ZERO);

        when(subscriptionClient.getUserById(userId)).thenReturn(null);
        when(walletRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> useCase.execute(userId, transaction))
                .isInstanceOf(InvalidTransactionAmountException.class)
                .hasMessage("O valor da transação deve ser maior que zero");

        verify(walletRepositoryPort, never()).save(any(Wallet.class));
        verify(transactionRepositoryPort, never()).save(any(Transaction.class));
    }

    @Test
    void execute_shouldThrowInvalidTransactionAmountException_whenAmountIsNegative() {
        transaction.setAmount(new BigDecimal("-10.00"));

        when(subscriptionClient.getUserById(userId)).thenReturn(null);
        when(walletRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> useCase.execute(userId, transaction))
                .isInstanceOf(InvalidTransactionAmountException.class)
                .hasMessage("O valor da transação deve ser maior que zero");

        verify(walletRepositoryPort, never()).save(any(Wallet.class));
        verify(transactionRepositoryPort, never()).save(any(Transaction.class));
    }

    @Test
    void execute_shouldThrowUserNotFoundException_whenUserNotFound() {
        when(subscriptionClient.getUserById(userId)).thenThrow(FeignException.NotFound.class);

        assertThatThrownBy(() -> useCase.execute(userId, transaction))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Usuário não encontrado: " + userId);

        verify(walletRepositoryPort, never()).findByUserId(any());
        verify(walletRepositoryPort, never()).save(any(Wallet.class));
        verify(transactionRepositoryPort, never()).save(any(Transaction.class));
    }

    @Test
    void execute_shouldThrowUserNotFoundException_whenUserValidationFails() {
        when(subscriptionClient.getUserById(userId)).thenThrow(FeignException.class);

        assertThatThrownBy(() -> useCase.execute(userId, transaction))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Erro ao validar usuário: " + userId);

        verify(walletRepositoryPort, never()).findByUserId(any());
        verify(walletRepositoryPort, never()).save(any(Wallet.class));
        verify(transactionRepositoryPort, never()).save(any(Transaction.class));
    }

    @Test
    void execute_shouldThrowWalletNotFoundException_whenWalletNotFound() {
        when(subscriptionClient.getUserById(userId)).thenReturn(null);
        when(walletRepositoryPort.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(userId, transaction))
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessage("Carteira não encontrada para o usuário: " + userId);

        verify(walletRepositoryPort, never()).save(any(Wallet.class));
        verify(transactionRepositoryPort, never()).save(any(Transaction.class));
    }

    @Test
    void execute_shouldThrowInvalidTransactionAmountException_whenInvalidType() {
        transaction.setType(null);

        when(subscriptionClient.getUserById(userId)).thenReturn(null);
        when(walletRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> useCase.execute(userId, transaction))
                .isInstanceOf(InvalidTransactionAmountException.class)
                .hasMessage("Tipo de transação inválido: null");

        verify(walletRepositoryPort, never()).save(any(Wallet.class));
        verify(transactionRepositoryPort, never()).save(any(Transaction.class));
    }
}
