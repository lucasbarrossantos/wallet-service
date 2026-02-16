# Wallet Service

Serviço responsável pela gestão de carteiras digitais, permitindo a criação de carteiras, consulta de saldo, processamento de transações e integração com eventos Kafka para operações financeiras.

## Funcionalidades Principais

- **Criação de Carteira**: Permite criar uma carteira digital para um usuário, validando a existência do usuário e o saldo mínimo inicial.
- **Consulta de Saldo**: Permite consultar o saldo atual da carteira de um usuário.
- **Consulta de Carteira**: Permite buscar os dados da carteira de um usuário pelo seu ID.
- **Processamento de Transações**: Realiza operações de crédito e débito na carteira do usuário, validando saldo e integridade dos dados.
- **Processamento de Eventos Kafka**:
  - Débito de plano de assinatura
  - Débito de valores avulsos
  - Crédito de reembolsos
- **Integração com Serviço de Assinaturas**: Valida usuários e atualiza status de assinaturas conforme operações financeiras.

## Endpoints REST

- `POST /wallets` — Criação de carteira
- `GET /wallets/{userId}/balance` — Consulta de saldo da carteira
- `GET /wallets/{userId}` — Consulta de dados da carteira
- `POST /wallets/{userId}/transactions` — Processamento de transações (crédito/débito)

## Integrações
- **Kafka**: Consome eventos para processar débitos e créditos automaticamente.
- **Subscription Service**: Valida existência do usuário e atualiza status de assinatura.

## Configuração
- Arquivo principal: `src/main/resources/application.yaml`
- Configurações de ambiente: `application-dev.yaml`, `application-prod.yaml`
- Banco de dados: PostgreSQL
- Portas padrão: 8081 (dev)

## Dependências Principais
- Spring Boot (Web, Data JPA, Validation, Kafka, OpenTelemetry)
- PostgreSQL
- Flyway (migração de banco)
- MapStruct (mapeamento DTO)
- Lombok

## Como rodar localmente

1. Configure o banco de dados PostgreSQL e o Kafka conforme `application-dev.yaml`.
2. Execute o comando:
   ```bash
   ./mvnw spring-boot:run
   ```
3. Acesse os endpoints via `http://localhost:8081/wallet-service/api`.

## Testes

Para rodar os testes automatizados:
```bash
./mvnw test
```

---

> Projeto desenvolvido para gestão de carteiras digitais, com foco em integração, segurança e escalabilidade.
