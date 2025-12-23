# 🚀 Testes da US#72 - Onboarding Guiado de Projeto Real

## 🎯 Objetivo

Validar fluxo completo de onboarding de projeto REAL (GitHub/GitLab) no sistema de análise de risco.

**Características:**
- ✅ READ-ONLY absoluto (nunca escreve no Git)
- ✅ Orquestra US#48-US#71 (sem duplicação)
- ✅ Fail-safe (erro não quebra sistema)
- ✅ Enterprise-grade (auditável, logs estruturados)

---

## 📋 Endpoints Testados

```
POST /risk/projects/onboarding/start
GET /risk/projects/onboarding/status/{projectId}
GET /risk/projects/onboarding/health
```

**RBAC:**
- POST /start → ADMIN
- GET /status/** → ADMIN
- GET /health → Autenticado (qualquer role)

---

## 🧪 Testes com PowerShell

### Teste 1: Onboarding Completo (GitHub)

```powershell
# 1. Login como ADMIN
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body '{"username":"admin","password":"admin123"}'

$token = $loginResponse.token

# 2. Criar projeto (se não existir)
$projectBody = @{
    name = "Payment Backoffice"
    description = "Sistema de pagamentos crítico"
    owner = "Financeiro"
    repositoryUrl = "https://github.com/seu-repo/payment-backoffice"
    active = $true
} | ConvertTo-Json

$project = Invoke-RestMethod -Uri "http://localhost:8080/api/projects" `
    -Method POST `
    -Headers @{"Authorization"="Bearer $token"; "Content-Type"="application/json"} `
    -Body $projectBody

# 3. Iniciar onboarding
$onboardingBody = @{
    projectId = $project.id
    provider = "GITHUB"
    repositoryUrl = "https://github.com/seu-repo/payment-backoffice"
    branch = "main"
} | ConvertTo-Json

$result = Invoke-RestMethod -Uri "http://localhost:8080/risk/projects/onboarding/start" `
    -Method POST `
    -Headers @{"Authorization"="Bearer $token"; "Content-Type"="application/json"} `
    -Body $onboardingBody

# Exibir resultado
$result | ConvertTo-Json -Depth 10
```

**Resposta Esperada (Sucesso):**
```json
{
  "projectId": "550e8400-e29b-41d4-a716-446655440000",
  "projectName": "Payment Backoffice",
  "status": "ONBOARDED",
  "rulesImported": 23,
  "embeddingsIndexed": 23,
  "astCoverage": "PARTIAL",
  "ragStatus": "FULL",
  "baselineRisk": "MEDIUM",
  "limitations": []
}
```

---

### Teste 2: Onboarding com Limitações

```powershell
# Projeto com RAG desabilitado
$onboardingBody = @{
    projectId = "660e8400-e29b-41d4-a716-446655440111"
    provider = "GITLAB"
    repositoryUrl = "https://gitlab.com/company/legacy-system"
    branch = "master"
} | ConvertTo-Json

$result = Invoke-RestMethod -Uri "http://localhost:8080/risk/projects/onboarding/start" `
    -Method POST `
    -Headers @{"Authorization"="Bearer $token"; "Content-Type"="application/json"} `
    -Body $onboardingBody
```

**Resposta Esperada (Com Limitações):**
```json
{
  "projectId": "660e8400-e29b-41d4-a716-446655440111",
  "projectName": "Legacy System",
  "status": "ONBOARDED",
  "rulesImported": 5,
  "embeddingsIndexed": 0,
  "astCoverage": "NONE",
  "ragStatus": "LIMITED",
  "baselineRisk": "HIGH",
  "limitations": [
    "RAG disabled - embeddings not available",
    "AST analysis not available"
  ]
}
```

---

### Teste 3: Consultar Status

```powershell
$projectId = "550e8400-e29b-41d4-a716-446655440000"

$status = Invoke-RestMethod `
    -Uri "http://localhost:8080/risk/projects/onboarding/status/$projectId" `
    -Method GET `
    -Headers @{"Authorization"="Bearer $token"}

$status | ConvertTo-Json
```

**Resposta Esperada:**
```json
{
  "currentStep": "COMPLETED",
  "completedSteps": [
    "VALIDATE_PROJECT",
    "VALIDATE_GIT",
    "IMPORT_RULES",
    "INDEX_EMBEDDINGS",
    "ANALYZE_AST",
    "GENERATE_BASELINE",
    "FINALIZE"
  ],
  "pendingSteps": [],
  "lastUpdated": "2025-12-22T10:45:30"
}
```

---

### Teste 4: Health Check

```powershell
$health = Invoke-RestMethod `
    -Uri "http://localhost:8080/risk/projects/onboarding/health" `
    -Method GET `
    -Headers @{"Authorization"="Bearer $token"}

$health
```

**Resposta Esperada:**
```json
{
  "status": "OPERATIONAL",
  "capabilities": {
    "ruleImport": "AVAILABLE",
    "embeddings": "AVAILABLE",
    "ast": "AVAILABLE",
    "gitConnectivity": "AVAILABLE"
  }
}
```

---

## 🐧 Testes com Curl (Linux/Mac)

### Teste 1: Onboarding Completo

```bash
# 1. Login
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.token')

# 2. Criar projeto
PROJECT_ID=$(curl -s -X POST http://localhost:8080/api/projects \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Payment Backoffice",
    "description": "Sistema de pagamentos",
    "owner": "Financeiro",
    "repositoryUrl": "https://github.com/company/payment",
    "active": true
  }' | jq -r '.id')

# 3. Iniciar onboarding
curl -X POST http://localhost:8080/risk/projects/onboarding/start \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"projectId\": \"$PROJECT_ID\",
    \"provider\": \"GITHUB\",
    \"repositoryUrl\": \"https://github.com/company/payment\",
    \"branch\": \"main\"
  }" | jq .
```

### Teste 2: Consultar Status

```bash
curl -X GET http://localhost:8080/risk/projects/onboarding/status/$PROJECT_ID \
  -H "Authorization: Bearer $TOKEN" | jq .
```

---

## 🌐 Teste via Swagger UI

1. Acesse: http://localhost:8080/swagger-ui.html
2. Localize: **Project Onboarding**
3. Expanda: `POST /risk/projects/onboarding/start`
4. Clique em **Authorize**, insira token de ADMIN
5. Clique em **Try it out**
6. Preencha o JSON:
   ```json
   {
     "projectId": "550e8400-e29b-41d4-a716-446655440000",
     "provider": "GITHUB",
     "repositoryUrl": "https://github.com/company/payment",
     "branch": "main"
   }
   ```
7. Clique em **Execute**
8. Verifique:
   - Status Code: **200**
   - Response: `status: "ONBOARDED"` ou `"FAILED"`
   - `rulesImported > 0`
   - `limitations` vazio ou com warnings

---

## 🔐 Testes de RBAC

### Acesso Negado (403) - ENGINEER tentando onboarding

```powershell
# Login como ENGINEER
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body '{"username":"engineer.user","password":"senha"}'

$tokenEngineer = $loginResponse.token

# Tentar iniciar onboarding (deve falhar)
try {
    Invoke-RestMethod -Uri "http://localhost:8080/risk/projects/onboarding/start" `
        -Method POST `
        -Headers @{"Authorization"="Bearer $tokenEngineer"; "Content-Type"="application/json"} `
        -Body $onboardingBody
} catch {
    Write-Host "Erro esperado: $($_.Exception.Message)"
    # Deve retornar 403 Forbidden
}
```

### Health Check Público (200)

```powershell
# Qualquer usuário autenticado pode verificar health
$health = Invoke-RestMethod `
    -Uri "http://localhost:8080/risk/projects/onboarding/health" `
    -Method GET `
    -Headers @{"Authorization"="Bearer $tokenEngineer"}

# Deve retornar 200 OK
```

---

## 📊 Fluxo de Onboarding - Etapas

| Etapa | Descrição | US Reutilizada | Fail-Safe |
|-------|-----------|----------------|-----------|
| 1 | Validar Projeto | US#48 | ✅ |
| 2 | Validar Git | US#51/52 | ✅ |
| 3 | Importar Regras | US#68 | ✅ |
| 4 | Indexar Embeddings | US#65 + US#66 | ✅ |
| 5 | Análise AST | US#69 | ✅ |
| 6 | Baseline Risco | US#51/52 | ✅ |
| 7 | Finalizar | - | ✅ |

**Cada etapa:**
- Tem log estruturado `[US#72]`
- Nunca lança exceção
- Adiciona limitações em caso de falha não-crítica
- Bloqueia onboarding apenas se crítico

---

## 📝 Verificação de Logs

Ao executar onboarding, verifique os logs do backend:

```bash
# Logs esperados
[US#72] 🚀 Starting onboarding for projectId: 550e8400-e29b-41d4-a716-446655440000
[US#72] 📋 STEP 1: Validating project 550e8400-e29b-41d4-a716-446655440000
[US#72] ✅ Project validated: Payment Backoffice
[US#72] 🔗 STEP 2: Validating Git connectivity
[US#72] 🔗 Git connectivity validated
[US#72] 📥 STEP 3: Importing business rules
[US#72] 📥 Rules imported: 23
[US#72] 🧠 STEP 4: Indexing embeddings
[US#72] 🧠 Embeddings indexed: 23
[US#72] 🌳 STEP 5: Analyzing AST
[US#72] 🌳 AST coverage: PARTIAL
[US#72] 📊 STEP 6: Generating risk baseline
[US#72] 📊 Baseline risk: MEDIUM
[US#72] 🎉 STEP 7: Finalizing onboarding
[US#72] ✅ Project 550e8400-e29b-41d4-a716-446655440000 is now ONBOARDED
[US#72] ✅ Onboarding completed successfully for project: Payment Backoffice
```

---

## ✅ Checklist de Validação

### Funcional
- [ ] Endpoint `/risk/projects/onboarding/start` responde 200
- [ ] Projeto marcado como `ONBOARDED` após sucesso
- [ ] Regras importadas do Git (US#68)
- [ ] Embeddings indexados (US#65/66)
- [ ] AST analisado (US#69)
- [ ] Baseline de risco gerado
- [ ] Limitações reportadas corretamente
- [ ] Fail-safe: erro não quebra sistema

### Segurança
- [ ] ADMIN pode executar onboarding
- [ ] ENGINEER/RISK_MANAGER recebem 403 no POST
- [ ] READ-ONLY respeitado (nunca escreve no Git)
- [ ] Logs auditáveis com `[US#72]`

### Integração
- [ ] Reutiliza ProjectService (US#48)
- [ ] Reutiliza BusinessRuleImportService (US#68)
- [ ] Reutiliza EmbeddingProvider (US#65)
- [ ] Reutiliza VectorStore (US#66)
- [ ] Reutiliza ASTCodeAnalysisService (US#69)
- [ ] Reutiliza GitPullRequestImpactService (US#51/52)

### Swagger
- [ ] Endpoint documentado
- [ ] 3 exemplos (Completo, Limitado, Falho)
- [ ] RBAC documentado
- [ ] Etapas descritas

---

## 🔧 Troubleshooting

### Erro 403 Forbidden

**Causa:** Usuário sem role ADMIN

**Solução:** Apenas ADMIN pode executar onboarding

### Status FAILED - Git connectivity failed

**Causa:** Token GitHub/GitLab inválido ou repositório privado sem acesso

**Solução:**
1. Verificar tokens configurados
2. Validar permissões de leitura no repositório
3. Testar conectividade manual

### rulesImported = 0

**Causa:** Nenhuma regra encontrada no repositório

**Solução:**
1. Verificar se há arquivos `.java` no repo
2. Validar padrões de detecção (US#68)
3. Executar importação manual para debug

### embeddingsIndexed = 0, ragStatus = LIMITED

**Causa:** Embedding provider não disponível

**Solução:**
1. Verificar configuração Sentence Transformer ou OpenAI
2. Aceitar limitação (sistema funciona em modo degradado)
3. RAG não será usado, mas análise básica funciona

### astCoverage = NONE

**Causa:** AST service não disponível

**Solução:**
1. Verificar JavaParser configurado
2. Aceitar limitação (análise sintática desabilitada)

---

## 🚀 Casos de Uso Reais

### Caso 1: Onboarding de Projeto Greenfield

**Cenário:** Projeto novo com código limpo

**Resultado Esperado:**
- rulesImported: 15-30
- embeddingsIndexed: igual a rulesImported
- astCoverage: PARTIAL ou FULL
- ragStatus: FULL
- baselineRisk: LOW ou MEDIUM
- limitations: []

### Caso 2: Onboarding de Sistema Legacy

**Cenário:** Sistema antigo, código complexo

**Resultado Esperado:**
- rulesImported: 50+
- embeddingsIndexed: pode falhar
- astCoverage: PARTIAL
- ragStatus: LIMITED
- baselineRisk: HIGH
- limitations: ["RAG disabled", "Complex AST"]

### Caso 3: Onboarding de Microserviço

**Cenário:** Serviço pequeno, foco específico

**Resultado Esperado:**
- rulesImported: 3-10
- embeddingsIndexed: igual a rulesImported
- astCoverage: FULL
- ragStatus: FULL
- baselineRisk: LOW
- limitations: []

---

## 📚 Referências

- **US#48:** Projects API
- **US#50:** Contextualização por Projeto
- **US#51/52:** Análise de Pull Request
- **US#65:** Embeddings
- **US#66:** Vector DB
- **US#68:** Importação de regras
- **US#69:** Análise AST
- **US#71:** Comparação PRE vs POST

---

## 🎯 Critério de Sucesso Enterprise

Um onboarding é considerado **bem-sucedido** se:

1. ✅ `status = "ONBOARDED"`
2. ✅ `rulesImported > 0`
3. ✅ `baselineRisk` definido
4. ✅ Logs completos com todas as 7 etapas
5. ✅ Projeto pronto para análise de PRs reais
6. ✅ READ-ONLY absoluto respeitado

**Nota:** Limitações (RAG, AST) são aceitáveis. O sistema opera em modo degradado mas funcional.
