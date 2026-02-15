package com.globo.wallet.core.usecase.wallet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.globo.wallet.core.domain.Wallet;
import com.globo.wallet.core.exception.UserNotFoundException;
import com.globo.wallet.core.port.out.wallet.WalletRepositoryPort;

import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetWalletBalanceUseCaseTest {

    @Mock
    private WalletRepositoryPort walletRepositoryPort;

    @InjectMocks
    private GetWalletBalanceUseCase useCase;

    private UUID userId;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        wallet = new Wallet();
        wallet.setId(UUID.randomUUID());
        wallet.setUserId(userId);
        wallet.setBalance(new BigDecimal("100.00"));
    }

    @Test
    void getBalanceByUserId_shouldReturnWallet_whenWalletExists() {
        when(walletRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(wallet));

        Wallet result = useCase.getBalanceByUserId(userId);

        assertThat(result).isEqualTo(wallet);
    }

    @Test
    void getBalanceByUserId_shouldThrowUserNotFoundException_whenWalletNotFound() {
        when(walletRepositoryPort.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getBalanceByUserId(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Carteira não encontrada para o usuário: " + userId);
    }
}
