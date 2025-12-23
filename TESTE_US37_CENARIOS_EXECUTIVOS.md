# 🧪 Teste US#37 - Visualização de Impacto Sistêmico

## 📋 Cenários de Teste para Gestores

### 🎯 Objetivo
Demonstrar como a visualização de impacto ajuda gestores a entenderem o **efeito cascata** de mudanças no código.

---

## 🔹 Cenário 1: "E se mudarmos o PaymentService?"

### 📖 Contexto Executivo
> *"Precisamos alterar a regra de cálculo de horas PJ no PaymentService.java.  
> Quais outras regras serão afetadas? Tem risco de afetar faturamento?"*

### 🔧 Como Testar (Frontend)

1. **Login**: `admin` / `admin123`
2. **Acessar**: Menu lateral → **"Impacto Sistêmico"** (ícone de hub)
3. **Preencher formulário**:
   ```
   PR ID: PR-2025-001
   Arquivos Alterados:
   src/main/java/com/app/payment/PaymentService.java
   ```
4. **Clicar**: "Gerar Grafo de Impacto"

### ✅ Resultado Esperado

**📊 Sumário Executivo**:
- Total de Regras: `3`
- Impacto Direto: `1` (azul)
- Impacto Indireto: `1` (laranja)
- Impacto Cascata: `1` (vermelho)

**📍 Regras Impactadas**:

1. **REGRA_CALCULO_HORAS_PJ** (DIRECT - Azul)
   - Criticidade: **CRÍTICA**
   - Domínio: PAYMENT
   - Ownership: Platform Team
   - ⚠️ **Borda grossa** (regra crítica)

2. **REGRA_CALCULO_TRIBUTOS** (INDIRECT - Laranja)
   - Criticidade: ALTA
   - Domínio: BILLING
   - Ownership: Finance Team

3. **REGRA_VALIDACAO_CADASTRO_USUARIO** (CASCADE - Vermelho)
   - Criticidade: MÉDIA
   - Domínio: USER
   - Ownership: User Experience Team

**➡️ Dependências**:
- `BR-001 → BR-003` (tipo: FEEDS)
- `BR-003 → BR-004` (tipo: AGGREGATES)

### 💡 Interpretação para Gestor
> **"Mudamos Payment → afeta Billing → afeta User"**
>
> - ✅ Impacto direto é crítico (atenção total)
> - ⚠️ Finance Team precisa ser avisado (indireto)
> - ℹ️ User Experience Team deve validar (cascata)

---

## 🔹 Cenário 2: "E se mudarmos o TaxCalculator?"

### 📖 Contexto Executivo
> *"Vamos ajustar impostos no TaxCalculator.java.  
> Isso afeta só Billing ou tem efeito em outros módulos?"*

### 🔧 Como Testar (Frontend)

1. **Preencher formulário**:
   ```
   PR ID: PR-2025-002
   Arquivos Alterados:
   src/main/java/com/app/billing/TaxCalculator.java
   ```
2. **Clicar**: "Gerar Grafo de Impacto"

### ✅ Resultado Esperado

**📊 Sumário Executivo**:
- Total de Regras: `2`
- Impacto Direto: `1` (azul)
- Impacto Indireto: `1` (laranja)
- Impacto Cascata: `0` (vermelho)

**📍 Regras Impactadas**:

1. **REGRA_CALCULO_TRIBUTOS** (DIRECT - Azul)
   - Criticidade: ALTA
   - Domínio: BILLING

2. **REGRA_VALIDACAO_CADASTRO_USUARIO** (INDIRECT - Laranja)
   - Criticidade: MÉDIA
   - Domínio: USER

### 💡 Interpretação para Gestor
> **"Mudamos Billing → afeta User (mas menos risco)"**
>
> - ✅ Sem regras críticas no caminho
> - ℹ️ Impacto menor (sem cascata)
> - ✅ Mais seguro que Cenário 1

---

## 🔹 Cenário 3: Teste de Filtros

### 🎯 Objetivo
Mostrar como filtrar apenas impactos relevantes.

### 🔧 Como Testar

1. **Gerar grafo** do Cenário 1 novamente
2. **Aplicar filtros**:
   - ✅ **"Apenas Críticas"** → Mostra só BR-001
   - ✅ **"Apenas Cascata"** → Mostra só BR-004
   - ✅ **Domínio: PAYMENT** → Mostra só BR-001

### 💡 Interpretação para Gestor
> **Filtros ajudam focar no que importa:**
> - Ver só regras críticas (menos ruído)
> - Ver só efeitos cascata (riscos ocultos)
> - Filtrar por domínio (responsabilidade)

---

## 🧪 Teste Manual via API (Opcional - Para Devs)

### 1️⃣ Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Copiar o `token` retornado.**

