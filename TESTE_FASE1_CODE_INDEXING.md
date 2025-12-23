# 🧪 Guia de Testes - Fase 1: Indexação de Código

**Data:** 23/12/2024  
**Feature:** Code Indexing com Embeddings OpenAI  
**Status:** ✅ Habilitado (`rag.code.enabled=true`)

---

## 📋 Checklist de Testes

### ✅ Pré-requisitos
- [x] Migration V13 aplicada
- [x] Tabelas `code_file_embeddings` e `code_business_rule_mapping` criadas
- [x] CodeIndexingService compilado e deployado
- [x] Feature flag `rag.code.enabled=true`
- [x] Backend iniciado (Started Application in 36.248 seconds)

---

## 🔬 Teste 1: Verificar Estrutura do Banco

### Verificar tabelas criadas
```bash
docker compose exec postgres psql -U postgres -d backoffice_alerta -c "\dt code_*"
```

**Resultado Esperado:**
```
 Schema |            Name            | Type  
--------+----------------------------+-------
 public | code_business_rule_mapping | table
 public | code_file_embeddings       | table
```

### Verificar estrutura da tabela
```bash
docker compose exec postgres psql -U postgres -d backoffice_alerta -c "\d code_file_embeddings"
```

**Resultado Esperado:**
- Colunas: file_path, project_id, content, language, embedding, dimension, provider, created_at, updated_at
- Índices: idx_code_file_project, idx_code_file_language

---

## 🔬 Teste 2: Executar Onboarding com Indexação

### 2.1. Verificar que projeto tem regras com sourceFile

```bash
docker compose exec postgres psql -U postgres -d backoffice_alerta -c "
SELECT id, name, source_file 
FROM business_rule 
WHERE source_file IS NOT NULL 
LIMIT 5;
"
```

**Esperado:** Regras existentes devem ter `source_file` preenchido.

### 2.2. Executar novo onboarding

**Endpoint:** `POST /api/v1/business-rules/import`

**Payload:**
```json
{
  "projectId": "<UUID_DO_PROJETO>",
  "repositoryUrl": "https://github.com/seu-repo/projeto",
  "branch": "main",
  "gitProvider": "GITHUB",
  "dryRun": false
}
```

### 2.3. Verificar logs do CodeIndexingService

```bash
docker compose logs backend -f | Select-String -Pattern "CodeIndexing|indexed code"
```

**Resultado Esperado:**
```
Successfully indexed code for rule RULE_001: src/main/java/...
Successfully indexed code for rule RULE_002: docs/rules/...
Code indexing complete: 8 indexed, 0 skipped
```

### 2.4. Verificar dados indexados

```bash
docker compose exec postgres psql -U postgres -d backoffice_alerta -c "
SELECT 
    file_path, 
    language, 
    LENGTH(content) as content_size,
    dimension,
    provider,
    created_at 
FROM code_file_embeddings 
LIMIT 5;
"
```

**Esperado:**
- Arquivos .java, .ts, .md indexados
- `language` detectado corretamente (java, typescript, markdown)
- `dimension` = 384 (tamanho do embedding)
- `provider` = 'openai'

### 2.5. Verificar mapeamentos código ↔ regras

```bash
docker compose exec postgres psql -U postgres -d backoffice_alerta -c "
SELECT 
    m.file_path,
    m.business_rule_id,
    m.confidence,
    m.relationship_type,
    m.detection_method
FROM code_business_rule_mapping m
LIMIT 5;
"
```

**Esperado:**
- `confidence` = 1.0 (alta confiança)
- `relationship_type` = 'DIRECT'
- `detection_method` = 'SOURCE_FILE_FIELD'

---

## 🔬 Teste 3: Busca Semântica em Código

### 3.1. Teste via SQL (Busca por Similaridade)

⚠️ **Nota:** A busca semântica em código ainda não está integrada no RAG (será Fase 1.5).  
Por enquanto, podemos testar diretamente no banco:

```bash
docker compose exec postgres psql -U postgres -d backoffice_alerta -c "
SELECT 
    file_path,
    language,
    SUBSTRING(content, 1, 100) as snippet
FROM code_file_embeddings
WHERE language = 'java'
LIMIT 3;
"
```

---

## 🔬 Teste 4: Chat com Context Enriquecido

### 4.1. Verificar que sourceFile está no contexto RAG

**Query no Chat:** "Qual o sourceFile da regra de autenticação?"

**Verificar nos logs:**
```bash
docker compose logs backend --tail 100 | Select-String -Pattern "sourceFile"
```

**Esperado:** Logs devem mostrar que `sourceFile` está sendo enviado ao ChatGPT no contexto.

### 4.2. Perguntar sobre código específico

**Queries de teste:**
1. "Em qual arquivo está implementada a regra AUTH_001?"
2. "Mostre o sourceFile de todas as regras de autenticação"
3. "Quais regras estão no arquivo UserService.java?"

**Resultado Esperado:**
- ChatGPT deve responder com o caminho do arquivo (`sourceFile`)
- Resposta deve ser precisa (baseada no contexto enviado)

---

## 🔬 Teste 5: Validar Detecção de Linguagem

### Criar regra de teste com diferentes extensões

```sql
-- Via onboarding ou manualmente:
INSERT INTO business_rule (id, name, domain, description, content, source_file, criticality, owner)
VALUES 
  ('TEST_JAVA', 'Test Java', 'TECHNICAL', 'Test', 'public class Test {}', 'src/Test.java', 'MEDIUM', 'system'),
  ('TEST_TS', 'Test TypeScript', 'TECHNICAL', 'Test', 'const x = 1;', 'src/app.ts', 'MEDIUM', 'system'),
  ('TEST_PY', 'Test Python', 'TECHNICAL', 'Test', 'def hello(): pass', 'scripts/hello.py', 'MEDIUM', 'system');
```

