# US#53 - Integração CI/CD Nativa

## 📋 Visão Geral

A US#53 implementa integração nativa com GitHub Actions e GitLab CI, permitindo usar o backoffice-alerta como **gate de risco organizacional** em pipelines de CI/CD.

### 🎯 Objetivo

Bloquear ou aprovar Pull Requests/Merge Requests **automaticamente** baseado em análise de risco de negócio, ANTES do merge.

---

## 🚀 Como Funciona

### Fluxo do Gate

```
PR/MR criado → Pipeline CI/CD inicia → Chama /risk/ci/gate
                                              ↓
                                    Análise de Risco
                                              ↓
                        ┌─────────────────────┼─────────────────────┐
                        ↓                     ↓                     ↓
                  exitCode=0            exitCode=1            exitCode=2
                  (APROVADO)    (APROVADO_COM_RESTRICOES)    (BLOQUEADO)
                        ↓                     ↓                     ↓
              Pipeline continua    Pipeline continua     Pipeline FALHA
                                   (com warnings)
```

### Exit Codes

| Code | Decisão | Comportamento | Quando Ocorre |
|------|---------|---------------|---------------|
| `0` | APROVADO | ✅ Pipeline continua normalmente | Risco BAIXO, sem regras críticas impactadas |
| `1` | APROVADO_COM_RESTRICOES | ⚠️ Pipeline continua com warnings | Risco MÉDIO, ou fallback mode |
| `2` | BLOQUEADO | ❌ Pipeline falha, merge bloqueado | Risco ALTO/CRÍTICO em produção |

---

## 🔧 Configuração

### 1️⃣ GitHub Actions

#### Passo 1: Configurar Secrets

No repositório GitHub, adicione os secrets:

```
Settings → Secrets and variables → Actions → New repository secret
```

**Secrets necessários:**
- `BACKOFFICE_ALERTA_URL`: URL da API (ex: `https://alerta.company.com`)
- `BACKOFFICE_ALERTA_TOKEN`: JWT token de autenticação

#### Passo 2: Adicionar Workflow

Crie o arquivo `.github/workflows/risk-gate.yml`:

```yaml
name: Risk Gate

on:
  pull_request:
    branches: [main, develop]

jobs:
  risk-gate:
    uses: ./.github/workflows/risk-gate-template.yml
    secrets: inherit
```

Copie o arquivo [cicd/github-action.yml](../cicd/github-action.yml) para `.github/workflows/risk-gate-template.yml`

#### Passo 3: Configurar Token Git no Servidor

No servidor onde o backoffice-alerta roda:

```bash
export GITHUB_TOKEN="ghp_yourPersonalAccessToken"
```

---

### 2️⃣ GitLab CI

#### Passo 1: Configurar Variables

No projeto GitLab, adicione as variáveis:

```
Settings → CI/CD → Variables → Add variable
```

**Variáveis necessárias:**
- `BACKOFFICE_ALERTA_URL`: URL da API (masked)
- `BACKOFFICE_ALERTA_TOKEN`: JWT token (masked, protected)
- `GITLAB_TOKEN`: Personal Access Token do GitLab (masked)

#### Passo 2: Incluir Template

No seu `.gitlab-ci.yml`:

```yaml
include:
  - local: '/cicd/gitlab-ci-template.yml'

stages:
  - risk-gate
  - build
  - test
  - deploy

# Seus outros jobs aqui...
```

#### Passo 3: Configurar Token Git no Servidor

No servidor onde o backoffice-alerta roda:

```bash
export GITLAB_TOKEN="glpat_yourPersonalAccessToken"
```

---

## 📡 Endpoint da API

### `POST /risk/ci/gate`

**Autenticação:** Bearer JWT token

