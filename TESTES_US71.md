# 📊 Testes da US#71 - Comparação de Impacto PRE vs POST

## 🎯 Objetivo

Validar endpoint de comparação de impacto entre estado PRE (baseline) e POST (após mudança).

**Características:**
- ✅ READ-ONLY (não persiste dados)
- ✅ Determinístico (sem IA/ML)
- ✅ Fail-safe (erro → UNCHANGED)
- ✅ 4 dimensões: AST, RAG, BUSINESS, TESTS

---

## 📋 Endpoint Testado

```
POST /risk/llm/impact/compare
```

**RBAC:** ADMIN, RISK_MANAGER, ENGINEER

---

## 🧪 Testes com PowerShell

### Teste 1: Comparação Básica (IMPROVED)

```powershell
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer SEU_TOKEN_JWT"
}

$body = @{
    provider = "GITHUB"
    repositoryUrl = "https://github.com/seu-repo/projeto"
    baseRef = "main"
    compareRef = "123"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/risk/llm/impact/compare" `
    -Method POST `
    -Headers $headers `
    -Body $body
```

**Resposta Esperada (IMPROVED):**
```json
{
  "finalScoreDelta": 25,
  "finalVerdict": "IMPROVED",
  "deltas": [
    {
      "dimension": "AST",
      "metric": "cyclomaticComplexity",
      "beforeValue": 3.4,
      "afterValue": 2.1,
      "delta": -1.3,
      "interpretation": "IMPROVED"
    },
    {
      "dimension": "TESTS",
      "metric": "criticalFilesWithoutTests",
      "beforeValue": 5.0,
      "afterValue": 2.0,
      "delta": -3.0,
      "interpretation": "IMPROVED"
    }
  ],
  "executiveSummary": "✅ **Melhoria Detectada**\n\nA mudança reduziu complexidade...",
  "baseRef": "main",
  "compareRef": "123"
}
```

---

### Teste 2: Degradação Detectada (DEGRADED)

```powershell
$body = @{
    provider = "GITHUB"
    repositoryUrl = "https://github.com/seu-repo/projeto"
    baseRef = "main"
    compareRef = "456"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/risk/llm/impact/compare" `
    -Method POST `
    -Headers $headers `
    -Body $body
```

**Resposta Esperada (DEGRADED):**
```json
{
  "finalScoreDelta": -35,
  "finalVerdict": "DEGRADED",
  "deltas": [
    {
      "dimension": "AST",
      "metric": "cyclomaticComplexity",
      "beforeValue": 2.1,
      "afterValue": 5.8,
      "delta": 3.7,
      "interpretation": "DEGRADED"
    },
    {
      "dimension": "RAG",
      "metric": "fallbackRate",
      "beforeValue": 15.0,
      "afterValue": 42.0,
      "delta": 27.0,
      "interpretation": "DEGRADED"
    }
  ],
  "executiveSummary": "🚨 **Degradação Detectada**\n\nA mudança aumentou a complexidade...",
  "baseRef": "main",
  "compareRef": "456"
}
```

---

### Teste 3: Sem Mudanças Significativas (UNCHANGED)

```powershell
$body = @{
    provider = "GITHUB"
    repositoryUrl = "https://github.com/seu-repo/projeto"
    baseRef = "main"
    compareRef = "789"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/risk/llm/impact/compare" `
    -Method POST `
    -Headers $headers `
    -Body $body
```

**Resposta Esperada (UNCHANGED):**
```json
{
  "finalScoreDelta": 0,
  "finalVerdict": "UNCHANGED",
  "deltas": [
    {
      "dimension": "AST",
      "metric": "cyclomaticComplexity",
      "beforeValue": 2.1,
      "afterValue": 2.3,
      "delta": 0.2,
      "interpretation": "NEUTRAL"
    }
  ],
  "executiveSummary": "➡️ **Sem Mudança Significativa**...",
  "baseRef": "main",
  "compareRef": "789"
}
```

---

## 🐧 Testes com Curl (Linux/Mac)

### Teste 1: IMPROVED

```bash
curl -X POST http://localhost:8080/risk/llm/impact/compare \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_JWT" \
  -d '{
    "provider": "GITHUB",
    "repositoryUrl": "https://github.com/seu-repo/projeto",
    "baseRef": "main",
    "compareRef": "123"
  }' | jq .
```

### Teste 2: DEGRADED

```bash
curl -X POST http://localhost:8080/risk/llm/impact/compare \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_JWT" \
  -d '{
    "provider": "GITHUB",
    "repositoryUrl": "https://github.com/seu-repo/projeto",
    "baseRef": "main",
    "compareRef": "456"
  }' | jq .
