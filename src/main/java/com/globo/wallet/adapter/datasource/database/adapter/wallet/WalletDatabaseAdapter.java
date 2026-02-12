package com.globo.wallet.adapter.datasource.database.adapter.wallet;

import java.util.Optional;
import java.util.UUID;

import com.globo.wallet.adapter.datasource.database.repository.wallet.WalletRepository;
import org.springframework.stereotype.Component;

import com.globo.wallet.adapter.datasource.database.entity.wallet.WalletEntity;
import com.globo.wallet.adapter.datasource.database.mapper.WalletMapper;
import com.globo.wallet.core.domain.Wallet;
import com.globo.wallet.core.port.out.wallet.WalletRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WalletDatabaseAdapter implements WalletRepositoryPort {

    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;

    @Override
    public Wallet save(Wallet wallet) {
        WalletEntity entity = walletMapper.toEntity(wallet);
        return walletMapper.toDomain(walletRepository.save(entity));
    }

    @Override
    public Optional<Wallet> findByUserId(UUID userId) {
        return walletRepository.findByUserId(userId)
                .map(walletMapper::toDomain);
    }
}
