package com.globo.wallet.core.usecase.transaction.impl;

import com.globo.wallet.adapter.integration.subscription.SubscriptionClient;
import com.globo.wallet.adapter.integration.subscription.dto.UpdateSubscriptionStatusRequest;
import com.globo.wallet.adapter.kafka.dto.DebitSubscriptionPlanEventDTO;
import com.globo.wallet.core.domain.Wallet;
import com.globo.wallet.core.domain.enums.TransactionType;
import com.globo.wallet.core.exception.InsufficientBalanceException;
import com.globo.wallet.core.exception.WalletNotFoundException;
import com.globo.wallet.core.port.in.GetWalletByUserIdPort;
import com.globo.wallet.core.port.in.WalletTransactionPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProcessKafkaTransactionUseCaseImplTest {
    @Mock
    private GetWalletByUserIdPort getWalletByUserIdPort;
    @Mock
    private WalletTransactionPort walletTransactionPort;
    @Mock
    private SubscriptionClient subscriptionClient;
    @InjectMocks
    private ProcessKafkaTransactionUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new ProcessKafkaTransactionUseCaseImpl(getWalletByUserIdPort, walletTransactionPort, subscriptionClient);
    }

    @Test
    void shouldSetStatusActiveWhenPaymentApproved() {
        UUID userId = UUID.randomUUID();
        UUID subscriptionId = UUID.randomUUID();
        DebitSubscriptionPlanEventDTO event = new DebitSubscriptionPlanEventDTO();
        event.setUserId(userId);
        event.setPlan("BASIC");
        event.setDescription("desc");
        event.setSubscriptionId(subscriptionId);
        Wallet wallet = mock(Wallet.class);
        when(getWalletByUserIdPort.execute(userId)).thenReturn(wallet);
        when(wallet.getBalance()).thenReturn(new BigDecimal("100.00"));
        ArgumentCaptor<UpdateSubscriptionStatusRequest> captor = ArgumentCaptor.forClass(UpdateSubscriptionStatusRequest.class);
        useCase.processDebitSubscriptionPlan(event);
        verify(walletTransactionPort).debit(eq(wallet), eq(new BigDecimal("9.90")), eq("desc"), eq(TransactionType.DEBIT));
        verify(subscriptionClient).updateSubscriptionStatus(captor.capture());
        assertEquals("ACTIVE", captor.getValue().getStatus());
    }

    @Test
    void shouldSetStatusPaymentFailedWhenInsufficientBalance() {
        UUID userId = UUID.randomUUID();
        UUID subscriptionId = UUID.randomUUID();
        DebitSubscriptionPlanEventDTO event = new DebitSubscriptionPlanEventDTO();
        event.setUserId(userId);
        event.setPlan("BASIC");
        event.setDescription("desc");
        event.setSubscriptionId(subscriptionId);
        Wallet wallet = mock(Wallet.class);
        when(getWalletByUserIdPort.execute(userId)).thenReturn(wallet);
        when(wallet.getBalance()).thenReturn(new BigDecimal("1.00"));
        ArgumentCaptor<UpdateSubscriptionStatusRequest> captor = ArgumentCaptor.forClass(UpdateSubscriptionStatusRequest.class);
        assertThrows(InsufficientBalanceException.class, () -> useCase.processDebitSubscriptionPlan(event));
        verify(subscriptionClient).updateSubscriptionStatus(captor.capture());
        assertEquals("PAYMENT_FAILED", captor.getValue().getStatus());
    }

    @Test
    void shouldThrowWalletNotFoundException() {
        UUID userId = UUID.randomUUID();
        DebitSubscriptionPlanEventDTO event = new DebitSubscriptionPlanEventDTO();
        event.setUserId(userId);
        event.setPlan("BASIC");
        when(getWalletByUserIdPort.execute(userId)).thenReturn(null);
        assertThrows(WalletNotFoundException.class, () -> useCase.processDebitSubscriptionPlan(event));
        verifyNoInteractions(subscriptionClient);
    }
}
