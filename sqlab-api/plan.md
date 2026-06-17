# Plano de Correção — sqlab-api

> Análise de integridade completa em 185 testes (0 falhas).
> Data: 2026-06-14

---

## 🔴 Fase 1 — Correções Críticas (Arquitetura + Segurança)

### 1.1 Remover `PasswordEncoder` do domínio e aplicação

**Motivação:** `User.matchesPassword()` depende de `org.springframework.security.crypto.password.PasswordEncoder` — leak do Spring Security no domínio. `RegisterUserService` e `AuthenticateUserService` também injetam `PasswordEncoder` diretamente.

**O que fazer:**
1. Criar `PasswordHasher` em `application/port/out/`
2. `User.matchesPassword()` recebe `PasswordHasher` (ou `Function<String, Boolean>`)
3. Implementar `SpringPasswordHasher` em infraestrutura (delega para `PasswordEncoder`)
4. Atualizar `RegisterUserService` e `AuthenticateUserService` para usar `PasswordHasher`

**Arquivos afetados:**
- `domain/model/User.java` — trocar `PasswordEncoder` por `PasswordHasher`
- `application/port/out/PasswordHasher.java` — **novo**
- `application/usecase/RegisterUserService.java`
- `application/usecase/AuthenticateUserService.java`
- `infrastructure/config/SpringPasswordHasher.java` — **novo** (injeta `PasswordEncoder`)
- `infrastructure/config/SecurityConfig.java` — **manter** o bean `passwordEncoder()`
- `application/usecase/RegisterUserServiceTest.java`
- `application/usecase/AuthenticateUserServiceTest.java`
- `domain/model/UserTest.java`

**⚠️ Atenção:** O bean `PasswordEncoder` em `SecurityConfig.java` deve ser **mantido** — o `SpringPasswordHasher` o utiliza como dependência. A mudança é que use cases e domínio não o enxergam mais diretamente.

**Verificação:** `mvn test` — 0 falhas, `UserTest` não precisa mais de `@Mock`

---

### 1.2 Extrair queries dos controllers para use cases

**Motivação:** 3 controllers injetam portas de saída (`MissionRepository`, `ScenarioRepository`, `ProgressRepository`, `ThemeRepository`, `TechniqueRepository`). Controllers devem depender apenas de portas de entrada (use cases).

#### 1.2.1 `UserController.getProgress()` — linhas 58-71

**Lógica vazada:** Busca missions por IDs, busca scenarios por IDs, monta `ProgressResponse` com título da missão e cenário.

**O que fazer:**
1. Adicionar campo `scenarioTitle` e `missionTitle` em `GetUserProgressUseCase.Query` — ou criar DTO de resposta
2. Mover a lógica de enriquecimento para `GetUserProgressService`
3. Remover `MissionRepository` e `ScenarioRepository` de `UserController`

**Arquivos afetados:**
- `application/port/in/GetUserProgressUseCase.java` — adicionar DTO de resposta
- `application/usecase/GetUserProgressService.java` — enriquecer com títulos
- `infrastructure/adapter/in/web/UserController.java` — remover repositórios, usar use case
- `infrastructure/adapter/in/web/dto/UserDto.java` — ajustar se necessário

#### 1.2.2 `MissionController.resolveTheme()` e `resolveTechniques()`

**Lógica vazada:** Lookup de `Theme` e `Technique` por nome feito diretamente no controller.

**O que fazer:**
1. Mover lookup para `ManageMissionService.create()` e `ManageMissionService.update()`
2. `ManageMissionUseCase.CreateMissionCommand` recebe `String themeName` e `List<String> techniqueNames`
3. Remover `ThemeRepository` e `TechniqueRepository` de `MissionController`

**Arquivos afetados:**
- `application/port/in/ManageMissionUseCase.java` — commands com nomes
- `application/usecase/ManageMissionService.java` — resolver nomes
- `infrastructure/adapter/in/web/MissionController.java` — remover repositórios
- `infrastructure/adapter/in/web/dto/MissionDto.java`

#### 1.2.3 `ScenarioController` — filtragem e queries

**Lógica vazada:** Filtragem de cenários com todas as missões habilitadas, queries de progresso, `ProgressRepository` e `ThemeRepository` injetados diretamente.

**O que fazer:**
1. Mover filtragem de cenários (user-facing) para **`GetScenariosService`** — ele já injeta `MissionRepository` e `ProgressRepository`
2. Mover `ProgressRepository` para `GetScenariosService` (usado em `listAll()` para contar progresso)
3. Mover `ThemeRepository` (usado em `resolveTheme()`) para `ManageScenarioService`
4. `GetScenariosUseCase.ScenarioDetailResult` enriquecido com dados de progresso
5. Remover `MissionRepository`, `ProgressRepository`, `ThemeRepository` de `ScenarioController`

