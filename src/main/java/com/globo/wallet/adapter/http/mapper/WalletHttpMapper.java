package com.globo.wallet.adapter.http.mapper;

import com.globo.wallet.adapter.http.dto.wallet.BalanceResponse;
import org.mapstruct.Mapper;

import com.globo.wallet.adapter.http.dto.wallet.WalletRequest;
import com.globo.wallet.adapter.http.dto.wallet.WalletResponse;
import com.globo.wallet.core.domain.Wallet;

@Mapper(componentModel = "spring")
public interface WalletHttpMapper {
    Wallet toDomain(WalletRequest request);
    WalletResponse toResponse(Wallet domain);
    BalanceResponse toBalanceResponse(Wallet domain);
}
