package com.globo.wallet.adapter.http.controller.wallet;

import com.globo.wallet.adapter.http.dto.wallet.BalanceResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.globo.wallet.adapter.http.dto.wallet.WalletRequest;
import com.globo.wallet.adapter.http.dto.wallet.WalletResponse;
import com.globo.wallet.adapter.http.mapper.WalletHttpMapper;
import com.globo.wallet.core.port.in.wallet.CreateWalletPort;
import com.globo.wallet.core.port.in.wallet.GetWalletBalancePort;
import com.globo.wallet.core.port.in.GetWalletByUserIdPort;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final CreateWalletPort createWalletPort;
    private final WalletHttpMapper walletHttpMapper;
    private final GetWalletBalancePort getWalletBalancePort;
    private final GetWalletByUserIdPort getWalletByUserIdPort;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WalletResponse create(@RequestBody @Valid WalletRequest request) {
        var wallet = walletHttpMapper.toDomain(request);
        var savedWallet = createWalletPort.execute(wallet);
        return walletHttpMapper.toResponse(savedWallet);
    }

    @GetMapping("/{userId}/balance")
    @ResponseStatus(HttpStatus.OK)
    public BalanceResponse getBalance(@PathVariable UUID userId) {
        var wallet = getWalletBalancePort.getBalanceByUserId(userId);
        return walletHttpMapper.toBalanceResponse(wallet);
    }

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public WalletResponse getWallet(@PathVariable UUID userId) {
        var wallet = getWalletByUserIdPort.execute(userId);
        return walletHttpMapper.toResponse(wallet);
    }
}