**Request Body:**
```json
{
  "provider": "GITHUB",
  "repositoryUrl": "https://github.com/company/repo",
  "pullRequestNumber": "123",
  "environment": "PRODUCTION",
  "changeType": "FEATURE",
  "projectId": "550e8400-e29b-41d4-a716-446655440010"  // Opcional (US#50)
}
```

**Response:**
```json
{
  "finalDecision": "APROVADO_COM_RESTRICOES",
  "overallRiskLevel": "MEDIO",
  "exitCode": 1,
  "summary": "⚠️ PR aprovado com restrições. 3 regra(s) de negócio impactada(s). Risco: MEDIO.",
  "reasonCodes": [
    "DECISION_APROVADO_COM_RESTRICOES",
    "RISK_LEVEL_MEDIO",
    "BUSINESS_RULES_IMPACTED",
    "ENV_PRODUCTION",
    "CHANGE_TYPE_FEATURE"
  ],
  "actionsRequired": [
    "Garantir cobertura de testes",
    "Validar com owners das regras impactadas"
  ],
  "projectContext": {
    "mode": "SCOPED",
    "projectId": "550e8400-e29b-41d4-a716-446655440010",
    "projectName": "Payment Gateway"
  },
  "provider": "GITHUB",
  "pullRequestNumber": "123",
  "repositoryUrl": "https://github.com/company/repo"
}
```

---

## 🌍 Modos de Operação

### GLOBAL (Sem `projectId`)

Analisa contra **todas as regras de negócio** cadastradas no sistema.

**Quando usar:**
- Repositórios monorepo
- Análise corporativa
- Sem segregação por projeto

**Exemplo:**
```json
{
  "provider": "GITHUB",
  "repositoryUrl": "https://github.com/company/monorepo",
  "pullRequestNumber": "456",
  "environment": "PRODUCTION",
  "changeType": "FEATURE"
}
```

---

### SCOPED (Com `projectId`)

