# MR: Padronização e Refatoração de Nível Pleno/Sênior — CertifAI Backend

> **Branch de Origem:** `chore/portfolio-pleno-standardization`  
> **Branch de Destino:** `main`  
> **Tipo de Mudança:** `chore` / `refactor` / `test` / `ci` / `docs`  
> **Status dos Testes:** 104/104 Aprovados (100% de sucesso)

---

## 📌 Contexto & Motivação

Este Merge Request consolida uma refatoração técnica de ponta a ponta no repositório `certifai-backend`, elevando a maturidade do projeto para padrões de engenharia **Pleno/Sênior** avaliados por Tech Leads e recrutadores técnicos.

Foram abordados pontos críticos de governança de código, sanitização de segurança, tratamento centralizado de exceções, observabilidade com logs estruturados, autonomia e cobertura de testes automatizados, conteinerização com boas práticas de segurança (Alpine + non-root) e pipeline de integração contínua (CI).

---

## 🚀 Resumo das Alterações por Pilar

### 1. Governança de Git & Segurança (`chore`)
- **Sanitização de Segurança Crítica:** Detectado e expurgado diretório anômalo em `src/main/java/.../public/fonts/` que continha script JavaScript obstruso (`fa-solid-900.woff2`) disparado automaticamente na abertura do editor via `.vscode/tasks.json`.
- **Higiene de IDE e Rastreamento:** Desvinculados arquivos de IDE (`.vscode/`) e artefatos do Git.
- **`.gitignore` Robusto:** Atualizado com regras para Java 21, Spring Boot, Maven, Gradle, IDEs, arquivos de sistema e variáveis de ambiente sensíveis.
- **Template Seguro (`.env.example`):** Criado template com todas as variáveis requeridas pela aplicação (`DATASOURCE_*`, `ADMIN_*`, `JWT_SECRET`, `SENDGRID_*`, `CLIENT_*`), documentadas com valores fictícios seguros.

