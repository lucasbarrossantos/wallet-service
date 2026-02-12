package com.globo.wallet.adapter.datasource.database.mapper;

import org.mapstruct.Mapper;

import com.globo.wallet.adapter.datasource.database.entity.transaction.TransactionEntity;
import com.globo.wallet.core.domain.Transaction;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionEntity toEntity(Transaction domain);
    Transaction toDomain(TransactionEntity entity);
}