**Arquivos afetados:**
- `application/port/in/GetScenariosUseCase.java`
- `application/port/in/GetAdminScenariosUseCase.java` (se necessário)
- `application/usecase/GetScenariosService.java`
- `infrastructure/adapter/in/web/ScenarioController.java`

**Verificação:** `mvn test`

#### 1.2.4 — Cleanup: atualizar testes dos controllers

**Motivação:** Após remover os repositórios dos controllers, os testes precisam parar de mocká-los.

**Arquivos afetados:**
- `infrastructure/adapter/in/web/UserControllerTest.java` — remover `@MockitoBean` de `MissionRepository` e `ScenarioRepository`
- `infrastructure/adapter/in/web/MissionControllerTest.java` — remover `@MockitoBean` de `ThemeRepository` e `TechniqueRepository`
- `infrastructure/adapter/in/web/ScenarioControllerTest.java` — remover `@MockitoBean` de `MissionRepository`, `ProgressRepository` e `ThemeRepository`

---

### 1.3 Sanitizar exception handler

**Motivação:**
1. Handler genérico vaza `ex.getMessage()` — risco de segurança
2. `IllegalArgumentException` não tratado — cai no handler genérico (500 em vez de 400/404)
3. Formato 403 inconsistente: `Map<String, Object>` vs `ErrorResponse`

**O que fazer:**
1. Handler genérico retorna mensagem fixa ("An internal error occurred") + `uuid` de correlação (opcional)
2. Adicionar `@ExceptionHandler(IllegalArgumentException.class)` → 400 BAD_REQUEST
3. Adicionar handlers para `HttpMessageNotReadableException`, `HttpRequestMethodNotSupportedException`, `MissingServletRequestParameterException`
4. Unificar formato 403: `ErrorResponse` ganha campos opcionais `code` (String), `scenarioId` (UUID), `requiredLevel` (Integer), `currentLevel` (Integer) — preserva os dados contextuais sem usar `Map<String, Object>`

**Arquivos afetados:**
- `infrastructure/adapter/in/web/GlobalExceptionHandler.java`
- `infrastructure/adapter/in/web/dto/ErrorResponse.java` — adicionar campo `code`

**Verificação:** `mvn test` + testes do `GlobalExceptionHandler` (a serem criados na Fase 2)

---

### 1.4 Unificar geração de ID nas JPA entities

**Motivação:** Inconsistência: 4 entidades usam `@PrePersist` com `UUID.randomUUID()`, 2 usam `@GeneratedValue(GenerationType.UUID)`.

**O que fazer:**
- Padronizar para **`@PrePersist`** (já usado pela maioria, mais simples e portável) em `TechniqueJpaEntity` e `ThemeJpaEntity`
- Remover `@GeneratedValue(GenerationType.UUID)` — `@PrePersist` com `UUID.randomUUID()` é o padrão do projeto

**Arquivos afetados:**
- `infrastructure/adapter/out/persistence/entity/TechniqueJpaEntity.java`
- `infrastructure/adapter/out/persistence/entity/ThemeJpaEntity.java`

**Verificação:** `mvn test` — repository tests validam persistência

---

## 🟡 Fase 2 — Cobertura de Testes

### 2.1 `MissionAccessValidatorTest` 🔴 Prioridade Máxima

81 linhas de lógica, **zero testes diretos**. É a classe mais crítica sem teste.

**Cenários a cobrir:**
- `ensureAccessible()` com missão habilitada e desabilitada
- `ensureAccessible()` com cenário com missões desabilitadas
- `checkLevel()` com level suficiente e insuficiente
- `checkLevel()` com admin bypass
- `checkOrder()` com missão anterior completa e incompleta
- `checkOrder()` com `orderIndex = 1` (não verifica ordem)
- `checkOrder()` com `userId = null`
- Todas as exceptions (`MissionNotFoundException`, `LevelRequiredException`, `MissionLockedException`)

**Arquivo novo:** `src/test/java/.../application/usecase/MissionAccessValidatorTest.java`

---

### 2.2 `JwtTokenProviderTest`

**Cenários:**
- Geração de token retorna string não vazia
- `extractUserId` retorna UUID correto
- `extractRole` retorna role correta
- `isValid` com token válido, expirado (manipular `expiration` via config ou reflection), malformado
- `@PostConstruct` valida chave curta

---

### 2.3 `GlobalExceptionHandlerTest`

Usar `@WebMvcTest` com um controller dummy ou `MockMvc` + `standaloneSetup`.

