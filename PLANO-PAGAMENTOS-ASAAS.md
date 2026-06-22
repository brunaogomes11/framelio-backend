# Plano — Pagamentos: abstração de gateway + Asaas split (primeiro provedor)

## Context

Hoje o checkout fala **direto com o Mercado Pago** (`StoreService` → API `/v1/orders`), com dois problemas de fundo:

1. **Modelo de custódia (risco regulatório):** o dinheiro de todas as vendas cai na conta da plataforma; o valor do fotógrafo é só um número interno (`PhotographerBalance`) e o repasse é **PIX manual** (`WithdrawalRequest`). Segurar dinheiro de terceiros e repassar aproxima a plataforma de "instituição de pagamento", além de carregar o risco de chargeback.
2. **Webhook inseguro (P0 do `ROADMAP.md` #3):** `WebhookController` aceita qualquer POST como confirmação de pagamento e engole exceções retornando 200.

**Decisões tomadas com o usuário** (junho/2026):
- Adotar **split nativo** com **subconta criada por nós via API** → o dinheiro cai direto na subconta do fotógrafo (sem custódia).
- Público-alvo são **fotógrafos iniciantes** (provavelmente **sem conta Mercado Pago**) → onboarding via OAuth do MP é atrito; subconta-criada-por-nós (Asaas) não é.
- Aceitar **PIX + cartão** desde já.
- Modelo de recebimento: **acumula na subconta + saque mínimo**; janela contra estorno coberta naturalmente pela liquidação de cartão (~D+30) do Asaas.
- Foto pode custar **R$5** (ticket baixo). Asaas cobra **PIX por taxa fixa (R$1,99)** → resolver venda pequena com **pedido mínimo** (configurável).
- Arquitetura: **abstração de gateway**, começando pelo **Asaas**; MP entra depois atrás da mesma interface (sem reescrever o checkout).
- **AbacatePay descartado para o núcleo:** não tem split nem subcontas, e o saque só sai **dia 20/mês com teto de R$5k** — inviável como espinha dorsal de marketplace.

**Resultado pretendido:** checkout e repasse desacoplados do provedor, com Asaas implementado primeiro (split + subconta + saque), webhook seguro e idempotente, e sem custódia de dinheiro de terceiros.

---

## Estratégia de altitude

- O `StoreService.createCheckout` passa a depender de **interfaces**, não do MP. As 3 responsabilidades que divergem entre provedores ficam em interfaces separadas (evita interface "Frankenstein"):
  1. `PaymentGateway` — cobrança, status, parse de webhook.
  2. `SellerAccountProvider` — onboarding do recebedor (Asaas: subconta; MP futuro: OAuth).
  3. `PayoutProvider` — saldo da subconta + saque (transferência PIX).
- O código MP atual **não é apagado**; serve de referência para um futuro `MpGateway`. Apenas deixa de ser chamado pelo checkout.

---

## Mudanças (recomendado)

### 1. Nova abstração — pacote `domain/store/payment`
- `PaymentGateway` (interface):
  - `ChargeResult createCharge(ChargeCommand cmd)` → `{chargeId, status, pixQrCode, pixCopyPaste, paymentUrl}`
  - `ChargeStatus getCharge(String chargeId)`
  - `WebhookEvent parseWebhook(Map<String,String> headers, Map<String,Object> body)`
  - `void refund(String chargeId)`
- `SellerAccountProvider` (interface):
  - `SellerAccount createSeller(User photographer)`  // Asaas: cria subconta
  - `SellerStatus getSellerStatus(String sellerRef)`
- `PayoutProvider` (interface):
  - `ProviderBalance getBalance(String sellerRef)`    // disponível vs pendente
  - `PayoutResult transfer(String sellerRef, BigDecimal amount, String pixKey, String pixKeyType)`
- DTOs imutáveis (records): `ChargeCommand`, `ChargeResult`, `WebhookEvent`, `SellerAccount`, `ProviderBalance`, etc.
- Implementações Asaas: `AsaasGateway`, `AsaasSellerAccountProvider`, `AsaasPayoutProvider` (HTTP via `HttpClient`, reaproveitando o padrão `sendMercadoPago` de `StoreService` como base para um `AsaasClient` comum: base-url, header `access_token`, tratamento de erro).

### 2. Config — `config/asaas/AsaasConfig.java` (espelha `MercadoPagoConfig`)
- `application.properties` (novos):
  - `asaas.base-url=${ASAAS_BASE_URL:https://api-sandbox.asaas.com/v3}`
  - `asaas.api-key=${ASAAS_API_KEY}` (conta-raiz CNPJ)
  - `asaas.wallet-id=${ASAAS_WALLET_ID}` (walletId da plataforma p/ receber a comissão no split)
  - `asaas.webhook-token=${ASAAS_WEBHOOK_TOKEN}` (validação do webhook)
  - `store.min-order-amount=15.00` (novo — pedido mínimo)
  - `store.commission-rate=0.06` (existente)
- `.env`: adicionar `ASAAS_API_KEY`, `ASAAS_WALLET_ID`, `ASAAS_WEBHOOK_TOKEN`, `ASAAS_BASE_URL`.

### 3. Entidade `User` (`domain/usuario/User.java`) — dados da subconta
Adicionar campos (seguindo o padrão de comentário `ALTER TABLE ... IF NOT EXISTS` já usado no arquivo, pois o schema é `ddl-auto=update`):
- `asaasAccountId`, `asaasWalletId`, `asaasApiKey` (**criptografado em repouso**), `asaasOnboardingStatus` (PENDING|APPROVED|REJECTED), `asaasOnboardingUrl`.
- A `asaasApiKey` da subconta só é retornada **uma vez** na criação → persistir imediatamente e nunca expor em DTO.

### 4. Onboarding do fotógrafo
- `SellerOnboardingService` (ou métodos em `StoreService`): ao **habilitar loja** (`enableStore`) ou no cadastro, chamar `sellerAccountProvider.createSeller(user)` e gravar walletId/accountId/apiKey/onboardingUrl/status.
- Endpoint: `GET /photographer/store/onboarding` → retorna status do KYC + link (`onboardingUrl`) quando pendente.
- Regra: **só permitir vender (checkout) quando `asaasOnboardingStatus = APPROVED`** (KYC ~48h). `enableStore` pode iniciar o onboarding mas marcar a galeria como "aguardando aprovação".

### 5. Checkout — reescrever `StoreService.createCheckout`
- Validar `subtotal >= store.min-order-amount` (resolve o R$5 avulso).
- Validar que o fotógrafo está `APPROVED`.
- Montar **split**: walletId do fotógrafo recebe `subtotal − platformFee`; walletId da plataforma recebe `platformFee` (= `subtotal * commissionRate`). Atenção: Asaas desconta a taxa dele **off-top** e calcula o split sobre o **líquido** — alinhar a base do split (ver "Decisões em aberto").
- PIX: `billingType=PIX` → obter QR Code + copia-e-cola.
- Cartão: `billingType=CREDIT_CARD` com `creditCardToken`, `holderInfo` e `installmentCount`.
- Persistir refs no `Order` (ver item 7).
- **Remover o crédito de saldo interno** em `markOrderPaid` — o split já depositou na subconta. `markOrderPaid` mantém só: status PAID, `paidAt`, emissão do `DownloadToken` e e-mail.

### 6. Webhook — `WebhookController` + handler
- Novo endpoint `POST /webhook/asaas`.
- **Validar token** do header `asaas-access-token` contra `asaas.webhook-token`; rejeitar (401) se inválido → **corrige ROADMAP P0 #3**.
- **Idempotência:** registrar `event.id` já processado (tabela/coluna) e ignorar repetidos.
- Eventos: `PAYMENT_RECEIVED`/`PAYMENT_CONFIRMED` → `markOrderPaid`; `PAYMENT_REFUNDED`/`PAYMENT_CHARGEBACK` → `Order.status=REFUNDED` + revogar `DownloadToken`.
- Parar de engolir exceção como 200 cego: logar e retornar status adequado (200 só em evento tratado/ignorado conscientemente).

### 7. Entidade `Order` (`domain/store/order/Order.java`) — refs neutras
- Adicionar campos genéricos: `paymentProvider` (ex.: "ASAAS"), `gatewayChargeId`, `pixQrCode`, `paymentUrl`.
- Manter os campos `mp*` existentes (deprecados) para não quebrar dados; novas vendas usam os genéricos.
- `status` já contempla `PENDING|PAID|EXPIRED|REFUNDED`.

### 8. Saldo e saque
- `getBalance` → consultar `payoutProvider.getBalance(walletId)` (fonte da verdade: disponível vs pendente já refletindo liquidação D+30 do cartão = **a "janela" sai de graça**). `PhotographerBalance.totalEarned` pode virar só espelho de analytics atualizado por webhook (ou ser descontinuado).
- `requestWithdrawal` → manter mínimo (`WithdrawalPayload` já tem `@DecimalMin("20.00")`); chamar `payoutProvider.transfer(...)` (PIX da subconta p/ a chave do fotógrafo); gravar `WithdrawalRequest` com id/status da transferência; atualizar via webhook de transferência.
- **Remover** a lógica de subtrair de `availableAmount` interno (não há mais custódia).

---

## Arquivos principais

- Novos: `domain/store/payment/` (interfaces + DTOs + impls Asaas), `config/asaas/AsaasConfig.java`, `AsaasClient`.
- Alterados: `domain/store/StoreService.java` (checkout/saldo/saque/markOrderPaid), `domain/store/WebhookController.java`, `domain/store/order/Order.java`, `domain/usuario/User.java`, `domain/store/PhotographerStoreController.java` (endpoint de onboarding), `application.properties`, `.env`.
- Referência (não chamado): `config/mercadopago/MercadoPagoConfig.java` e trechos MP de `StoreService` (base p/ `MpGateway` futuro).

---

## Decisões em aberto (confirmar na implementação)

1. **Base do split / quem absorve a taxa Asaas:** comissão de 6% sobre bruto ou sobre o líquido (pós-taxa Asaas)? Recomendado: platformFee = 6% do subtotal; taxa Asaas sai off-top e reduz o valor do fotógrafo.
2. **Pedido mínimo:** valor inicial `R$15` (ajustável). Avaliar também repasse opcional da taxa ao comprador no futuro.
3. **Bloqueio por KYC:** confirmar que vendas só são permitidas após `APPROVED` (recomendado).
4. **Criptografia da `asaasApiKey`:** definir mecanismo (ex.: Jasypt / coluna cifrada).

---

## Verificação (end-to-end, sandbox Asaas)

1. **Onboarding:** habilitar loja → subconta criada → `GET /photographer/store/onboarding` retorna link; simular aprovação KYC no sandbox → status `APPROVED`.
2. **Pedido mínimo:** checkout com subtotal < `store.min-order-amount` → 400.
3. **PIX:** checkout PIX → retorna QR/copia-e-cola; pagar no sandbox → webhook `PAYMENT_RECEIVED` → `Order` vira PAID, `DownloadToken` emitido + e-mail; split aparece na subconta do fotógrafo.
4. **Cartão:** checkout com token de cartão de teste → aprovado → saldo entra como pendente (liquidação D+30).
5. **Webhook seguro:** POST sem header `asaas-access-token` válido → **401** (não libera pedido).
6. **Estorno:** disparar refund no sandbox → `Order` REFUNDED + `DownloadToken` revogado + split revertido.
7. **Saque:** com saldo disponível ≥ mínimo → `POST /photographer/store/withdrawal` → transferência PIX criada; abaixo do mínimo → rejeitado.
8. **Testes:** unitários para cálculo do split, validação de pedido mínimo e rejeição de webhook com token inválido.

> Schema é gerenciado por `ddl-auto=update` (sem Flyway — ROADMAP #7); as colunas novas sobem automaticamente. Migrar para Flyway está fora do escopo deste plano.

---

## Comparação de gateways (registro da decisão)

| Critério | **Asaas (escolhido)** | MP split | AbacatePay |
|---|---|---|---|
| Split nativo | ✅ | ✅ | ❌ (em desenvolvimento) |
| Subconta criada por nós | ✅ (BaaS, conta-raiz CNPJ) | ❌ (exige OAuth do fotógrafo) | ❌ não tem |
| Taxa PIX | R$1,99 fixo | 0,99% | R$0,80 fixo |
| Taxa cartão | ~3,5% + R$0,49 | 4,98% na hora / 3,98% 30d | até 12x |
| Saque | a qualquer momento | split instantâneo | **só dia 20/mês, teto R$5k** |
| Fit p/ iniciante sem conta | ✅ | ❌ | — |
| Risco de custódia | ✅ eliminado | ✅ eliminado | ❌ (centralizado) |
