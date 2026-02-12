package com.globo.wallet.adapter.datasource.database.mapper;

import org.mapstruct.Mapper;

import com.globo.wallet.adapter.datasource.database.entity.wallet.WalletEntity;
import com.globo.wallet.core.domain.Wallet;

@Mapper(componentModel = "spring")
public interface WalletMapper {
    Wallet toDomain(WalletEntity entity);
    WalletEntity toEntity(Wallet domain);
}
