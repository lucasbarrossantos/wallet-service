package com.globo.wallet.adapter.http.mapper;

import org.mapstruct.Mapper;

import com.globo.wallet.adapter.http.dto.transaction.TransactionRequest;
import com.globo.wallet.adapter.http.dto.transaction.TransactionResponse;
import com.globo.wallet.core.domain.Transaction;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface TransactionHttpMapper {
    Transaction toDomain(TransactionRequest request);
    TransactionResponse toResponse(Transaction domain, BigDecimal newBalance);
}