### 2️⃣ Gerar Grafo de Impacto
```bash
curl -X POST http://localhost:8080/api/risk/business-impact/graph \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -d '{
    "pullRequestId": "PR-2025-001",
    "changedFiles": [
      "src/main/java/com/app/payment/PaymentService.java"
    ]
  }'
```

### ✅ Resposta Esperada (JSON)
```json
{
  "pullRequestId": "PR-2025-001",
  "nodes": [
    {
      "ruleId": "550e8400-e29b-41d4-a716-446655440001",
      "ruleName": "REGRA_CALCULO_HORAS_PJ",
      "domain": "PAYMENT",
      "criticality": "CRITICA",
      "impactLevel": "DIRECT",
      "ownerships": [
        {"teamName": "Platform Team", "role": "PRIMARY_OWNER"}
      ],
      "hasIncidents": false
    },
    {
      "ruleId": "550e8400-e29b-41d4-a716-446655440003",
      "ruleName": "REGRA_CALCULO_TRIBUTOS",
      "domain": "BILLING",
      "criticality": "ALTA",
      "impactLevel": "INDIRECT",
      "ownerships": [
        {"teamName": "Finance Team", "role": "PRIMARY_OWNER"}
      ],
      "hasIncidents": false
    },
    {
      "ruleId": "550e8400-e29b-41d4-a716-446655440004",
      "ruleName": "REGRA_VALIDACAO_CADASTRO_USUARIO",
      "domain": "USER",
      "criticality": "MEDIA",
      "impactLevel": "CASCADE",
      "ownerships": [
        {"teamName": "User Experience Team", "role": "PRIMARY_OWNER"}
      ],
      "hasIncidents": false
    }
  ],
  "edges": [
    {
      "sourceRuleId": "550e8400-e29b-41d4-a716-446655440001",
      "targetRuleId": "550e8400-e29b-41d4-a716-446655440003",
      "dependencyType": "FEEDS"
    },
    {
      "sourceRuleId": "550e8400-e29b-41d4-a716-446655440003",
      "targetRuleId": "550e8400-e29b-41d4-a716-446655440004",
      "dependencyType": "AGGREGATES"
    }
  ],
  "summary": {
    "totalRules": 3,
    "direct": 1,
    "indirect": 1,
    "cascade": 1,
    "criticalRules": 1,
    "requiresExecutiveAttention": false
  }
}
```

---

## 🎯 Critérios de Aceite (Checklist)

### Frontend
- [ ] Menu "Impacto Sistêmico" visível para ADMIN, RISK_MANAGER, ENGINEER
- [ ] Menu **não visível** para VIEWER
- [ ] Formulário aceita PR ID e lista de arquivos
- [ ] Botão "Gerar Grafo" chama API corretamente
- [ ] Sumário exibe 4 cards (Total, Direto, Indireto, Cascata)
- [ ] Nós exibem cor correta (azul/laranja/vermelho)
- [ ] Regras CRÍTICAS têm borda grossa
- [ ] Ownerships aparecem como chips
- [ ] Filtros funcionam (Apenas Críticas, Apenas Cascata, Domínio)
- [ ] Dependências listadas com setas visuais (→)

### Backend
- [ ] Endpoint POST `/risk/business-impact/graph` responde
- [ ] RBAC bloqueia VIEWER
- [ ] 3 regras criadas no seed (BR-001, BR-003, BR-004)
- [ ] 2 dependências criadas (BR-001→BR-003, BR-003→BR-004)
- [ ] 3 mapeamentos criados (arquivos → regras)
- [ ] Resposta JSON contém nodes, edges, summary
- [ ] ImpactLevel correto (DIRECT, INDIRECT, CASCADE)

---

## 📝 Feedback Esperado de Gestores

### ✅ Positivo
- "Agora entendo o impacto cascata antes de aprovar"
- "As cores ajudam a ver o risco rapidamente"
- "Filtro por críticas é essencial"
- "Ver ownership ajuda saber quem avisar"

### ⚠️ Atenção
- "Preciso entender melhor os tipos de dependência (FEEDS, AGGREGATES)"
- "Grafo com muitas regras pode ficar confuso" (limite de 3 níveis ajuda)
- "Seria bom exportar para PDF/PNG" (melhoria futura)

---

## 🚀 Resumo Executivo

**US#37 implementa:**
- ✅ Visualização de impacto sistêmico (mapa de dependências)
- ✅ Código de cores executivo (azul/laranja/vermelho)
- ✅ Filtros por criticidade, impacto e domínio
- ✅ Alertas visuais para regras críticas
- ✅ Informação de ownership (quem avisar)
- ✅ RBAC: apenas gestores/engenheiros veem

**Benefício para o negócio:**
> **Reduz risco de aprovar PRs sem entender impacto completo.**  
> Gestores veem visualmente: "mudei A → afeta B → afeta C".
