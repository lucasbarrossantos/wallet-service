package com.globo.wallet.core.usecase.wallet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.globo.wallet.adapter.integration.subscription.SubscriptionClient;
import com.globo.wallet.core.domain.Wallet;
import com.globo.wallet.core.exception.BusinessException;
import com.globo.wallet.core.exception.UserNotFoundException;
import com.globo.wallet.core.exception.WalletAlreadyExistsException;
import com.globo.wallet.core.port.out.wallet.WalletRepositoryPort;

import feign.FeignException;

@ExtendWith(MockitoExtension.class)
class CreateWalletUseCaseTest {

    @Mock
    private WalletRepositoryPort walletRepositoryPort;

    @Mock
    private SubscriptionClient subscriptionClient;

    @InjectMocks
    private CreateWalletUseCase createWalletUseCase;

    private Wallet wallet;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(new BigDecimal("10.00"));
    }

    @Test
    void execute_shouldCreateWallet_whenValidInput() {
        // Given
        when(subscriptionClient.getUserById(userId)).thenReturn(null); // Assuming it returns something, but we don't care
        when(walletRepositoryPort.findByUserId(userId)).thenReturn(Optional.empty());
        when(walletRepositoryPort.save(any(Wallet.class))).thenReturn(wallet);

        // When
        Wallet result = createWalletUseCase.execute(wallet);

        // Then
        assertThat(result).isEqualTo(wallet);
        verify(subscriptionClient).getUserById(userId);
        verify(walletRepositoryPort).findByUserId(userId);
        verify(walletRepositoryPort).save(wallet);
        verifyNoMoreInteractions(subscriptionClient, walletRepositoryPort);
    }

    @Test
    void execute_shouldThrowUserNotFoundException_whenUserNotFound() {
        // Given
        when(subscriptionClient.getUserById(userId)).thenThrow(FeignException.NotFound.class);

        // When & Then
        assertThatThrownBy(() -> createWalletUseCase.execute(wallet))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Usuário não encontrado no serviço de assinaturas");

        verify(subscriptionClient).getUserById(userId);
        verifyNoMoreInteractions(subscriptionClient);
        verifyNoInteractions(walletRepositoryPort);
    }

    @Test
    void execute_shouldThrowBusinessException_whenSubscriptionClientError() {
        // Given
        when(subscriptionClient.getUserById(userId)).thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThatThrownBy(() -> createWalletUseCase.execute(wallet))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Erro ao validar usuário no serviço de assinaturas");

        verify(subscriptionClient).getUserById(userId);
        verifyNoMoreInteractions(subscriptionClient);
        verifyNoInteractions(walletRepositoryPort);
    }

    @Test
    void execute_shouldThrowBusinessException_whenBalanceBelowMinimum() {
        // Given
        wallet.setBalance(new BigDecimal("0.05"));

        // When & Then
        assertThatThrownBy(() -> createWalletUseCase.execute(wallet))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Saldo mínimo para criação de carteira é R$ 0,10");

        verify(subscriptionClient).getUserById(userId);
        verifyNoMoreInteractions(subscriptionClient);
        verifyNoInteractions(walletRepositoryPort);
    }

    @Test
    void execute_shouldThrowWalletAlreadyExistsException_whenWalletExists() {
        // Given
        when(subscriptionClient.getUserById(userId)).thenReturn(null);
        when(walletRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(wallet));

        // When & Then
        assertThatThrownBy(() -> createWalletUseCase.execute(wallet))
                .isInstanceOf(WalletAlreadyExistsException.class)
                .hasMessage("Usuário já possui uma carteira cadastrada");

        verify(subscriptionClient).getUserById(userId);
        verify(walletRepositoryPort).findByUserId(userId);
        verifyNoMoreInteractions(subscriptionClient, walletRepositoryPort);
    }
}