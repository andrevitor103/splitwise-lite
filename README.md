# SplitWise-lite

API REST em **Kotlin + Spring Boot** para dividir despesas entre amigos (estilo Splitwise).

## Stack

- Kotlin 2.1 / Java 21
- Spring Boot 3.4 (Web, Data JPA, Security, Validation)
- JWT (jjwt)
- H2 (arquivo local em `./data`)

## Como rodar

```bash
./gradlew bootRun
```

- API: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console  
  - JDBC URL: `jdbc:h2:file:./data/splitwise`  
  - User: `sa` / senha vazia

## Usuários seed

| Email | Senha |
|-------|-------|
| andre@email.com | senha123 |
| maria@email.com | senha123 |
| joao@email.com | senha123 |

Já existe o grupo **Viagem Florianópolis** com 3 despesas de exemplo.

## Fluxo rápido (curl)

### 1. Login

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"andre@email.com","password":"senha123"}'
```

Guarde o `token` da resposta.

### 2. Listar grupos

```bash
TOKEN=... # cole o JWT

curl -s http://localhost:8080/api/groups \
  -H "Authorization: Bearer $TOKEN"
```

### 3. Ver balanço

```bash
curl -s http://localhost:8080/api/groups/1/balance \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Criar despesa

```bash
curl -s -X POST http://localhost:8080/api/groups/1/expenses \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "description": "Café da manhã",
    "amount": 45.00,
    "participantIds": [1, 2, 3]
  }'
```

### 5. Registrar novo usuário

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Ana","email":"ana@email.com","password":"senha123"}'
```

### 6. Adicionar membro ao grupo

```bash
curl -s -X POST http://localhost:8080/api/groups/1/members \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"email":"ana@email.com"}'
```

## Endpoints

| Método | Path | Auth |
|--------|------|------|
| POST | `/api/auth/register` | não |
| POST | `/api/auth/login` | não |
| POST | `/api/groups` | sim |
| GET | `/api/groups` | sim |
| GET | `/api/groups/{id}` | sim |
| POST | `/api/groups/{id}/members` | sim |
| POST | `/api/groups/{id}/expenses` | sim |
| GET | `/api/groups/{id}/expenses` | sim |
| GET | `/api/groups/{id}/balance` | sim |

## Estrutura do código

```
com.example.splitwise
├── config       # Security + seed
├── domain       # Entidades JPA
├── repository   # Spring Data
├── security     # JWT + UserPrincipal
├── service      # Regras de negócio + BalanceCalculator
└── web          # Controllers, DTOs, exception handler
```

## Testes

```bash
./gradlew test
```

O teste principal está em `BalanceCalculatorTest` (algoritmo de simplificação de dívidas).

## Conceitos para estudar no código

1. **Camadas**: Controller → Service → Repository  
2. **JWT stateless** em `JwtAuthenticationFilter` + `SecurityConfig`  
3. **Divisão igual** com ajuste do centavo no último participante (`ExpenseService`)  
4. **Debt simplification** em `BalanceCalculator`  
5. **Validações** com Bean Validation nos DTOs  
6. **Erros de domínio** via `ApiException` + `GlobalExceptionHandler`
