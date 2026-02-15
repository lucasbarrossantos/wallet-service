package com.globo.wallet.adapter.datasource.database.adapter.wallet;

import com.globo.wallet.adapter.datasource.database.entity.wallet.WalletEntity;
import com.globo.wallet.adapter.datasource.database.mapper.WalletMapper;
import com.globo.wallet.adapter.datasource.database.repository.wallet.WalletRepository;
import com.globo.wallet.core.domain.Wallet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletDatabaseAdapterTest {

    @Mock
    private WalletRepository walletRepository;
    @Mock
    private WalletMapper walletMapper;

    @InjectMocks
    private WalletDatabaseAdapter adapter;

    @Test
    void save_shouldPersistAndReturnDomain() {
        Wallet domain = new Wallet();
        WalletEntity entity = new WalletEntity();
        WalletEntity savedEntity = new WalletEntity();
        Wallet savedDomain = new Wallet();

        when(walletMapper.toEntity(domain)).thenReturn(entity);
        when(walletRepository.save(entity)).thenReturn(savedEntity);
        when(walletMapper.toDomain(savedEntity)).thenReturn(savedDomain);

        Wallet result = adapter.save(domain);
        assertThat(result).isEqualTo(savedDomain);
    }

    @Test
    void findByUserId_shouldReturnMappedDomainOptional() {
        UUID userId = UUID.randomUUID();
        WalletEntity entity = new WalletEntity();
        Wallet domain = new Wallet();
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(entity));
        when(walletMapper.toDomain(entity)).thenReturn(domain);
        Optional<Wallet> result = adapter.findByUserId(userId);
        assertThat(result).isPresent().contains(domain);
    }

    @Test
    void findByUserId_shouldReturnEmptyOptional_whenNotFound() {
        UUID userId = UUID.randomUUID();
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());
        Optional<Wallet> result = adapter.findByUserId(userId);
        assertThat(result).isEmpty();
    }
}
