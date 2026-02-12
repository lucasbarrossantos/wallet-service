package com.globo.wallet.core.port.out.transaction;

import com.globo.wallet.core.domain.Transaction;
import java.util.List;
import java.util.UUID;

public interface TransactionRepositoryPort {
    Transaction save(Transaction transaction);
    List<Transaction> findByWalletId(UUID walletId);
}
