package com.globo.wallet.core.port.in;

import com.globo.wallet.core.domain.Wallet;
import com.globo.wallet.core.domain.enums.TransactionType;
import java.math.BigDecimal;

public interface WalletTransactionPort {
    void debit(Wallet wallet, BigDecimal amount, String description, TransactionType type);
    void credit(Wallet wallet, BigDecimal amount, String description, TransactionType type);
}