### 2. Arquitetura, Tratamento Global de Exceções & Observabilidade (`refactor`)
- **Global Exception Handler Centralizado (`@RestControllerAdvice`):**
  - Padronização no formato JSON estruturado ([`ErrorDetails`](file:///home/mayara.eufrasio/Área%20de%20trabalho/Workspace/certifai-backend/src/main/java/br/com/certifai/exception/config/ErrorDetails.java)) contendo `timestamp`, `status`, `error`, `message`, `path` e detalhamento campo a campo para falhas de validação (`errors`).
  - Cobertura explícita para `EntidadeNaoEncontradaException` (404), `ConflitoException` (409), `MethodArgumentNotValidException` (400), `ConstraintViolationException` (400), `IllegalArgumentException` / `IllegalStateException` (400), `BadCredentialsException` (401), `AccessDeniedException` (403) e exceções internas genéricas (500).
- **Substituição de Saídas de Console por Logging Estruturado:**
  - Removidos todos os `System.out.println` e `System.err.println` em controllers e services (`ChecklistController`, `ChecklistService`, `EmailService`), substituídos por `@Slf4j` com níveis semânticos apropriados.
- **OpenAPI / Swagger 3.0 Corrigido:**
  - Corrigidos título, descrição e contatos que continham dados de template de barbearia.
  - Documentação completa via `@Tag` em todas as interfaces de API (`AuthApi`, `CertificacaoApi`, `ProvaApi`, `QuestaoApi`, `ChecklistApi`, `ItemChecklistApi`, `EventoApi`, `UsuarioApi`).
  - Configurado esquema de segurança JWT Bearer global e agrupamentos de endpoints funcionais.
- **Refatoração de Mappers e Entidades:**
  - Eliminados todos os warnings do MapStruct (`IUsuarioMapper`, `IItemChecklistMapper`, `IProvaMapper`, `IChecklistMapper`).
  - Corrigido construtor de `LoginDTO` e adicionado `@Builder.Default` em entidades Lombok para evitar defaults ignorados.

### 3. Autonomia da Suíte de Testes & Cobertura (`test`)
- **Banco de Dados In-Memory (H2 Database):**
  - Adicionada dependência do H2 em escopo de teste e criado `src/test/resources/application.properties` configurado para emulação PostgreSQL (`MODE=PostgreSQL`).
  - Os testes de integração via `MockMvc` agora rodam 100% de forma isolada e determinística, sem necessidade de PostgreSQL externo ou conexões de rede ativas.
- **Correção dos Testes Quebrados:**
  - Corrigido mock e extração de usuário logado em `AuthService.getPrincipal()` e `AuthServiceTest`.
  - Corrigida a ordem de limpeza de foreign keys e seeding de dados de questões em `ProvaControllerIntegrationTest`.
- **Novas Suítes de Testes Unitários de Regra de Negócio:**
  - **`ChecklistServiceTest`:** 9 testes unitários cobrindo criação, validações de regra de negócio, busca, atualização e deleção com padrão AAA.
  - **`ItemChecklistServiceTest`:** 9 testes unitários cobrindo ciclo de vida do item, preenchimento automático da data de conclusão e vinculação/desvinculação de eventos.
- **Métrica Final:** **104 testes executados com 100% de aprovação (0 falhas, 0 erros).**

### 4. Conteinerização & CI/CD (`ci`)
- **`Dockerfile` Multi-Stage Otimizado:**
  - **Builder:** `eclipse-temurin:21-jdk-alpine`, com cache de camadas para dependências Maven (`dependency:go-offline`).
  - **Runner:** `eclipse-temurin:21-jre-alpine` (imagem enxuta), usuário não-root dedicado (`appuser:appgroup`), healthcheck integrado e flags JVM ajustadas para limites de memória de containers (`-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`).
- **Orquestração `docker-compose.yml`:**
  - Serviços `postgres` (PostgreSQL 16 com healthcheck `pg_isready`) e `backend` com inicialização sincronizada (`service_healthy`).
- **Pipeline GitHub Actions (`.github/workflows/ci.yml`):**
  - Execução automática em push e PR para branches principais com JDK 21 Temurin, cache de dependências Maven, compilação, suíte de 104 testes e geração de artefato JAR.

### 5. Documentação Técnica Executiva (`docs`)
- **`README.md` Completo:**
  - Badges de status, linguagem, framework e licença.
  - Descrição da dor de negócio resolvida pela plataforma CertifAI.
  - Diagrama de arquitetura em camadas e segurança em **Mermaid.js**.
  - Discussão técnica de trade-offs arquiteturais.
  - Guia rápido de execução (Docker Compose vs. Execução Nativa).
  - Tabela de endpoints REST e link do Swagger UI.
  - Guia de execução dos testes.

---

## 📦 Commits Deste MR

```text
f3bfa0a docs(readme): rewrite README with architecture diagrams, technical decisions and setup guide
082f2da ci(workflow): add multi-stage Dockerfile, docker-compose and GitHub Actions CI pipeline
ed7a0f8 test(service): add and fix unit and integration tests for core business flows
1f39982 refactor(core): standardize exception handling, logging and API documentation
eb02454 chore(config): sanitize tracked files, update .gitignore and add env template
```

---

## 🔍 Como Validar Localmente

### 1. Validar a Suíte Completa de Testes
```bash
./mvnw clean test
```
*Resultado esperado: `Tests run: 104, Failures: 0, Errors: 0, Skipped: 0` -> `BUILD SUCCESS`.*

### 2. Validar a Execução Conteinerizada
```bash
docker compose up --build
```
*Acesse o Swagger UI para testar os endpoints interativamente:*
`http://localhost:8080/certifai/swagger-ui.html`

---

## ✅ Checklist de Qualidade

- [x] Árvore de trabalho limpa (`git status` limpo).
- [x] Commits atômicos no padrão Conventional Commits em inglês.
- [x] Nenhum segredo ou credencial sensível no código-fonte.
- [x] 100% dos testes unitários e de integração passando.
- [x] Build do container Docker validado com usuário não-root.
- [x] Pipeline de CI configurado e validado.
- [x] Documentação técnica completa e atualizada no `README.md`.