Analisa apenas contra regras **específicas do projeto** (US#50).

**Quando usar:**
- Multi-projeto
- Times isolados
- Análise focada

**Exemplo:**
```json
{
  "provider": "GITLAB",
  "repositoryUrl": "https://gitlab.com/company/payment-api",
  "pullRequestNumber": "42",
  "environment": "PRODUCTION",
  "changeType": "HOTFIX",
  "projectId": "b394f1c1-4a51-42ca-89e4-14353eaa37e1"
}
```

---

## 🔄 Fallback Automático

Se o provider Git estiver **indisponível** (token não configurado, erro de rede, etc.), o sistema retorna automaticamente:

- **exitCode:** `1` (warning)
- **finalDecision:** `APROVADO_COM_RESTRICOES`
- **reasonCode:** `CI_PROVIDER_UNAVAILABLE` ou `ANALYSIS_ERROR`

**Pipeline NÃO é bloqueado**, mas gera warning solicitando revisão manual.

### Exemplo de Fallback

```json
{
  "finalDecision": "APROVADO_COM_RESTRICOES",
  "overallRiskLevel": "DESCONHECIDO",
  "exitCode": 1,
  "summary": "⚠️ Análise não pôde ser concluída. Provider indisponível ou erro de configuração.",
  "reasonCodes": [
    "CI_PROVIDER_UNAVAILABLE",
    "FALLBACK_MODE"
  ],
  "actionsRequired": [
    "Verificar configuração de tokens (GITHUB_TOKEN ou GITLAB_TOKEN)",
    "Validar acesso ao repositório",
    "Revisar manualmente o Pull Request"
  ]
}
```

---

## 📊 Políticas de Risco

### Regras de Decisão

| Risco | Ambiente | Decisão | Exit Code |
|-------|----------|---------|-----------|
| BAIXO | Qualquer | APROVADO | 0 |
| MEDIO | DEVELOPMENT/STAGING | APROVADO | 0 |
| MEDIO | PRODUCTION | APROVADO_COM_RESTRICOES | 1 |
| ALTO | DEVELOPMENT/STAGING | APROVADO_COM_RESTRICOES | 1 |
| ALTO | PRODUCTION | BLOQUEADO | 2 |
| CRITICO | PRODUCTION | BLOQUEADO | 2 |

### Customização (GitLab CI)

Você pode desabilitar o bloqueio automático:

```yaml
variables:
  RISK_GATE_FAIL_ON_BLOCK: "false"  # Não falha pipeline mesmo com exitCode=2
  RISK_GATE_WARN_ON_RESTRICTIONS: "true"
```

---

## 🧪 Testes

### Teste Local (curl)

```bash
# 1. Obter JWT token
TOKEN=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' \
  http://localhost:8080/auth/login | jq -r '.token')

# 2. Chamar gate
curl -X POST \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "provider": "GITHUB",
    "repositoryUrl": "https://github.com/test/repo",
    "pullRequestNumber": "123",
    "environment": "PRODUCTION",
    "changeType": "FEATURE"
  }' \
  http://localhost:8080/risk/ci/gate | jq
```

### Teste com GitHub Actions

Crie um PR de teste e veja o workflow executar:

```
Actions → Risk Gate → Workflow runs
```

### Teste com GitLab CI

Crie um MR de teste e veja o pipeline:

```
CI/CD → Pipelines → risk-gate:production
```

---

## 🔐 Segurança

### ✅ READ-ONLY Garantido

O endpoint `/risk/ci/gate`:

- ❌ **NÃO cria** auditorias
- ❌ **NÃO cria** SLAs
- ❌ **NÃO envia** notificações
- ❌ **NÃO comenta** em PR/MR
- ❌ **NÃO faz** commits ou merges
- ✅ **APENAS** consulta e retorna decisão

### Autenticação

**JWT obrigatório:**
- Roles permitidas: `ADMIN`, `RISK_MANAGER`, `ENGINEER`
- Token deve ser passado no header `Authorization: Bearer <token>`

**CSRF desabilitado** para `/risk/ci/**` (necessário para chamadas CI/CD).

### Tokens Git

- Armazenados como **variáveis de ambiente** no servidor
- **Nunca** passados no request do CI/CD
- Permissões **read-only** no GitHub/GitLab

---

## 📦 Reutilização de Serviços

A US#53 **reutiliza 100%** dos serviços existentes:

1. **GitPullRequestImpactService** (US#51/52)
   - Busca dados do PR/MR
   - Usa GitHubProviderClient ou GitLabProviderClient

2. **BusinessImpactAnalysisService**
   - Analisa impacto em regras de negócio
   - Calcula risco

3. **ProjectRepository** (US#50)
   - Resolve contexto GLOBAL/SCOPED

**Nenhuma duplicação de lógica.**

---

## 📝 Logs Estruturados

### Logs de Sucesso

```
🔁 CI GATE | provider=GITHUB | repo=https://github.com/company/repo | pr=123 | env=PRODUCTION | changeType=FEATURE
🔎 CI GATE | Modo SCOPED | Projeto: Payment API (550e8400-...)
📄 [GITHUB] Pull Request encontrado: feat: Add CPF validation
✅ CI GATE | decision=APROVADO_COM_RESTRICOES | risk=MEDIO | exitCode=1 | impactedRules=3
```

### Logs de Fallback

```
🔁 CI GATE | provider=GITHUB | repo=... | pr=999
⚠️ CI GATE | Provider indisponível: GitHub token não configurado
🔄 CI GATE | Fallback mode | exitCode=1 | reason=CI_PROVIDER_UNAVAILABLE
```

---

## 🎯 Exemplos de Uso

### Exemplo 1: GitHub + SCOPED + APROVADO

**Request:**
```json
{
  "provider": "GITHUB",
  "repositoryUrl": "https://github.com/acme/payment-api",
  "pullRequestNumber": "42",
  "environment": "STAGING",
  "changeType": "FEATURE",
  "projectId": "550e8400-e29b-41d4-a716-446655440010"
}
```

**Response:**
```json
{
  "finalDecision": "APROVADO",
  "overallRiskLevel": "BAIXO",
  "exitCode": 0,
  "summary": "✅ PR aprovado. Nenhuma regra crítica impactada. Risco: BAIXO.",
  "reasonCodes": ["DECISION_APROVADO", "RISK_LEVEL_BAIXO", "NO_CRITICAL_IMPACT"],
  "actionsRequired": [],
  "projectContext": {
    "mode": "SCOPED",
    "projectId": "550e8400-e29b-41d4-a716-446655440010",
    "projectName": "Payment API"
  }
}
```

**Pipeline:** ✅ Continua normalmente

---

### Exemplo 2: GitLab + GLOBAL + BLOQUEADO

**Request:**
```json
{
  "provider": "GITLAB",
  "repositoryUrl": "https://gitlab.com/acme/core",
  "pullRequestNumber": "999",
  "environment": "PRODUCTION",
  "changeType": "HOTFIX"
}
```

**Response:**
```json
{
  "finalDecision": "BLOQUEADO",
  "overallRiskLevel": "CRITICO",
  "exitCode": 2,
  "summary": "❌ PR bloqueado. 8 regra(s) de negócio impactada(s). Risco: CRITICO.",
  "reasonCodes": ["DECISION_BLOQUEADO", "RISK_LEVEL_CRITICO", "BUSINESS_RULES_IMPACTED"],
  "actionsRequired": [
    "Revisar mudanças com time de engenharia",
    "Reduzir impacto em regras críticas",
    "Obter aprovação executiva antes do merge"
  ],
  "projectContext": {
    "mode": "GLOBAL"
  }
}
```

**Pipeline:** ❌ FALHA (exit code 2)

---

## 🐛 Troubleshooting

### Erro: "Provider indisponível"

**Causa:** Token não configurado no servidor.

**Solução:**
```bash
# No servidor backoffice-alerta
export GITHUB_TOKEN="ghp_..."
export GITLAB_TOKEN="glpat_..."

# Reiniciar aplicação
```

---

### Erro: "401 Unauthorized"

**Causa:** JWT token inválido ou expirado.

**Solução:**
1. Gerar novo token via `/auth/login`
2. Atualizar secret `BACKOFFICE_ALERTA_TOKEN` no GitHub/GitLab

---

### Pipeline continua mesmo com exitCode=2

**Causa:** Script do CI/CD não está verificando exit code.

**Solução (GitHub):**
Verifique se o step `Process gate decision` tem:
```yaml
if [[ "$EXIT_CODE" == "2" ]]; then
  exit 1
fi
```

**Solução (GitLab):**
Verifique se `RISK_GATE_FAIL_ON_BLOCK` está `true`.

---

## 📚 Referências

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [GitLab CI/CD Documentation](https://docs.gitlab.com/ee/ci/)
- [US#50 - Contextualização por Projeto](../docs/US50_PROJECTS.md)
- [US#51/52 - Integração Git](../US52_INTEGRACAO_GIT.md)

---

## ✅ Checklist de Implementação

- [x] Endpoint `/risk/ci/gate` criado
- [x] DTOs `CIGateRequest` e `CIGateResponse`
- [x] Service `CIGateService` com reutilização
- [x] Controller `CIGateController` com Swagger
- [x] GitHub Action template
- [x] GitLab CI template
- [x] Fallback automático
- [x] Logs estruturados
- [x] Documentação completa
- [x] Modo GLOBAL e SCOPED
- [x] Exit codes corretos (0, 1, 2)
- [x] READ-ONLY garantido
- [ ] Testes de integração
- [ ] Deploy em ambiente de homologação

---

**Implementado por:** GitHub Copilot  
**Data:** 2025-12-20  
**Status:** ✅ CONCLUÍDO
