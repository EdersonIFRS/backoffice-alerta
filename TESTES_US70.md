# Testes US#70 - LLM Change Detection

## Endpoints Disponíveis

### 1. POST /risk/llm/changes/analyze
Analisa mudanças de um Pull Request em busca de padrões LLM

### 2. GET /risk/llm/changes/cicd-status/{pullRequestId}
Retorna exit code para integração CI/CD

---

## Teste 1: Análise Básica (PowerShell)

```powershell
# Teste LOW RISK
$body = @{
    pullRequestId = "123"
    provider = "GITHUB"
    repositoryUrl = "https://github.com/test/repo"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/risk/llm/changes/analyze" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body
```

## Teste 2: Com Projeto Específico (PowerShell)

```powershell
$body = @{
    projectId = "123e4567-e89b-12d3-a456-426614174000"
    pullRequestId = "456"
    provider = "GITHUB"
    repositoryUrl = "https://github.com/org/repo"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/risk/llm/changes/analyze" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body
```

## Teste 3: Status CI/CD (PowerShell)

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/risk/llm/changes/cicd-status/123" `
    -Method GET
```

---

## Testes com CURL (Bash/Git Bash)

### Análise Básica
```bash
curl -X POST http://localhost:8080/risk/llm/changes/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "pullRequestId": "123",
    "provider": "GITHUB",
    "repositoryUrl": "https://github.com/test/repo"
  }'
```

### Com Projeto
```bash
curl -X POST http://localhost:8080/risk/llm/changes/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "123e4567-e89b-12d3-a456-426614174000",
    "pullRequestId": "456",
    "provider": "GITHUB",
    "repositoryUrl": "https://github.com/org/repo"
  }'
```

### Status CI/CD
```bash
curl http://localhost:8080/risk/llm/changes/cicd-status/123
```

---

## Teste 4: Validar Resposta Completa (PowerShell com saída formatada)

```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/risk/llm/changes/analyze" `
    -Method POST `
    -ContentType "application/json" `
    -Body (@{
        pullRequestId = "789"
        provider = "GITHUB"
        repositoryUrl = "https://github.com/example/project"
    } | ConvertTo-Json)

# Exibir resultado formatado
Write-Host "`n=== Análise LLM Change Detection ===" -ForegroundColor Cyan
Write-Host "PR ID: $($response.pullRequestId)"
Write-Host "Score Total: $($response.totalScore)/100" -ForegroundColor $(
    if ($response.totalScore -ge 60) { "Red" } 
    elseif ($response.totalScore -ge 30) { "Yellow" } 
    else { "Green" }
)
Write-Host "Nível de Suspeição: $($response.suspicionLevel)"
Write-Host "Arquivos Analisados: $($response.totalFilesAnalyzed)"
Write-Host "Arquivos Java: $($response.javaFilesAnalyzed)"
Write-Host "`nHeurísticas Detectadas:"
foreach ($h in $response.heuristics) {
    Write-Host "  - $($h.heuristic): +$($h.score) pontos"
    Write-Host "    $($h.explanation)"
}
Write-Host "`nSumário:" -ForegroundColor Cyan
Write-Host $response.summary
```

---

## Cenários de Teste

### Cenário LOW RISK (Score esperado: 0-29)
- Pull Request pequeno com poucos arquivos
- Mudanças dentro do escopo
- Sem padrões suspeitos

### Cenário MEDIUM RISK (Score esperado: 30-59)
- Arquivos fora de escopo detectados (+30 pts)
- Comentários genéricos (+15 pts)
- Padrões repetitivos (+10 pts)

### Cenário HIGH RISK (Score esperado: 60+)
- Métodos massivamente alterados (+25 pts)
- Mudanças críticas sem testes (+20 pts)
- Fora de escopo (+30 pts)
- Refatoração perfeita suspeita (+10 pts)

---

## Verificar se Backend está Rodando

```powershell
# Testar conectividade
Test-NetConnection -ComputerName localhost -Port 8080

# Ou verificar processos Java
Get-Process -Name java -ErrorAction SilentlyContinue

# Verificar porta 8080
netstat -ano | findstr :8080
```

---

## Swagger UI

Acesse: http://localhost:8080/swagger-ui/index.html

Procure pela tag **"LLM Change Detection"** e teste os endpoints diretamente na interface.

**Nota sobre CORS**: Se o Swagger apresentar erro "Failed to fetch", use os comandos PowerShell/curl acima para testar diretamente.

---

## Exemplo de Resposta Esperada

```json
{
  "totalScore": 45,
  "suspicionLevel": "MEDIUM",
  "heuristics": [
    {
      "heuristic": "OUT_OF_SCOPE",
      "score": 30,
      "explanation": "Detectados 2 arquivo(s) alterado(s) fora do escopo das regras de negócio impactadas.",
      "affectedFiles": [
        "src/main/java/UnrelatedService.java",
        "src/main/java/RandomUtil.java"
      ]
    },
    {
      "heuristic": "GENERIC_COMMENTS",
      "score": 15,
      "explanation": "Detectados 1 arquivo(s) com potencial para comentários genéricos.",
      "affectedFiles": ["src/main/java/NewHelper.java"]
    }
  ],
  "affectsCriticalRule": false,
  "exceedsRuleScope": true,
  "pullRequestId": "123",
  "totalFilesAnalyzed": 4,
  "javaFilesAnalyzed": 3,
  "summary": "📊 **Análise de Mudança LLM - PR #123**\n\n**Score Total**: 45/100\n**Nível de Suspeição**: MEDIUM\n\n⚠️ **CUIDADO**: Risco moderado detectado. Revisão manual recomendada.",
  "projectContext": {
    "scoped": false,
    "global": true
  }
}
```

---

## Integração CI/CD (US#53)

```yaml
# Exemplo GitHub Actions
- name: Check LLM Risk
  run: |
    RESPONSE=$(curl -s http://api/risk/llm/changes/cicd-status/${{ github.event.pull_request.number }})
    EXIT_CODE=$(echo $RESPONSE | jq -r '.exitCode')
    
    if [ $EXIT_CODE -eq 2 ]; then
      echo "❌ HIGH RISK - Bloqueando merge"
      exit 1
    elif [ $EXIT_CODE -eq 1 ]; then
      echo "⚠️ MEDIUM RISK - Revisão recomendada"
    else
      echo "✅ LOW RISK - OK para merge"
    fi
```

---

## Troubleshooting

### Erro "Failed to fetch" no Swagger
1. Verificar se backend está rodando: `Test-NetConnection localhost -Port 8080`
2. Testar diretamente com PowerShell (comandos acima)
3. Verificar logs do backend

### Erro 401/403
- Endpoint requer autenticação (bearerAuth)
- Roles necessárias: ADMIN, RISK_MANAGER ou ENGINEER
- Desabilitar segurança temporariamente para testes (não recomendado em produção)

### Erro 404
- Verificar se rota está correta: `/risk/llm/changes/analyze`
- Confirmar que SecurityConfig inclui `/risk/llm/**`
