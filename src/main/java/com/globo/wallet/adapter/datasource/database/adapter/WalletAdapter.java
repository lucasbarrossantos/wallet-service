package com.globo.wallet.adapter.datasource.database.adapter;

import com.globo.wallet.core.domain.Wallet;
import com.globo.wallet.core.port.in.GetWalletByUserIdPort;
import com.globo.wallet.core.port.out.wallet.WalletRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WalletAdapter implements GetWalletByUserIdPort {

    private final WalletRepositoryPort walletRepositoryPort;

    @Override
    public Wallet execute(UUID userId) {
        return walletRepositoryPort.findByUserId(userId).orElse(null);
    }
}
