package com.globo.wallet.adapter.datasource.database.adapter;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.globo.wallet.core.port.out.wallet.WalletRepositoryPort;

import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WalletAdapterTest {

    @Mock
    private WalletRepositoryPort walletRepositoryPort;

    @InjectMocks
    private WalletAdapter adapter;

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
    void execute_shouldReturnWallet_whenWalletExists() {
        when(walletRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(wallet));

        Wallet result = adapter.execute(userId);

        assertThat(result).isEqualTo(wallet);
    }

    @Test
    void execute_shouldReturnNull_whenWalletNotFound() {
        when(walletRepositoryPort.findByUserId(userId)).thenReturn(Optional.empty());

        Wallet result = adapter.execute(userId);

        assertThat(result).isNull();
    }
}
