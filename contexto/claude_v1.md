# SQLab — Contexto de Projeto

## 1. Visão Geral

**SQLab** é uma plataforma web gamificada para o ensino e prática da linguagem SQL, desenvolvida como Trabalho de Conclusão de Curso (TCC) para o curso de Tecnologia em Análise e Desenvolvimento de Sistemas no IFRS Campus Rio Grande.

O projeto é produzido em LaTeX no Overleaf e o código da API é desenvolvido em Java com Spring Boot.

---

## 2. Conceito e Funcionamento

O usuário recebe **missões independentes** com um briefing narrativo. Cada missão carrega um banco de dados temático que é instanciado localmente no navegador via **PGlite** (PostgreSQL via WebAssembly). O usuário escreve SQL livremente em um terminal integrado, executa a query e submete o resultado ao backend para validação.

### Pilares do projeto

- **Terminal livre:** o usuário digita SQL real, sem restrição por botões
- **Sandboxing via PGlite:** a execução é isolada no cliente, sem risco para a infraestrutura central
- **Validação estrutural:** o backend valida as tuplas retornadas, não a query em si
- **Gamificação:** sistema de XP, missões com dificuldades variadas
- **Restauração do banco:** o usuário pode reiniciar o estado original da missão a qualquer momento
- **DML liberado em todas as missões:** o usuário pode executar INSERT, UPDATE, DELETE livremente — o restore é o safety net

---

## 3. Decisões de Design Registradas

### 3.1 Sobre banco de dados
- **PostgreSQL** foi escolhido para o backend por coerência com o PGlite (mesmo dialeto SQL no cliente e no servidor)
- **MongoDB foi descartado** — o modelo de dados é naturalmente relacional
- **Campos como `techniques` usam `TEXT[]`** (array nativo do PostgreSQL) em vez de tabela separada

### 3.2 Sobre arquitetura
- **Arquitetura hexagonal** (Ports and Adapters) para isolar o domínio de frameworks e infraestrutura, favorecendo testabilidade
- **Projeto único** (sem multi-módulos Maven) — complexidade desnecessária para o escopo
- **CQRS** aplicado na camada `port/in`: Commands e Queries são records aninhados dentro de suas próprias interfaces de UseCase
- **DTOs** separados em pacote `dto/` dedicado, fora dos controllers

### 3.3 Sobre o modelo de missões
- **`Scenario` foi removido** — missões são standalone, não pertencem a uma sequência
- **`Theme`** é um enum classificatório (não uma entidade), usado apenas para filtragem
- **`DifficultyLevel`** é um enum com os valores: `BEGINNER`, `INTERMEDIATE`, `ADVANCED`, `EXPERT`
- **`expected_result`** é um campo `JSONB` direto na tabela `missions` (não tabela separada), preservando a ordem das tuplas para validação de `ORDER BY`
- **Flag `ordered`** indica se a validação deve comparar as tuplas em ordem (`ORDER BY`) ou não
- **`ddl_script`** cria as tabelas da missão; **`dml_script`** insere os dados iniciais — juntos constroem o ambiente no PGlite
- **DML liberado:** nenhuma flag restringe comandos DML por missão — decisão intencional que favorece a experimentação

### 3.4 Sobre validação
- O frontend envia as tuplas retornadas pela query do usuário
- O backend realiza validação estrutural: compara as tuplas com o `expected_result` armazenado
- Validação ordenada (`matchesOrdered`) ou não-ordenada (`matchesUnordered`) conforme a flag `ordered` da missão
- A lógica de validação está encapsulada no método `mission.validate()` no domínio
- **Estratégias de validação consideradas e descartadas:**
  - Exigir que as `techniques` estejam presentes na query — bloquearia soluções alternativas válidas
  - Hash do estado do banco + hash do resultado — complexidade sem ganho pedagógico suficiente
  - Reexecutar query no backend em banco efêmero — contradiz a arquitetura de sandboxing no cliente
  - **Decisão final:** validação apenas por tuplas, DML liberado, restore sempre disponível

