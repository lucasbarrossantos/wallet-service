package com.globo.wallet.adapter.datasource.database.adapter.transaction;

import java.util.List;
import java.util.UUID;

import com.globo.wallet.adapter.datasource.database.repository.transaction.TransactionRepository;
import org.springframework.stereotype.Component;

import com.globo.wallet.adapter.datasource.database.mapper.TransactionMapper;
import com.globo.wallet.core.domain.Transaction;
import com.globo.wallet.core.port.out.transaction.TransactionRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransactionDatabaseAdapter implements TransactionRepositoryPort {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public Transaction save(Transaction transaction) {
        var entity = transactionMapper.toEntity(transaction);
        var savedEntity = transactionRepository.save(entity);
        return transactionMapper.toDomain(savedEntity);
    }

    @Override
    public List<Transaction> findByWalletId(UUID walletId) {
        return transactionRepository.findByWalletId(walletId)
                .stream()
                .map(transactionMapper::toDomain)
                .toList();
    }
}
