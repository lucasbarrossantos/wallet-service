package com.globo.wallet.core.port.out.wallet;

import java.util.Optional;
import java.util.UUID;

import com.globo.wallet.core.domain.Wallet;

public interface WalletRepositoryPort {
    Wallet save(Wallet wallet);
    Optional<Wallet> findByUserId(UUID userId);
}