### 3.5 Sobre segurança e JWT
- **Spring Security** em modo stateless com JWT (JJWT 0.12.6)
- `JwtTokenProvider` implementa a porta de saída `TokenProvider`, mantendo o domínio desacoplado
- Apenas `/api/auth/register` e `/api/auth/login` são públicos

### 3.6 Sobre UserRole
- Enum com valor único `USER` por ora
- Mantido para extensibilidade futura (ex: `ADMIN` para gerenciamento de missões)

---

## 4. Stack Tecnológica

### Backend (API)
| Tecnologia | Versão | Papel |
|---|---|---|
| Java | 25 (LTS) | Linguagem principal |
| Spring Boot | 3.4.4 | Framework web |
| Spring Security | — | Autenticação e autorização |
| Spring Data JPA | — | Persistência |
| PostgreSQL | 17 | Banco de dados |
| Flyway | — | Migrations |
| JJWT | 0.12.6 | Tokens JWT |
| Lombok | — | Redução de boilerplate |
| Testcontainers | — | Testes de integração |
| Maven | 3.9 | Gerenciador de dependências |
| Docker | — | Containerização |

### Frontend (planejado)
| Tecnologia | Papel |
|---|---|
| Angular + TypeScript | Framework frontend |
| PGlite | PostgreSQL no navegador via WebAssembly |

---

## 5. Estrutura do Projeto (API)

```
sqlab-api/
├── .env.example
├── .gitignore
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── src/main/
    ├── java/com/sqlab/
    │   ├── SqlabApplication.java
    │   ├── domain/
    │   │   ├── model/
    │   │   │   ├── DifficultyLevel.java      (enum: BEGINNER, INTERMEDIATE, ADVANCED, EXPERT)
    │   │   │   ├── ExpectedTuple.java         (record: matchesOrdered / matchesUnordered)
    │   │   │   ├── Mission.java
    │   │   │   ├── Progress.java
    │   │   │   ├── Theme.java                 (enum: ASTRONOMY, CYBERSECURITY, CRIMINAL, FINANCE, BIOLOGY)
    │   │   │   ├── User.java
    │   │   │   └── UserRole.java              (enum: USER)
    │   │   └── exception/
    │   │       ├── InvalidCredentialsException.java
    │   │       ├── MissionNotFoundException.java
    │   │       └── UserAlreadyExistsException.java
    │   ├── application/
    │   │   ├── port/
    │   │   │   ├── in/
    │   │   │   │   ├── AuthenticateUserUseCase.java   (Command aninhado)
    │   │   │   │   ├── GetMissionsUseCase.java        (ListAllQuery + FindByIdQuery aninhados)
    │   │   │   │   ├── GetUserProgressUseCase.java    (Query aninhada)
    │   │   │   │   ├── RegisterUserUseCase.java       (Command aninhado)
    │   │   │   │   └── ValidateMissionUseCase.java    (Command aninhado)
    │   │   │   └── out/
    │   │   │       ├── MissionRepository.java
    │   │   │       ├── ProgressRepository.java
    │   │   │       ├── TokenProvider.java
    │   │   │       └── UserRepository.java
    │   │   └── usecase/
    │   │       ├── AuthenticateUserService.java
    │   │       ├── GetMissionsService.java
    │   │       ├── GetUserProgressService.java
    │   │       ├── RegisterUserService.java
    │   │       └── ValidateMissionService.java
    │   └── infrastructure/
    │       ├── config/
    │       │   ├── JwtAuthFilter.java
    │       │   ├── JwtTokenProvider.java      (implementa TokenProvider)
    │       │   └── SecurityConfig.java
    │       └── adapter/
    │           ├── in/web/
    │           │   ├── dto/
    │           │   │   ├── AuthDto.java
    │           │   │   ├── ErrorResponse.java
    │           │   │   ├── MissionDto.java
    │           │   │   └── UserDto.java
    │           │   ├── AuthController.java
    │           │   ├── GlobalExceptionHandler.java
    │           │   ├── MissionController.java
    │           │   └── UserController.java
    │           └── out/persistence/
    │               ├── entity/
    │               │   ├── MissionJpaEntity.java
    │               │   ├── ProgressJpaEntity.java
    │               │   └── UserJpaEntity.java
    │               ├── mapper/
    │               │   ├── MissionMapper.java
    │               │   └── UserMapper.java
    │               ├── repository/
    │               │   ├── MissionJpaRepository.java
    │               │   ├── ProgressJpaRepository.java
    │               │   └── UserJpaRepository.java
    │               ├── MissionPersistenceAdapter.java
    │               ├── ProgressPersistenceAdapter.java
    │               └── UserPersistenceAdapter.java
    └── resources/
        ├── application.yml
        └── db/migration/
            ├── V1__create_initial_schema.sql
            └── V2__seed_missions.sql
```

