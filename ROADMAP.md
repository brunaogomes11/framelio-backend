# 🗺️ Roadmap Técnico — Framelio (Backend)

> Mapa de **gaps, débitos técnicos, correções e bugs**, com **foco em segurança**, do
> `photographer-manager` (Spring Boot 4 / Java 25 / PostgreSQL / JWT + OAuth2).
> Gerado em **2026-06-19** por auditoria de código. As IDOR/segurança foram identificadas via
> leitura dos controllers/services; o erro `GET /client/galleries` foi confirmado **ao vivo** no dev.

> Roadmap do frontend: ver `ROADMAP.md` em `agenda-photos`.

## Resumo
A base está organizada (camadas Entity→Repository→Service→Controller, DTOs validados, exception
handler global). Porém há **várias falhas críticas de segurança** que impedem ir para produção como
está: IDOR em múltiplos endpoints, CORS permissivo com credenciais, webhook de pagamento sem
verificação de assinatura e endpoints de usuário abertos.

## Legenda de prioridade
| | Significado |
|---|---|
| **P0** | Crítico — segurança. Bloqueia produção. |
| **P1** | Alto — segurança/infra/dados. |
| **P2** | Médio — robustez, performance, observabilidade. |
| **P3** | Baixo — limpeza. |

---

## P0 — Crítico (segurança)

### 1. IDOR — leitura de recursos de outros usuários por ID
- **Problema:** vários `GET /{id}` retornam o recurso **sem checar se o principal autenticado é o dono**.
  Um usuário logado lê eventos, galerias, vendas, times e dados de outros usuários só trocando o ID.
- **Evidência (snapshot):** `EventController:75`, `GalleryController:33`, `SaleController:68`,
  `TeamController:50`, `UserController:50` — `findById` sem validação de ownership no service.
- **Correção:** validar `recurso.ownerId == principal.id` no service (ou `@PreAuthorize`), retornando
  403/404 quando não for o dono. Aplicar também a update/delete onde faltar.

### 2. CORS curinga com credenciais
- **Problema:** origem `*` combinada com `allowCredentials(true)` — qualquer site faz requisições
  autenticadas em nome do usuário.
- **Evidência:** `config/CorsConfig.java:19,33`.
- **Correção:** allowlist explícita de origens (dev/prod) e métodos; sem curinga quando há credenciais.

### 3. Webhook MercadoPago sem verificação de assinatura
- **Problema:** o webhook aceita qualquer POST como confirmação de pagamento (dá para **forjar** um
  pagamento aprovado e liberar o pedido) e ainda **engole exceções retornando 200**, escondendo falhas.
- **Evidência:** `domain/store/WebhookController.java:22-43`.
- **Correção:** validar a assinatura do MercadoPago (header `x-signature`), processar de forma
  idempotente e logar/retornar erro adequado em falha.

### 4. Endpoints de usuário abertos
- **Problema:** `listAll`, `findById`, `update`, `delete` de usuários sem autorização → enumeração de
  e-mails/dados e edição/exclusão de **qualquer** conta por qualquer usuário autenticado.
- **Evidência:** `UserController.java:38-71`.
- **Correção:** restringir `listAll` a ADMIN; `findById/update/delete` ao próprio usuário ou ADMIN.

---

## P1 — Alto

### 5. OAuth2 cria usuário automaticamente sem verificação
- **Problema:** qualquer conta Google se auto-registra como `CLIENT`, sem verificação/aprovação.
- **Evidência:** `security/oauth2/CustomOAuth2UserService.java:28-40`.
- **Correção:** definir política (verificar e-mail, escolher perfil no primeiro acesso, etc.).

### 6. JWT trafega no query param do redirect OAuth
- **Problema:** o token volta como query string → vaza em histórico do navegador, logs de servidor e header `Referer`.
- **Evidência:** `security/oauth2/OAuth2SuccessHandler.java:35-37`.
- **Correção:** usar fragment (`#`) ou POST + troca por cookie/localStorage no callback.

### 7. Schema gerenciado por `ddl-auto=update` (sem migrations)
- **Problema:** alterações automáticas de schema em produção → risco de perda de dados/inconsistência.
- **Evidência:** `src/main/resources/application.properties:11`.
- **Correção:** adotar **Flyway/Liquibase** e mudar para `validate`.

