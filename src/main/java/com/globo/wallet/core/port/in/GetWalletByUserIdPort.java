package com.globo.wallet.core.port.in;

import com.globo.wallet.core.domain.Wallet;
import java.util.UUID;

public interface GetWalletByUserIdPort {
    Wallet execute(UUID userId);
}