---

## 6. Schema do Banco de Dados

```sql
users (
    id            UUID PRIMARY KEY,
    username      VARCHAR(50) UNIQUE NOT NULL,
    email         VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    xp            INTEGER DEFAULT 0,
    role          VARCHAR(20) DEFAULT 'USER',
    created_at    TIMESTAMP
)

missions (
    id              UUID PRIMARY KEY,
    title           VARCHAR(100) NOT NULL,
    briefing        TEXT NOT NULL,
    ddl_script      TEXT NOT NULL,       -- CREATE TABLE(s)
    dml_script      TEXT,                -- INSERT INTO (dados iniciais)
    techniques      TEXT[],              -- ex: {'SELECT', 'INNER JOIN'}
    xp_reward       INTEGER DEFAULT 100,
    expected_result JSONB NOT NULL,      -- lista ordenada de tuplas esperadas
    ordered         BOOLEAN DEFAULT FALSE,
    theme           VARCHAR(20) NOT NULL, -- enum: CRIMINAL, ASTRONOMY, etc.
    difficulty      VARCHAR(20) NOT NULL, -- enum: BEGINNER, INTERMEDIATE, etc.
    created_at      TIMESTAMP
)

progress (
    id           UUID PRIMARY KEY,
    user_id      UUID REFERENCES users(id),
    mission_id   UUID REFERENCES missions(id),
    completed    BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP,
    UNIQUE (user_id, mission_id)
)
```

---

## 7. Endpoints da API

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| POST | `/api/auth/register` | ❌ | Cadastro de usuário |
| POST | `/api/auth/login` | ❌ | Login, retorna JWT |
| GET | `/api/missions` | ✅ | Lista missões (filtros: `?theme=&difficulty=`) |
| GET | `/api/missions/{id}` | ✅ | Detalhe de uma missão |
| POST | `/api/missions/{id}/validate` | ✅ | Submete tuplas para validação |
| GET | `/api/users/me` | ✅ | Perfil do usuário autenticado |
| GET | `/api/users/me/progress` | ✅ | Progresso do usuário |

---

## 8. Seed de Missões (V2)

| # | Título | Theme | Difficulty | Técnicas principais |
|---|---|---|---|---|
| 1 | A Lista de Suspeitos | CRIMINAL | BEGINNER | SELECT |
| 2 | Suspeitos de São Paulo | CRIMINAL | BEGINNER | SELECT, WHERE |
| 3 | Cruzando Evidências | CRIMINAL | INTERMEDIATE | INNER JOIN |
| 4 | Planetas do Sistema Solar | ASTRONOMY | INTERMEDIATE | ORDER BY |
| 5 | Média de Distância por Tipo | ASTRONOMY | ADVANCED | GROUP BY, AVG |
| 6 | Usuários sem Autenticação Recente | CYBERSECURITY | ADVANCED | IS NULL, OR |
| 7 | Clientes Acima da Média | FINANCE | EXPERT | Subquery, AVG |
| 8 | Corrigindo o Cadastro | FINANCE | BEGINNER | UPDATE (DML) |

---

## 9. Próximos Passos

- [ ] Passo 8 — Testes unitários (casos de uso) e de integração (Testcontainers)
- [ ] Desenvolvimento do frontend Angular
- [ ] Integração PGlite no frontend
- [ ] Redação das seções restantes do TCC (Implementação, Testes, Considerações Finais)