**Handlers a testar (3/9 existentes):**
- ✅ `UserNotFoundException` → 404 (já testado)
- ✅ `InvalidCredentialsException` → 401 (já testado)
- ✅ `UserAlreadyExistsException` → 409 (já testado)
- ❌ `MissionLockedException` → 403 com `code: MISSION_LOCKED`
- ❌ `LevelRequiredException` → 403 com `code: LEVEL_REQUIRED`
- ❌ `AccessDeniedException` → 403
- ❌ `ThemeNotFoundException` → 400
- ❌ `MethodArgumentNotValidException` → 400
- ❌ `Exception` genérico → 500 (mensagem sanitizada)

---

### 2.4 `SecurityConfig` integration test

**Motivação:** `TestSecurityConfig` substitui segurança real por `permitAll()`. As regras de autorização nunca são validadas.

**O que fazer:**
1. Criar `SecurityIntegrationTest` com `@SpringBootTest(webEnvironment = RANDOM_PORT)`
2. Testar sem `TestSecurityConfig`
3. Usar `TestRestTemplate` ou `MockMvc` com `@AutoConfigureMockMvc`
4. Cenários:
   - `POST /api/auth/register` → 200 (sem auth)
   - `POST /api/auth/login` → 200 (sem auth)
   - `GET /api/missions` → 401 (sem token)
   - `POST /api/missions` → 403 (token USER)
   - `POST /api/missions` → 200 (token ADMIN)

---

### 2.5 `JwtAuthFilterTest`

**O que fazer:**
1. Usar `MockHttpServletRequest` / `MockHttpServletResponse`
2. Testar:
   - Token válido → `SecurityContext` populado com userId e role
   - Token inválido → `SecurityContext` não populado
   - Sem header → `SecurityContext` não populado
   - Header malformado (sem "Bearer ") → `SecurityContext` não populado

---

### 2.6 Service tests faltantes

- `GetProfileServiceTest`
- `GetAdminScenariosServiceTest`
- `GetAdminMissionsServiceTest`

---

## 📋 Fase 3 — Qualidade de Código

### 3.1 N+1 fix em `MissionPersistenceAdapter.save()`

**Problema:** Para cada técnica na missão, uma query `findByName()`.

**Solução:** Buscar todas as técnicas em lote.

```java
// Antes
for (Technique domainTechnique : mission.getTechniques()) {
    TechniqueJpaEntity entity = techniqueJpaRepository.findByName(domainTechnique.getName()).orElseThrow(...);
}

// Depois
Set<String> names = mission.getTechniques().stream().map(Technique::getName).collect(toSet());
List<TechniqueJpaEntity> entities = techniqueJpaRepository.findByNameIn(names);  // novo método no repositório
```

**Arquivos:**
- `infrastructure/adapter/out/persistence/repository/TechniqueJpaRepository.java` — adicionar `findByNameIn`
- `infrastructure/adapter/out/persistence/MissionPersistenceAdapter.java` — usar batch

**Verificação:** `mvn test`

---

### 3.2 Separar `MissionRepository` God Port

**Problema:** 18 métodos em uma única interface.

**Solução:** Dividir em 3 interfaces focadas:

```
MissionQueryPort          — findById, findAll, findAllById, findByTheme, findByDifficulty,
                            findByThemeAndDifficulty, findByScenarioIdOrderByOrderIndex,
                            findByScenarioIdInOrderByOrderIndex, findByEnabledTrue,
                            countByScenarioId, countByScenarioIdAndEnabledTrue
MissionCommandPort        — save, deleteById, setEnabledByScenarioId, setOrderIndex
MissionValidationPort     — isPreviousMissionCompleted, existsByScenarioIdAndEnabledFalse,
                            findScenarioIdsWithDisabledMissions
```

Implementação única em `MissionPersistenceAdapter` (que implementa as 3). Use cases importam apenas a interface necessária.

**Arquivos afetados:**
- `application/port/out/MissionQueryPort.java` — **novo**
- `application/port/out/MissionCommandPort.java` — **novo**
- `application/port/out/MissionValidationPort.java` — **novo**
- `application/port/out/MissionRepository.java` — remover (ou manter como compatibilidade)
- `infrastructure/adapter/out/persistence/MissionPersistenceAdapter.java` — implementar as 3
- Todos os use cases que injetam `MissionRepository` — trocar pela interface correta

**Verificação:** `mvn compile` + `mvn test`

---

### 3.3 `@Enumerated(STRING)` em `UserJpaEntity.role`

```java
@Enumerated(EnumType.STRING)
@Column(length = 20)
private UserRole role;
```

**Arquivo:** `infrastructure/adapter/out/persistence/entity/UserJpaEntity.java`

