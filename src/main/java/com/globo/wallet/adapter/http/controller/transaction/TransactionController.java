package com.globo.wallet.adapter.http.controller.transaction;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.globo.wallet.adapter.http.dto.transaction.TransactionRequest;
import com.globo.wallet.adapter.http.dto.transaction.TransactionResponse;
import com.globo.wallet.adapter.http.mapper.TransactionHttpMapper;
import com.globo.wallet.core.port.in.transaction.ProcessTransactionPort;
import com.globo.wallet.core.port.out.wallet.WalletRepositoryPort;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/wallets/{userId}/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final ProcessTransactionPort processTransactionPort;
    private final TransactionHttpMapper transactionHttpMapper;
    private final WalletRepositoryPort walletRepositoryPort;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse processTransaction(
            @PathVariable UUID userId,
            @RequestBody @Valid TransactionRequest request) {

        log.info("Recebida requisição de transação para usuário: {}, tipo: {}", userId, request.type());

        var transaction = transactionHttpMapper.toDomain(request);
        var processedTransaction = processTransactionPort.execute(userId, transaction);

        var wallet = walletRepositoryPort.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Carteira não encontrada"));

        return transactionHttpMapper.toResponse(processedTransaction, wallet.getBalance());
    }
}