### Executar indexação manual (via novo onboarding)

### Verificar linguagens detectadas

```bash
docker compose exec postgres psql -U postgres -d backoffice_alerta -c "
SELECT file_path, language FROM code_file_embeddings 
WHERE file_path LIKE '%Test%' OR file_path LIKE '%hello%';
"
```

**Esperado:**
- `src/Test.java` → `language = 'java'`
- `src/app.ts` → `language = 'typescript'`
- `scripts/hello.py` → `language = 'python'`

---

## 🔬 Teste 6: Validar Feature Flag

### 6.1. Desabilitar indexação

```yaml
# Em application-web.yml
rag:
  code:
    enabled: false  # Desabilitar
```

### 6.2. Rebuild e restart

```bash
docker compose up -d --build backend
```

### 6.3. Executar onboarding novamente

**Esperado:**
- CodeIndexingService **NÃO** deve ser inicializado
- Logs **NÃO** devem mostrar "Successfully indexed code"
- Tabela `code_file_embeddings` **NÃO** deve receber novos registros

### 6.4. Reabilitar para continuar testes

```yaml
rag:
  code:
    enabled: true  # Reabilitar
```

---

## 🔬 Teste 7: Validar Limite de Tamanho

### 7.1. Configurar limite baixo (para teste)

```yaml
rag:
  code:
    max-file-size-kb: 1  # 1KB para forçar erro
```

### 7.2. Tentar indexar arquivo grande (>1KB)

**Esperado:**
- Arquivo deve ser ignorado
- Log: "File too large, skipping: X KB > 1 KB"

⚠️ **Nota:** Esta validação ainda não foi implementada. Será necessário adicionar no CodeIndexingService se quiser testar.

---

## 📊 Métricas de Sucesso

| Métrica | Meta | Status |
|---------|------|--------|
| Regras com sourceFile | 100% das regras onboarded | ⚠️ Verificar |
| Arquivos indexados | = Total de regras com sourceFile | ⚠️ Verificar |
| Mapeamentos criados | = Total de arquivos indexados | ⚠️ Verificar |
| Embeddings gerados | dimension=384, provider=openai | ⚠️ Verificar |
| Linguagens detectadas | java, typescript, markdown, etc | ⚠️ Verificar |
| Tempo de indexação | <5s por regra | ⚠️ Medir |
| ChatGPT responde com sourceFile | 100% das queries válidas | ⚠️ Testar |

---

## 🐛 Problemas Conhecidos / Limitações

1. **Busca semântica em código ainda não integrada no RAG**
   - CodeIndexingService tem método `searchCode()` implementado
   - Mas UnifiedImpactChatService ainda não chama esse método
   - **Solução:** Implementar integração (Fase 1.5)

2. **Validação de tamanho de arquivo não implementada**
   - Config `max-file-size-kb` existe mas não é validada
   - **Solução:** Adicionar validação em `indexBusinessRuleCode()`

3. **Sem UI para visualizar código indexado**
   - Dados estão no banco mas sem frontend
   - **Solução:** Criar endpoint GET `/api/v1/code-files` (Fase 2)

4. **Embeddings não são atualizados automaticamente**
   - Se sourceFile muda no Git, embedding fica desatualizado
   - **Solução:** Implementar re-indexação periódica (Fase 2)

---

## ✅ Resultado dos Testes

| Teste | Status | Observações |
|-------|--------|-------------|
| T1: Estrutura DB | ⚠️ Pendente | Executar comandos SQL |
| T2: Onboarding | ⚠️ Pendente | Executar POST /import |
| T3: Busca Semântica | ⚠️ Pendente | Testar SQL |
| T4: Chat Context | ⚠️ Pendente | Testar queries |
| T5: Detecção Linguagem | ⚠️ Pendente | Criar regras teste |
| T6: Feature Flag | ⚠️ Pendente | Toggle enable/disable |
| T7: Limite Tamanho | ⚠️ Não Implementado | Adicionar validação |

---

## 🚀 Próximos Passos (Fase 1.5 - Opcional)

1. **Integrar searchCode() no RAG**
   - Modificar `UnifiedImpactChatService` para buscar em `code_file_embeddings`
   - Merge resultados de business_rule_embeddings + code_file_embeddings

2. **Adicionar validação de tamanho**
   ```java
   if (content.length() > maxFileSizeKb * 1024) {
       logger.warn("File too large: {} ({} KB)", filePath, content.length() / 1024);
       return;
   }
   ```

3. **Criar endpoint de consulta**
   ```java
   @GetMapping("/api/v1/code-files")
   public List<CodeFileDTO> listCodeFiles(
       @RequestParam(required = false) String language,
       @RequestParam(required = false) String businessRuleId
   ) { ... }
   ```

4. **Dashboard de Code Indexing**
   - Total de arquivos indexados
   - Distribuição por linguagem
   - Arquivos mais referenciados

---

## 📞 Suporte

Se encontrar problemas durante os testes:

1. **Verificar logs:**
   ```bash
   docker compose logs backend -f | Select-String -Pattern "CodeIndexing|ERROR"
   ```

2. **Verificar estado do banco:**
   ```bash
   docker compose exec postgres psql -U postgres -d backoffice_alerta
   ```

3. **Rollback se necessário:**
   ```yaml
   rag.code.enabled: false  # Desabilitar feature
   ```

---

**Última atualização:** 23/12/2024  
**Versão:** 1.0.0  
**Feature Flag:** ✅ HABILITADO (`rag.code.enabled=true`)