### 8. Upload de fotos sem validação + servido inline
- **Problema:** `upload` aceita qualquer `MultipartFile` (sem limite de tamanho, sem checar
  content-type/extensão) e os arquivos são servidos com `Content-Disposition: inline` (risco de XSS via
  polyglot HTML/JS) e sem rate limit (DoS de disco).
- **Evidência:** `domain/gallery/PhotoController.java:26-32`, `config/storage/StorageController.java:49`.
- **Correção:** validar MIME/extensão/tamanho (allowlist de imagens), servir como `attachment` (ou via
  CDN/domínio separado), aplicar limites de upload.

### 9. Path canônico das galerias do cliente (alinhar com o frontend)
- **Problema:** backend expõe `GET /galleries/client` (`GalleryController`), mas o frontend chama
  `GET /client/galleries` → falha (confirmado 500 no dev), quebrando a área do cliente.
- **Correção:** definir **um** path canônico. Recomendado: ajustar o frontend para `/galleries/client`
  (ou adicionar alias no backend), e garantir que o handler não estoure 500 com lista vazia.

---

## P2 — Médio (robustez / performance / observabilidade)

### 10. Configurações inseguras/ruidosas
- `spring.jpa.show-sql=true` em produção loga SQL com dados sensíveis → desligar fora de dev.
- `SessionCreationPolicy.IF_REQUIRED` (`SecurityConfig`) numa API por token → usar `STATELESS`.
- **Evidência:** `application.properties:12-13`, `config/SecurityConfig.java:52-53`.

### 11. Exception handler vaza mensagem interna
- **Problema:** o handler genérico retorna `e.getMessage()` para `Exception` não tratada (pode vazar detalhe interno).
- **Evidência:** `exception/GlobalExceptionHandler.java:54-59`.
- **Correção:** mensagem genérica + log server-side com correlação.

### 12. Armazenamento em disco local
- **Problema:** fotos no filesystem local não funcionam com múltiplas instâncias/containers efêmeros.
- **Evidência:** `config/storage/LocalStorageService.java` (existe `S3StorageService`, mas não é o default).
- **Correção:** usar S3/compatível em produção (perfil de config).

### 13. Listagens sem paginação e índices
- **Problema:** endpoints de listagem retornam coleções ilimitadas (memória) e provavelmente faltam
  índices em FKs/colunas de filtro (ex.: `photographerId`).
- **Correção:** paginar (`Pageable`) e adicionar índices.

### 14. JWT — claims e expiração
- **Problema:** sem `iss`/`aud`/`jti`; expiração de 24h sem refresh token (token roubado vale o dia todo).
- **Evidência:** `security/JwtService.java`, `application.properties` (`JWT_EXPIRATION_MS`).
- **Correção:** claims padrão, expiração curta + refresh token (e caminho de revogação).

### 15. Cobertura de testes mínima
- **Problema:** ~2 classes de teste. Sem testes de autorização/IDOR nem do fluxo de pagamento.
- **Correção:** testes de segurança (acesso cruzado deve falhar) e de webhook/checkout.

---

## P3 — Baixo (limpeza)
- Roles como string literal em `@Secured({"ROLE_AGENCY","ROLE_ADMIN"})` (`AgencyController`) → constantes/enum.
- Possível duplicação de queries entre repositories → base comum.

---

## Gaps de produto
- **Onboarding de AGÊNCIA:** existe o domínio/endpoints de agência, mas o cadastro público só cria
  PHOTOGRAPHER/CLIENT — definir como uma conta AGENCY é criada.
- **Apple OAuth** (só Google hoje).
- **Chat realtime** (hoje polling no frontend; avaliar WebSocket/SSE).

---

## Sugestão de sequência
1. **P0 inteiro** antes de qualquer deploy "real" (IDOR, CORS, webhook, usuários).
2. **P1** #7 (migrations) e #8 (upload) o quanto antes; #5/#6 (OAuth) e #9 (path) em seguida.
3. **P2** observabilidade/performance; **P3** quando houver folga.

## Como verificar (após corrigir)
- IDOR: com 2 contas (`qa.ph.*` e `qa.cl.*` já existem no dev), tentar acessar recursos da outra por ID → deve dar 403/404.
- Webhook: POST sem assinatura válida → deve ser rejeitado.
- `/galleries/client`: responder 200 (lista vazia inclusive), sem 500.

> **Nota:** referências `arquivo:linha` vêm de um retrato automatizado de 2026-06-19 — confirmar no
> código atual antes de editar.
