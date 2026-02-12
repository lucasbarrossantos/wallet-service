package com.globo.wallet.core.usecase.wallet;

import com.globo.wallet.core.domain.Wallet;
import com.globo.wallet.core.exception.UserNotFoundException;
import com.globo.wallet.core.port.in.wallet.GetWalletBalancePort;
import com.globo.wallet.core.port.out.wallet.WalletRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetWalletBalanceUseCase implements GetWalletBalancePort {

    private final WalletRepositoryPort walletRepositoryPort;

    @Override
    public Wallet getBalanceByUserId(UUID userId) {
        log.info("Buscando saldo da carteira para o usuário: {}", userId);

        return walletRepositoryPort.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Carteira não encontrada para o usuário: " + userId));
    }
}
