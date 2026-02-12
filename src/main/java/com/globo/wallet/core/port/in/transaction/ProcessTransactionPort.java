package com.globo.wallet.core.port.in.transaction;

import com.globo.wallet.core.domain.Transaction;
import java.util.UUID;

public interface ProcessTransactionPort {
    Transaction execute(UUID userId, Transaction transaction);
}
