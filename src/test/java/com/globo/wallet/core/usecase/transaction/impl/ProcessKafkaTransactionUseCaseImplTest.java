package com.globo.wallet.core.usecase.transaction.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

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
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessKafkaTransactionUseCaseImplTest {

    @Mock
    private GetWalletByUserIdPort getWalletByUserIdPort;

    @Mock
    private WalletTransactionPort walletTransactionPort;

    @Mock
    private SubscriptionClient subscriptionClient;

    @InjectMocks
    private ProcessKafkaTransactionUseCaseImpl useCase;

    private UUID userId;
    private UUID subscriptionId;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        subscriptionId = UUID.randomUUID();
        wallet = new Wallet();
        wallet.setId(UUID.randomUUID());
        wallet.setUserId(userId);
        wallet.setBalance(new BigDecimal("100.00"));
    }

    @Test
    void processDebitSubscriptionPlan_shouldSetStatusActive_whenPaymentApproved() {
        DebitSubscriptionPlanEventDTO event = new DebitSubscriptionPlanEventDTO();
        event.setUserId(userId);
        event.setPlan("BASIC");
        event.setDescription("Plano Básico");
        event.setSubscriptionId(subscriptionId);

        when(getWalletByUserIdPort.execute(userId)).thenReturn(wallet);

        ArgumentCaptor<UpdateSubscriptionStatusRequest> captor = ArgumentCaptor.forClass(UpdateSubscriptionStatusRequest.class);

        useCase.processDebitSubscriptionPlan(event);

        verify(walletTransactionPort).debit(eq(wallet), eq(new BigDecimal("19.90")), eq("Plano Básico"), eq(TransactionType.DEBIT));
        verify(subscriptionClient).updateSubscriptionStatus(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("ACTIVE");
        assertThat(captor.getValue().getSubscriptionId()).isEqualTo(subscriptionId);
    }

    @Test
    void processDebitSubscriptionPlan_shouldSetStatusPaymentFailed_whenInsufficientBalance() {
        DebitSubscriptionPlanEventDTO event = new DebitSubscriptionPlanEventDTO();
        event.setUserId(userId);
        event.setPlan("BASIC");
        event.setDescription("Plano Básico");
        event.setSubscriptionId(subscriptionId);

        wallet.setBalance(new BigDecimal("1.00"));
        when(getWalletByUserIdPort.execute(userId)).thenReturn(wallet);

        ArgumentCaptor<UpdateSubscriptionStatusRequest> captor = ArgumentCaptor.forClass(UpdateSubscriptionStatusRequest.class);

        assertThatThrownBy(() -> useCase.processDebitSubscriptionPlan(event))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessage("Saldo insuficiente para o plano: BASIC");

        verify(walletTransactionPort, never()).debit(any(), any(), any(), any());
        verify(subscriptionClient).updateSubscriptionStatus(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("PAYMENT_FAILED");
    }

    @Test
    void processDebitSubscriptionPlan_shouldNotUpdateStatus_whenSubscriptionIdIsNull() {
        DebitSubscriptionPlanEventDTO event = new DebitSubscriptionPlanEventDTO();
        event.setUserId(userId);
        event.setPlan("PREMIUM");
        event.setDescription("Plano Premium");
        event.setSubscriptionId(null); // Null subscriptionId

        when(getWalletByUserIdPort.execute(userId)).thenReturn(wallet);

        useCase.processDebitSubscriptionPlan(event);

        verify(walletTransactionPort).debit(eq(wallet), eq(new BigDecimal("39.90")), eq("Plano Premium"), eq(TransactionType.DEBIT));
        verifyNoInteractions(subscriptionClient);
    }

    @Test
    void processDebitSubscriptionPlan_shouldThrowBusinessException_whenInvalidPlan() {
        DebitSubscriptionPlanEventDTO event = new DebitSubscriptionPlanEventDTO();
        event.setUserId(userId);
        event.setPlan("INVALID");
        event.setDescription("Plano Inválido");
        event.setSubscriptionId(subscriptionId);

        when(getWalletByUserIdPort.execute(userId)).thenReturn(wallet);

        assertThatThrownBy(() -> useCase.processDebitSubscriptionPlan(event))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Plano inválido: INVALID");

        verifyNoInteractions(walletTransactionPort, subscriptionClient);
    }

    @Test
    void processDebitSubscriptionPlan_shouldRefund_whenUpdateSubscriptionFails() {
        DebitSubscriptionPlanEventDTO event = new DebitSubscriptionPlanEventDTO();
        event.setUserId(userId);
        event.setPlan("FAMILY");
        event.setDescription("Plano Família");
        event.setSubscriptionId(subscriptionId);

        when(getWalletByUserIdPort.execute(userId)).thenReturn(wallet);
        doThrow(new RuntimeException("Update failed")).when(subscriptionClient).updateSubscriptionStatus(any());

        useCase.processDebitSubscriptionPlan(event);

        verify(walletTransactionPort).debit(eq(wallet), eq(new BigDecimal("59.90")), eq("Plano Família"), eq(TransactionType.DEBIT));
        verify(walletTransactionPort).credit(eq(wallet), eq(new BigDecimal("59.90")), eq("Reembolso por falha na atualização do status da assinatura"), eq(TransactionType.CREDIT));
        verify(subscriptionClient).updateSubscriptionStatus(any());
    }

    @Test
    void processDebitSubscriptionPlan_shouldThrowWalletNotFoundException_whenWalletNotFound() {
        DebitSubscriptionPlanEventDTO event = new DebitSubscriptionPlanEventDTO();
        event.setUserId(userId);
        event.setPlan("BASIC");

        when(getWalletByUserIdPort.execute(userId)).thenReturn(null);

        assertThatThrownBy(() -> useCase.processDebitSubscriptionPlan(event))
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessage("Carteira não encontrada para o usuário: " + userId);

        verifyNoInteractions(walletTransactionPort, subscriptionClient);
    }

    @Test
    void processDebitAmount_shouldDebitSuccessfully() {
        DebitAmountEventDTO event = new DebitAmountEventDTO();
        event.setUserId(userId);
        event.setAmount(BigDecimal.valueOf(50.00));
        event.setDescription("Débito manual");
        event.setSubscriptionId(subscriptionId);

        when(getWalletByUserIdPort.execute(userId)).thenReturn(wallet);

        var captor = ArgumentCaptor.forClass(UpdateSubscriptionStatusRequest.class);

        useCase.processDebitAmount(event);

        verify(walletTransactionPort).debit(eq(wallet), eq(BigDecimal.valueOf(50.00)),
                eq("Débito manual"), eq(TransactionType.DEBIT));
        verify(subscriptionClient).updateSubscriptionStatus(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void processDebitAmount_shouldThrowInsufficientBalanceException_whenInsufficientBalance() {
        DebitAmountEventDTO event = new DebitAmountEventDTO();
        event.setUserId(userId);
        event.setAmount(BigDecimal.valueOf(200.00));
        event.setDescription("Débito grande");

        wallet.setBalance(new BigDecimal("50.00"));
        when(getWalletByUserIdPort.execute(userId)).thenReturn(wallet);

        assertThatThrownBy(() -> useCase.processDebitAmount(event))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessage("Saldo insuficiente para débito: 200.0");

        verifyNoInteractions(walletTransactionPort, subscriptionClient);
    }

    @Test
    void processCreditRefund_shouldCreditSuccessfully() {
        CreditRefundEventDTO event = new CreditRefundEventDTO();
        event.setUserId(userId);
        event.setAmount(BigDecimal.valueOf(25.00));
        event.setDescription("Reembolso");
        event.setSubscriptionId(subscriptionId);

        when(getWalletByUserIdPort.execute(userId)).thenReturn(wallet);

        ArgumentCaptor<UpdateSubscriptionStatusRequest> captor = ArgumentCaptor.forClass(UpdateSubscriptionStatusRequest.class);

        useCase.processCreditRefund(event);

        verify(walletTransactionPort).credit(eq(wallet), eq(BigDecimal.valueOf(25.00)), eq("Reembolso"), eq(TransactionType.CREDIT));
        verify(subscriptionClient).updateSubscriptionStatus(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("ACTIVE");
    }
}