```

---

## 🌐 Teste via Swagger UI

1. Acesse: http://localhost:8080/swagger-ui.html
2. Localize: **LLM Impact Comparison**
3. Expanda: `POST /risk/llm/impact/compare`
4. Clique em **Try it out**
5. Preencha o JSON:
   ```json
   {
     "provider": "GITHUB",
     "repositoryUrl": "https://github.com/seu-repo/projeto",
     "baseRef": "main",
     "compareRef": "123"
   }
   ```
6. Clique em **Execute**
7. Verifique:
   - Status Code: **200**
   - Response Body: JSON com `finalVerdict`, `deltas`, `executiveSummary`

---

## 🔐 Testes de RBAC

### Acesso Negado (403) - Sem Role

```powershell
# Login como usuário sem role adequada
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer TOKEN_SEM_ROLE"
}

try {
    Invoke-RestMethod -Uri "http://localhost:8080/risk/llm/impact/compare" `
        -Method POST `
        -Headers $headers `
        -Body $body
} catch {
    Write-Host "Erro esperado: $($_.Exception.Message)"
    # Deve retornar 403 Forbidden
}
```

### Acesso Permitido - ENGINEER

```powershell
# Login como ENGINEER
POST /auth/login
{
  "username": "engineer.user",
  "password": "senha"
}

# Response:
{
  "token": "eyJhbGciOi...",
  "role": "ENGINEER"
}

# Usar token no request de comparação
# Deve retornar 200 OK
```

---

## 🚀 Integração CI/CD

### GitHub Actions

```yaml
name: Impact Comparison Gate

on:
  pull_request:
    types: [opened, synchronize]

jobs:
  compare-impact:
    runs-on: ubuntu-latest
    steps:
      - name: Compare PRE vs POST Impact
        id: compare
        run: |
          RESPONSE=$(curl -s -X POST http://api.example.com/risk/llm/impact/compare \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer ${{ secrets.API_TOKEN }}" \
            -d '{
              "provider": "GITHUB",
              "repositoryUrl": "${{ github.repository }}",
              "baseRef": "${{ github.base_ref }}",
              "compareRef": "${{ github.event.pull_request.number }}"
            }')
          
          VERDICT=$(echo $RESPONSE | jq -r '.finalVerdict')
          echo "verdict=$VERDICT" >> $GITHUB_OUTPUT
          
          # Exit codes para pipeline
          if [ "$VERDICT" == "IMPROVED" ]; then
            exit 0  # ✅ Sucesso
          elif [ "$VERDICT" == "UNCHANGED" ]; then
            exit 1  # ⚠️ Warning
          else
            exit 2  # ❌ Bloqueio (DEGRADED)
          fi

      - name: Comment PR
        if: always()
        uses: actions/github-script@v6
        with:
          script: |
            const verdict = '${{ steps.compare.outputs.verdict }}';
            const emoji = verdict === 'IMPROVED' ? '✅' : 
                         verdict === 'UNCHANGED' ? '➡️' : '🚨';
            
            github.rest.issues.createComment({
              issue_number: context.issue.number,
              owner: context.repo.owner,
              repo: context.repo.repo,
              body: `${emoji} **Impact Comparison Result**: ${verdict}`
            });
```

---

## 📊 Exit Codes para CI/CD

| Veredicto   | Exit Code | Significado                     |
|-------------|-----------|---------------------------------|
| IMPROVED    | 0         | ✅ Mudança melhorou métricas    |
| UNCHANGED   | 1         | ➡️ Sem mudança significativa    |
| DEGRADED    | 2         | ❌ Degradação detectada         |

### Script Bash para CI/CD

```bash
#!/bin/bash

RESPONSE=$(curl -s -X POST http://api.example.com/risk/llm/impact/compare \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $API_TOKEN" \
  -d "{
    \"provider\": \"GITHUB\",
    \"repositoryUrl\": \"$REPO_URL\",
    \"baseRef\": \"$BASE_REF\",
    \"compareRef\": \"$PR_NUMBER\"
  }")

VERDICT=$(echo $RESPONSE | jq -r '.finalVerdict')

case $VERDICT in
  IMPROVED)
    echo "✅ Impact comparison: IMPROVED"
    exit 0
    ;;
  UNCHANGED)
    echo "➡️ Impact comparison: UNCHANGED"
    exit 1
    ;;
  DEGRADED)
    echo "🚨 Impact comparison: DEGRADED"
    exit 2
    ;;
  *)
    echo "❌ Unknown verdict: $VERDICT"
    exit 3
    ;;
esac
```

---

## 📝 Verificação de Logs

Ao executar o endpoint, verifique os logs do backend:

```bash
# Logs esperados
📊 [US#71] Iniciando comparação PRE vs POST | base=main | compare=123
✅ [US#71] PRE state recuperado | files=0
✅ [US#71] POST state recuperado | files=5
📊 [US#71] PRE vs POST | Dimension=AST | metric=complexity | Δ=-1.3 → IMPROVED
📊 [US#71] PRE vs POST | Dimension=RAG | metric=fallbackRate | Δ=10.0 → NEUTRAL
📊 [US#71] PRE vs POST | Dimension=TESTS | metric=untestedFiles | Δ=-3 → IMPROVED
🧠 [US#71] Final Verdict: IMPROVED | scoreDelta=25
✅ [US#71] Comparação concluída | verdict=IMPROVED | deltas=3
```

---

## ✅ Checklist de Validação

- [ ] Endpoint `/risk/llm/impact/compare` responde com 200
- [ ] Response contém `finalVerdict` (IMPROVED/DEGRADED/UNCHANGED)
- [ ] Response contém array `deltas` com dimensões
- [ ] Response contém `executiveSummary` legível
- [ ] RBAC funciona (ADMIN, RISK_MANAGER, ENGINEER têm acesso)
- [ ] Usuário sem role recebe 403 Forbidden
- [ ] Logs mostram marcador `[US#71]`
- [ ] Fail-safe retorna UNCHANGED em caso de erro
- [ ] Swagger UI exibe endpoint com 3 exemplos
- [ ] Endpoint está documentado em `/v3/api-docs`

---

## 🔧 Troubleshooting

### Erro 403 Forbidden

**Causa:** Usuário sem role adequada

**Solução:** Verificar se usuário tem role `ADMIN`, `RISK_MANAGER` ou `ENGINEER`

### Erro 500 Internal Server Error

**Causa:** Erro interno no service

**Solução:** Verificar logs. Fail-safe deve retornar UNCHANGED com mensagem de erro no `executiveSummary`

### Response sempre UNCHANGED

**Causa:** PRE state não está sendo calculado (implementação simplificada)

**Solução:** Em produção, implementar análise do commit base. Por enquanto, comportamento esperado.

---

## 📚 Referências

- **US#69:** Análise AST para complexidade ciclomática
- **US#70:** Detecção de mudanças geradas por LLM
- **US#51/52:** Análise de Pull Request
- **US#63/67:** RAG e scores semânticos

---

**Nota:** Esta US é READ-ONLY e determinística. Não persiste dados, não executa código, não usa IA/ML.