---

### 3.4 Criar builders de teste compartilhados

**Problema:** `Mission.builder()` com 15+ campos repetido em ~15 arquivos. `new Theme(UUID.randomUUID(), "ASTRONOMY", null, null)` aparece em todo lugar.

**O que fazer:**
1. `TestMissions.java` — métodos estáticos para missões comuns (válida, sem técnicas, desabilitada, etc.)
2. `TestUsers.java` — `user()`, `admin()`
3. `TestThemes.java` — temas fixos pré-definidos
4. `TestConstants.java` — UUIDs fixos para userId, missionId, scenarioId

**Local:** `src/test/java/com/sqlab/shared/`

---

### 3.5 `matchesOrdered` — otimizar checagem redundante

**Problema:** `ExpectedTuple.matchesOrdered()` faz um `allMatch`/`anyMatch` completo (O(n*m)) **antes** da comparação posicional. O erro específico "Rows are correct but in wrong order" é valioso para o usuário e deve ser preservado.

**O que fazer:** Otimizar a implementação mantendo a mensagem de erro distinta. Extrair a comparação posicional para um método reutilizável e evitar a varredura dupla quando os dados não correspondem.

---

## 🔵 Fase 4 — Melhorias

### 4.1 DTO mappers ausentes

- Criar `UserDtoMapper`, `MissionDtoMapper`, `ScenarioDtoMapper` (consistência com `ThemeDtoMapper` e `TechniqueDtoMapper`)
- Nos controllers, usar os mappers em vez de construtores inline

### 4.2 Remover DTOs não utilizados

- `AuthDto.UserResponse`
- `AuthDto.TokenResponse`

### 4.3 Login retorna dados do usuário

- `AuthController.login()` deve retornar `id`, `username`, `email` completos (não `null`)
- **Abordagem:** Alterar `AuthenticateUserUseCase.AuthResult` para incluir `id`, `username`, `email` — o service já tem acesso ao `User` após autenticação, basta mapear no objeto de retorno. Isso evita lookup extra no controller.

### 4.4 Documentar `equals/hashCode` de `Theme`/`Technique`

Adicionar comentário: `equals/hashCode baseado em name (business key) — id NÃO é considerado`

### 4.5 Unificar mensagens de exceção

- Todas as exceções de domínio em português (já que o público é brasileiro)
- Ou todas em inglês (consistência técnica)

### 4.6 `@PostConstruct` → validação eager em `JwtTokenProvider`

**Problema:** `validateConfig()` roda em `@PostConstruct`, validando a chave **depois** da criação do bean. Além disso, a descrição "antes de criar a chave" é inviável — o método lê `key.getEncoded().length` (a chave já precisa existir).

**O que fazer:**
- Validar o comprimento do `secret` **raw** no construtor (antes de criar a `SecretKey`): `secret.getBytes(StandardCharsets.UTF_8).length >= 32`
- Remover `@PostConstruct validateConfig()` — a validação no construtor é eager e elimina a necessidade do lifecycle callback

---

## Resumo de Esforço

| Fase | Tarefas | Esforço Estimado | Riscos |
|------|---------|-----------------|--------|
| 🔴 Fase 1 | 5 correções arquiteturais | 10-12h | Moderado — afeta controllers e use cases |
| 🟡 Fase 2 | 6 grupos de testes | 14-18h | Baixo — testes novos não quebram existentes |
| 📋 Fase 3 | 5 melhorias de código | 8-10h | Baixo a moderado — God Port impacta muitos arquivos |
| 🔵 Fase 4 | 6 melhorias | 4-6h | Baixo — cosméticas e documentação |
| **Total** | **22 tarefas** | **36-46h** | |

---

## Critérios de Sucesso

- [ ] `mvn test` — 0 falhas (mantido)
- [ ] Nenhum controller injeta portas de saída
- [ ] Nenhuma classe de domínio depende de framework externo
- [ ] `MissionAccessValidator` com cobertura ≥ 90%
- [ ] `GlobalExceptionHandler` com cobertura ≥ 90%
- [ ] `SecurityConfig` testado com regras reais
- [ ] `MissionPersistenceAdapter.save()` sem N+1
- [ ] `MissionRepository` com no máximo 8-10 métodos
- [ ] `ErrorResponse` único e consistente para todos os HTTP status

---

## Notas Técnicas

- Projeto usa Java 25, Spring Boot 4.0.5, Hibernate 7.2.7
- Testcontainers 1.20.4 com fallback H2 (MODE=PostgreSQL)
- `mvnw.cmd` para build (Maven Wrapper)
- JUnit 5 com Mockito e AssertJ
- Flyway para migrations (sem `ddl-auto`)
