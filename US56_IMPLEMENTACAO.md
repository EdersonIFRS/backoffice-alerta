# US#56 - Alertas Inteligentes via Slack / Microsoft Teams

## 📋 Visão Geral

Sistema de notificações de alertas de risco via webhooks do Slack e Microsoft Teams.

**Status:** ✅ COMPLETO  
**Tipo:** READ-ONLY  
**RBAC:** ADMIN, RISK_MANAGER

## 🎯 Objetivos

1. ✅ Enviar alertas de risco para Slack/Teams via webhook
2. ✅ Filtrar alertas por severidade (ignorar INFO)
3. ✅ Formatar mensagens com contexto e recomendações
4. ✅ Fallback seguro em caso de erros
5. ✅ Logs estruturados com emojis
6. ✅ Disclaimer obrigatório
7. ✅ Não persistir dados (in-memory)

## 🏗️ Arquitetura

### Componentes Criados

#### 1. Enums (`alerts.notification`)
- **NotificationChannel**: `SLACK`, `TEAMS`
- **NotificationStatus**: `SENT`, `FAILED`, `SKIPPED`

#### 2. DTOs (`alerts.notification`)
- **RiskAlertNotificationRequest**: Dados de entrada (channel, webhookUrl)
- **RiskAlertNotificationResponse**: Resposta com status e timestamps

#### 3. Interface Strategy (`alerts.notification`)
- **AlertNotifier**: Interface para implementações plugáveis
  - `NotificationStatus send(RiskMetricAlert alert, String webhookUrl)`
  - `NotificationChannel getChannel()`

#### 4. Implementações (`alerts.notification.impl`)
- **SlackAlertNotifier**: Webhook Slack com Markdown
- **TeamsAlertNotifier**: Webhook Teams com MessageCard

#### 5. Serviço (`alerts.notification`)
- **RiskAlertNotificationService**: Orquestração e lógica de negócio

#### 6. Controller (`alerts.notification`)
- **RiskAlertNotificationController**: Endpoints REST

## 📡 Endpoints

### POST /risk/alerts/notify/{alertId}
Envia notificação para um alerta específico.

**Request:**
```json
{
  "channel": "SLACK",
    "webhookUrl": "<REDACTED_SLACK_WEBHOOK>"
}
```

**Response (Sucesso):**
```json
{
  "alertId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "channel": "SLACK",
  "status": "SENT",
  "sentAt": "2024-01-15T10:30:00Z",
  "errorMessage": null
}
```

**Response (Falha):**
```json
{
  "alertId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "channel": "TEAMS",
  "status": "FAILED",
  "sentAt": "2024-01-15T10:30:00Z",
  "errorMessage": "Webhook inválido ou inacessível"
}
```

**Response (Ignorado - INFO):**
```json
{
  "alertId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "channel": "SLACK",
  "status": "SKIPPED",
  "sentAt": "2024-01-15T10:30:00Z",
  "errorMessage": "Alerta com severidade INFO não é notificado"
}
```

### GET /risk/alerts/notify/health
Verifica saúde do serviço de notificações.

