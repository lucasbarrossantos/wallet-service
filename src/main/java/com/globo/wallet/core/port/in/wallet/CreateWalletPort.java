package com.globo.wallet.core.port.in.wallet;

import com.globo.wallet.core.domain.Wallet;

public interface CreateWalletPort {
    Wallet execute(Wallet wallet);
}
