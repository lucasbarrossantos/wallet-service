package com.globo.wallet.adapter.http.controller.wallet;

import com.globo.wallet.adapter.http.dto.wallet.WalletResponse;
import com.globo.wallet.adapter.http.mapper.WalletHttpMapper;
import com.globo.wallet.core.domain.Wallet;
import com.globo.wallet.core.port.in.wallet.CreateWalletPort;
import com.globo.wallet.core.port.in.wallet.GetWalletBalancePort;
import com.globo.wallet.core.port.in.GetWalletByUserIdPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private WalletController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new WalletController(createWalletPort, walletHttpMapper, getWalletBalancePort, getWalletByUserIdPort);
    }

    @Test
    void shouldReturnWalletResponseForGetWallet() {
        UUID userId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        WalletResponse response = mock(WalletResponse.class);
        when(getWalletByUserIdPort.execute(userId)).thenReturn(wallet);
        when(walletHttpMapper.toResponse(wallet)).thenReturn(response);
        WalletResponse result = controller.getWallet(userId);
        assertEquals(response, result);
        verify(getWalletByUserIdPort).execute(userId);
        verify(walletHttpMapper).toResponse(wallet);
    }
}