**Response:**
```json
{
  "status": "UP",
  "availableChannels": 2,
  "supportedChannels": ["SLACK", "TEAMS"],
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## 🔒 Segurança

### RBAC
- **ADMIN**: Acesso total
- **RISK_MANAGER**: Acesso total
- **ENGINEER**: Sem acesso
- **VIEWER**: Sem acesso

### CSRF
Desabilitado para `/risk/alerts/**` (API REST)

### Validação
- `@NotNull` em campos obrigatórios
- `@NotBlank` em URLs de webhook

## 📊 Fluxo de Notificação

```
1. POST /risk/alerts/notify/{alertId}
   ↓
2. RiskAlertNotificationService.notifyAlert()
   ↓
3. Buscar alerta (in-memory via RiskMetricAlertService)
   ↓
4. Filtrar severidade (INFO → SKIPPED)
   ↓
5. Selecionar notificador (Slack ou Teams)
   ↓
6. Formatar mensagem (Markdown ou MessageCard)
   ↓
7. Enviar via RestTemplate (webhook)
   ↓
8. Capturar exceções (retorna FAILED)
   ↓
9. Retornar RiskAlertNotificationResponse
```

## 🎨 Formato de Mensagens

### Slack (Markdown)
```markdown
🚨 *Alerta de Risco Detectado*

*Tipo:* HIGH_BLOCK_RATE_PROJECT
*Severidade:* CRITICAL

*Descrição:*
Projeto 'Payment Service' apresenta taxa de bloqueio de 45% (threshold: 30%)

*Projeto:* a1b2c3d4-e5f6-7890-abcd-ef1234567890
*Regra de Negócio:* fraud-detection-rule

*Métricas:*
• blockRate: 45%
• blockedCount: 18
• totalExecutions: 40

*Recomendações:*
• Revisar regras de negócio do projeto
• Validar se bloqueios são verdadeiros positivos
• Considerar ajustar thresholds das regras

⚠️ _Alerta consultivo – nenhuma ação automática foi executada._
```

### Teams (MessageCard)
```json
{
  "@type": "MessageCard",
  "@context": "https://schema.org/extensions",
  "themeColor": "FF0000",
  "title": "🚨 Alerta de Risco Detectado",
  "summary": "Projeto apresenta taxa de bloqueio elevada",
  "sections": [
    {
      "activityTitle": "HIGH_BLOCK_RATE_PROJECT",
      "activitySubtitle": "Severidade: CRITICAL",
      "text": "Projeto 'Payment Service' apresenta taxa de bloqueio de 45%",
      "facts": [
        {"name": "Projeto", "value": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"},
        {"name": "blockRate", "value": "45%"},
        {"name": "blockedCount", "value": "18"}
      ]
    },
    {
      "title": "Recomendações",
      "text": "- Revisar regras de negócio do projeto\n- Validar se bloqueios são verdadeiros positivos"
    },
    {
      "text": "⚠️ **Alerta consultivo** – nenhuma ação automática foi executada."
    }
  ]
}
```

## 📝 Logs Estruturados

```
2024-01-15 10:30:00 INFO  📣 Tentando enviar alerta via Slack - alertId: xxx, type: HIGH_BLOCK_RATE_PROJECT
2024-01-15 10:30:01 INFO  ✅ Alerta enviado com sucesso via Slack - alertId: xxx
```

```
2024-01-15 10:31:00 INFO  📣 Tentando enviar alerta via Teams - alertId: yyy, type: RULE_OVERBLOCKING
2024-01-15 10:31:01 WARN  ⚠️ Teams retornou status não-OK: 404 - alertId: yyy
```

```
2024-01-15 10:32:00 INFO  ℹ️ Alerta ignorado (severidade INFO) - alertId: zzz
```

```
2024-01-15 10:33:00 ERROR ❌ Falha ao enviar alerta via Slack - alertId: www, error: Connection timeout
```

## 🧪 Testes Manuais

### 1. Configurar Webhook do Slack
```bash
# Criar Incoming Webhook no Slack
# https://api.slack.com/messaging/webhooks

# Obter URL do webhook (removido do repositório por segurança)
```

### 2. Configurar Webhook do Teams
```bash
# Criar Incoming Webhook no Teams
# https://docs.microsoft.com/connectors/teams/

# Obter URL do webhook
# https://outlook.office.com/webhook/...
```

### 3. Testar Envio (Slack)
```bash
curl -X POST http://localhost:8080/risk/alerts/notify/{alertId} \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "channel": "SLACK",
    "webhookUrl": "https://hooks.slack.com/services/T00/B00/XXXX"
  }'
```

### 4. Testar Envio (Teams)
```bash
curl -X POST http://localhost:8080/risk/alerts/notify/{alertId} \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "channel": "TEAMS",
    "webhookUrl": "https://outlook.office.com/webhook/..."
  }'
```

### 5. Verificar Health
```bash
curl -X GET http://localhost:8080/risk/alerts/notify/health \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 🛡️ Princípios Garantidos

### ✅ READ-ONLY
- Não persiste dados de notificação
- Não altera estado dos alertas
- Apenas leitura de alertas existentes (in-memory)

### ✅ Fallback Seguro
- Nunca lança exceções
- Sempre retorna `RiskAlertNotificationResponse`
- Status `FAILED` em caso de erro
- Logs estruturados para auditoria

### ✅ Filtro de Severidade
- `INFO` → `SKIPPED` (não notifica)
- `WARNING` → `SENT` (notifica)
- `CRITICAL` → `SENT` (notifica)

### ✅ Disclaimer Obrigatório
Todas as mensagens incluem:
> ⚠️ Alerta consultivo – nenhuma ação automática foi executada.

### ✅ Contexto Rico
Mensagens incluem:
- Tipo do alerta
- Severidade
- Projeto/Regra afetados
- Métricas (evidências)
- Recomendações específicas

## 📚 Documentação Swagger

Acesse: http://localhost:8080/swagger-ui.html

Seção: **Notificações de Alertas**

Endpoints documentados:
1. `POST /risk/alerts/notify/{alertId}` - Enviar notificação
2. `GET /risk/alerts/notify/health` - Health check

## 🔗 Integração com US#55

US#56 consome alertas detectados pela US#55:

```java
// US#55 detecta alertas
RiskMetricAlertService.detectAlerts()
  ↓
// US#56 notifica alertas
RiskAlertNotificationService.notifyAlert(alertId, request)
```

Fluxo completo:
1. US#55 detecta anomalia (ex: blockRate > 30%)
2. US#55 cria `RiskMetricAlert` (in-memory)
3. Usuário chama `POST /risk/alerts/notify/{alertId}`
4. US#56 busca alerta via `RiskMetricAlertService`
5. US#56 formata e envia para Slack/Teams

## 📊 Métricas de Observabilidade

### Logs Disponíveis
- Tentativas de envio (`📣`)
- Envios com sucesso (`✅`)
- Falhas de envio (`❌`)
- Alertas ignorados (`ℹ️`)
- Warnings (`⚠️`)
- Health checks (`🏥`)

### Padrões de Log
```
📣 Iniciando notificação - alertId: {}, channel: {}
✅ Notificação enviada com sucesso - alertId: {}, channel: {}
❌ Falha ao enviar alerta via {} - alertId: {}, error: {}
ℹ️ Alerta ignorado (severidade INFO) - alertId: {}
⚠️ {} retornou status não-OK: {} - alertId: {}
🏥 Health check - canais disponíveis: {}
```

## 🚀 Próximos Passos (Melhorias Futuras)

1. **Retry Logic**: Implementar retentativas automáticas
2. **Circuit Breaker**: Proteger contra webhooks instáveis
3. **Rate Limiting**: Evitar spam de notificações
4. **Templates**: Mensagens customizáveis por equipe
5. **Persistência Opcional**: Histórico de notificações (opcional)
6. **Notificações em Batch**: Agrupar múltiplos alertas

## ✅ Critérios de Aceitação (DoD)

- [x] Enum `NotificationChannel` (SLACK, TEAMS)
- [x] Enum `NotificationStatus` (SENT, FAILED, SKIPPED)
- [x] DTO `RiskAlertNotificationRequest` com validação
- [x] DTO `RiskAlertNotificationResponse` com timestamps
- [x] Interface `AlertNotifier` (Strategy Pattern)
- [x] `SlackAlertNotifier` com Markdown
- [x] `TeamsAlertNotifier` com MessageCard
- [x] `RiskAlertNotificationService` com orquestração
- [x] `RiskAlertNotificationController` com Swagger
- [x] Filtro de severidade (INFO → SKIPPED)
- [x] Fallback seguro (sem exceções)
- [x] Logs estruturados com emojis
- [x] Disclaimer obrigatório
- [x] Recomendações específicas por tipo
- [x] RBAC (ADMIN, RISK_MANAGER)
- [x] SecurityConfig atualizado
- [x] Compilação sem erros
- [x] READ-ONLY (in-memory)
- [x] Health check endpoint

## 📖 Referências

- [Slack Incoming Webhooks](https://api.slack.com/messaging/webhooks)
- [Teams Incoming Webhooks](https://docs.microsoft.com/connectors/teams/)
- [MessageCard Format](https://docs.microsoft.com/outlook/actionable-messages/message-card-reference)
- US#55 - Alertas Inteligentes (detectores)
- US#54 - Métricas do Gate de Risco
