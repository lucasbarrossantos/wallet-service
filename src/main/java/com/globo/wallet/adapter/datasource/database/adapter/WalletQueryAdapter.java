package com.globo.wallet.adapter.datasource.database.adapter;

import com.globo.wallet.core.domain.Wallet;
import com.globo.wallet.core.port.in.WalletQueryPort;
import com.globo.wallet.core.port.out.wallet.WalletRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WalletQueryAdapter implements WalletQueryPort {
    private final WalletRepositoryPort walletRepositoryPort;

    public WalletQueryAdapter(WalletRepositoryPort walletRepositoryPort) {
        this.walletRepositoryPort = walletRepositoryPort;
    }

    @Override
    public Wallet findByUserId(UUID userId) {
        return walletRepositoryPort.findByUserId(userId).orElse(null);
    }
}
