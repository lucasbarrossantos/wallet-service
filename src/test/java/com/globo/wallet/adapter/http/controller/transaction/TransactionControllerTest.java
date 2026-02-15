package com.globo.wallet.adapter.http.controller.transaction;

import com.globo.wallet.adapter.http.dto.transaction.TransactionRequest;
import com.globo.wallet.adapter.http.dto.transaction.TransactionResponse;
import com.globo.wallet.adapter.http.mapper.TransactionHttpMapper;
import com.globo.wallet.core.domain.Transaction;
import com.globo.wallet.core.domain.Wallet;
import com.globo.wallet.core.domain.enums.TransactionType;
import com.globo.wallet.core.port.in.transaction.ProcessTransactionPort;
import com.globo.wallet.core.port.out.wallet.WalletRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private ProcessTransactionPort processTransactionPort;
    @Mock
    private TransactionHttpMapper transactionHttpMapper;
    @Mock
    private WalletRepositoryPort walletRepositoryPort;

    @InjectMocks
    private TransactionController transactionController;

    @Test
    void processTransaction_shouldReturnResponse_whenValidRequest() {

        UUID userId = UUID.randomUUID();
        TransactionRequest request = new TransactionRequest(TransactionType.CREDIT, new BigDecimal("100.00"), "desc");
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setWalletId(UUID.randomUUID());
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setType(TransactionType.CREDIT);
        transaction.setDescription("desc");
        transaction.setCreatedAt(LocalDateTime.now());
        Transaction processed = new Transaction();
        processed.setId(transaction.getId());
        processed.setWalletId(transaction.getWalletId());
        processed.setAmount(transaction.getAmount());
        processed.setType(transaction.getType());
        processed.setDescription(transaction.getDescription());
        processed.setCreatedAt(transaction.getCreatedAt());
        Wallet wallet = new Wallet();
        wallet.setId(processed.getWalletId());
        wallet.setUserId(userId);
        wallet.setBalance(new BigDecimal("200.00"));
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setUpdatedAt(LocalDateTime.now());

        TransactionResponse response = new TransactionResponse(
            processed.getId(),
            processed.getWalletId(),
            processed.getAmount(),
            processed.getType(),
            processed.getDescription(),
            processed.getCreatedAt(),
            wallet.getBalance()
        );

        when(transactionHttpMapper.toDomain(request)).thenReturn(transaction);
        when(processTransactionPort.execute(userId, transaction)).thenReturn(processed);
        when(walletRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(transactionHttpMapper.toResponse(processed, wallet.getBalance())).thenReturn(response);

        TransactionResponse result = transactionController.processTransaction(userId, request);
        assertThat(result).isEqualTo(response);
    }

    @Test
    void processTransaction_shouldThrow_whenWalletNotFound() {
        UUID userId = UUID.randomUUID();
        TransactionRequest request = new TransactionRequest(TransactionType.DEBIT, new BigDecimal("50.00"), "desc");
        Transaction transaction = new Transaction();

        when(transactionHttpMapper.toDomain(request)).thenReturn(transaction);
        when(processTransactionPort.execute(userId, transaction)).thenReturn(transaction);
        when(walletRepositoryPort.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionController.processTransaction(userId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Carteira não encontrada");
    }

    @Test
    void processTransaction_shouldPropagateException_whenMapperFails() {

        UUID userId = UUID.randomUUID();
        TransactionRequest request = new TransactionRequest(TransactionType.CREDIT, new BigDecimal("10.00"), "desc");

        when(transactionHttpMapper.toDomain(request)).thenThrow(new RuntimeException("Mapper error"));
        assertThatThrownBy(() -> transactionController.processTransaction(userId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Mapper error");
    }
}
