package com.globo.wallet.adapter.datasource.database.adapter.transaction;

import com.globo.wallet.adapter.datasource.database.mapper.TransactionMapper;
import com.globo.wallet.adapter.datasource.database.repository.transaction.TransactionRepository;
import com.globo.wallet.core.domain.Transaction;
import com.globo.wallet.adapter.datasource.database.entity.transaction.TransactionEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionDatabaseAdapterTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionDatabaseAdapter adapter;

    @Test
    void save_shouldPersistAndReturnDomain() {
        Transaction domain = new Transaction();
        TransactionEntity entity = new TransactionEntity();
        TransactionEntity savedEntity = new TransactionEntity();
        Transaction savedDomain = new Transaction();

        when(transactionMapper.toEntity(domain)).thenReturn(entity);
        when(transactionRepository.save(entity)).thenReturn(savedEntity);
        when(transactionMapper.toDomain(savedEntity)).thenReturn(savedDomain);

        Transaction result = adapter.save(domain);
        assertThat(result).isEqualTo(savedDomain);
    }

    @Test
    void findByWalletId_shouldReturnMappedDomainList() {
        UUID walletId = UUID.randomUUID();
        TransactionEntity entity1 = new TransactionEntity();
        TransactionEntity entity2 = new TransactionEntity();
        Transaction domain1 = new Transaction();
        Transaction domain2 = new Transaction();
        List<TransactionEntity> entities = List.of(entity1, entity2);
        List<Transaction> domains = List.of(domain1, domain2);

        when(transactionRepository.findByWalletId(walletId)).thenReturn(entities);
        when(transactionMapper.toDomain(entity1)).thenReturn(domain1);
        when(transactionMapper.toDomain(entity2)).thenReturn(domain2);

        List<Transaction> result = adapter.findByWalletId(walletId);
        assertThat(result).containsExactlyElementsOf(domains);
    }
}
