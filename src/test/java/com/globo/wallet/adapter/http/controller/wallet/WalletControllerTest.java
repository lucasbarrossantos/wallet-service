package com.globo.wallet.adapter.http.controller.wallet;

import com.globo.wallet.adapter.http.dto.wallet.BalanceResponse;
import com.globo.wallet.adapter.http.dto.wallet.WalletRequest;
import com.globo.wallet.adapter.http.dto.wallet.WalletResponse;
import com.globo.wallet.adapter.http.mapper.WalletHttpMapper;
import com.globo.wallet.core.domain.Wallet;
import com.globo.wallet.core.port.in.wallet.CreateWalletPort;
import com.globo.wallet.core.port.in.wallet.GetWalletBalancePort;
import com.globo.wallet.core.port.in.GetWalletByUserIdPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletControllerTest {

    @Mock
    private CreateWalletPort createWalletPort;
    @Mock
    private WalletHttpMapper walletHttpMapper;
    @Mock
    private GetWalletBalancePort getWalletBalancePort;
    @Mock
    private GetWalletByUserIdPort getWalletByUserIdPort;

    @InjectMocks
    private WalletController walletController;

    @Test
    void create_shouldReturnWalletResponse_whenRequestIsValid() {
        UUID userId = UUID.randomUUID();
        BigDecimal initialBalance = new BigDecimal("10.00");
        WalletRequest request = new WalletRequest(userId, initialBalance);
        Wallet wallet = new Wallet();
        wallet.setId(UUID.randomUUID());
        wallet.setUserId(userId);
        wallet.setBalance(initialBalance);
        Wallet savedWallet = new Wallet();
        savedWallet.setId(UUID.randomUUID());
        savedWallet.setUserId(userId);
        savedWallet.setBalance(initialBalance);
        savedWallet.setCreatedAt(java.time.LocalDateTime.now());
        savedWallet.setUpdatedAt(java.time.LocalDateTime.now());

        WalletResponse response = new WalletResponse(
            savedWallet.getId(),
            savedWallet.getUserId(),
            savedWallet.getBalance(),
            savedWallet.getCreatedAt(),
            savedWallet.getUpdatedAt()
        );

        when(walletHttpMapper.toDomain(request)).thenReturn(wallet);
        when(createWalletPort.execute(wallet)).thenReturn(savedWallet);
        when(walletHttpMapper.toResponse(savedWallet)).thenReturn(response);

        WalletResponse result = walletController.create(request);
        assertThat(result).isEqualTo(response);
    }

    @Test
    void getBalance_shouldReturnBalanceResponse_whenUserIdExists() {

        UUID userId = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("20.00");
        Wallet wallet = new Wallet();
        wallet.setId(UUID.randomUUID());
        wallet.setUserId(userId);
        wallet.setBalance(balance);
        BalanceResponse balanceResponse = new BalanceResponse(wallet.getId(), wallet.getUserId(), wallet.getBalance());

        when(getWalletBalancePort.getBalanceByUserId(userId)).thenReturn(wallet);
        when(walletHttpMapper.toBalanceResponse(wallet)).thenReturn(balanceResponse);

        BalanceResponse result = walletController.getBalance(userId);
        assertThat(result).isEqualTo(balanceResponse);
    }

    @Test
    void getWallet_shouldReturnWalletResponse_whenUserIdExists() {

        UUID userId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(UUID.randomUUID());
        wallet.setUserId(userId);
        wallet.setBalance(new BigDecimal("30.00"));
        wallet.setCreatedAt(java.time.LocalDateTime.now());
        wallet.setUpdatedAt(java.time.LocalDateTime.now());

        WalletResponse response = new WalletResponse(
            wallet.getId(),
            wallet.getUserId(),
            wallet.getBalance(),
            wallet.getCreatedAt(),
            wallet.getUpdatedAt()
        );

        when(getWalletByUserIdPort.execute(userId)).thenReturn(wallet);
        when(walletHttpMapper.toResponse(wallet)).thenReturn(response);

        WalletResponse result = walletController.getWallet(userId);
        assertThat(result).isEqualTo(response);
    }

    @Test
    void create_shouldThrowException_whenMapperThrows() {
        UUID userId = UUID.randomUUID();
        BigDecimal initialBalance = new BigDecimal("10.00");
        WalletRequest request = new WalletRequest(userId, initialBalance);
        when(walletHttpMapper.toDomain(request)).thenThrow(new RuntimeException("Mapper error"));
        assertThatThrownBy(() -> walletController.create(request)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void getBalance_shouldThrowException_whenPortThrows() {
        UUID userId = UUID.randomUUID();
        when(getWalletBalancePort.getBalanceByUserId(userId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        assertThatThrownBy(() -> walletController.getBalance(userId)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void getWallet_shouldThrowException_whenPortThrows() {
        UUID userId = UUID.randomUUID();
        when(getWalletByUserIdPort.execute(userId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        assertThatThrownBy(() -> walletController.getWallet(userId)).isInstanceOf(ResponseStatusException.class);
    }
}
