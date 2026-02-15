package com.globo.wallet.adapter.datasource.database.repository.transaction;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.globo.wallet.adapter.datasource.database.entity.transaction.TransactionEntity;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {
    List<TransactionEntity> findByWalletId(UUID walletId);
}
