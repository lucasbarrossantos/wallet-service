package com.globo.wallet.core.usecase.wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.globo.wallet.adapter.integration.subscription.SubscriptionClient;
import com.globo.wallet.core.domain.Wallet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.globo.wallet.core.exception.BusinessException;
import com.globo.wallet.core.exception.UserNotFoundException;
import com.globo.wallet.core.exception.WalletAlreadyExistsException;
import com.globo.wallet.core.port.in.wallet.CreateWalletPort;
import com.globo.wallet.core.port.out.wallet.WalletRepositoryPort;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateWalletUseCase implements CreateWalletPort {

    private final WalletRepositoryPort walletRepositoryPort;
    private final SubscriptionClient subscriptionClient;

    private static final BigDecimal MIN_BALANCE = new BigDecimal("0.10");

    @Override
    public Wallet execute(Wallet wallet) {
        validateUser(wallet.getUserId());
        
        if (wallet.getBalance().compareTo(MIN_BALANCE) < 0) {
            throw new BusinessException("Saldo mínimo para criação de carteira é R$ 0,10");
        }

        if (walletRepositoryPort.findByUserId(wallet.getUserId()).isPresent()) {
            throw new WalletAlreadyExistsException("Usuário já possui uma carteira cadastrada");
        }

        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setUpdatedAt(LocalDateTime.now());

        return walletRepositoryPort.save(wallet);
    }

    private void validateUser(java.util.UUID userId) {
        try {
            subscriptionClient.getUserById(userId);
        } catch (FeignException.NotFound e) {
            throw new UserNotFoundException("Usuário não encontrado no serviço de assinaturas");
        } catch (Exception e) {
            throw new BusinessException("Erro ao validar usuário no serviço de assinaturas");
        }
    }
}
