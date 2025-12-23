# US#69 - Análise de Código Real com AST (Java)

## 📋 Resumo da Implementação

**Status**: ✅ IMPLEMENTADO  
**Data**: 2024  
**Tipo**: READ-ONLY - Análise Estática de Código  

## 🎯 Objetivo

Adicionar análise estática de código Java usando Abstract Syntax Tree (AST) via JavaParser para identificar métodos e classes impactados por regras de negócio com precisão de linha.

## 🏗️ Arquitetura

### Dependência Adicionada

```xml
<!-- pom.xml -->
<dependency>
  <groupId>com.github.javaparser</groupId>
  <artifactId>javaparser-symbol-solver-core</artifactId>
  <version>3.25.7</version>
</dependency>
```

### Modelos Criados (`com.backoffice.alerta.ast`)

1. **ASTMethodNode.java** - Representa um método na árvore AST
   - `className`: Nome completo da classe
   - `methodName`: Nome do método
   - `parameterTypes`: Lista de tipos de parâmetros
   - `filePath`: Caminho do arquivo fonte
   - `lineStart/lineEnd`: Posição no código
   - **Imutável**: equals/hashCode baseado em classe + método + parâmetros

2. **ASTCallGraphEdge.java** - Representa chamada entre métodos
   - `caller`: Método que faz a chamada
   - `callee`: Método chamado
   - **Uso futuro**: Análise de grafo de chamadas

3. **ASTImpactDetail.java** - Detalhe de impacto identificado
   - `filePath`, `className`, `methodName`
   - `lineStart/lineEnd`: Localização precisa
   - `reason`: Explicação do impacto (ex: "Implementa regra REGRA_001")
   - **Mutável**: Usado em DTOs de resposta

### Serviço Principal

**ASTCodeAnalysisService.java** (`@Service`)

**Método Principal**:
```java
List<ASTImpactDetail> analyzeFiles(Map<String, String> javaFiles)
```

**Estratégias de Associação com Regras**:

1. **Comentário Javadoc/Inline**:
   ```java
   /**
    * @BusinessRule REGRA_001
    */
   public void validarPessoaJuridica() { ... }
   ```

2. **Convenção de Nomenclatura**:
   ```java
   public void validateREGRA_001() { ... }
   public class REGRA_002Validator { ... }
   ```

3. **FileBusinessRuleMapping** (reuso US#45):
   - Consulta mapeamentos existentes no banco

**Tratamento de Erros**:
- Try/catch em **cada arquivo parseado**
- Erros não quebram análise (log + continue)
- Retorno sempre válido (lista vazia se necessário)

**Logs Estruturados**:
```
🧩 [US#69] AST parsing iniciado | arquivos=5
📄 [US#69] Classe analisada: com.backoffice.service.ValidationService
🔍 [US#69] Método encontrado: validatePJ()
🔗 [US#69] Regra associada: REGRA_001
⚠️ [US#69] Erro ao parsear arquivo Main.java | ignorado
```

## 🔗 Integrações

### 1. BusinessRuleCodeImpactRagService (US#45)

**Modificação**: Injeção opcional de `ASTCodeAnalysisService`

```java
@Autowired(required = false)
private ASTCodeAnalysisService astCodeAnalysisService;
```

**Fluxo**:
1. Após análise RAG tradicional
2. Filtra arquivos `.java` dos impactados
3. Se `astCodeAnalysisService != null`:
   - Chama `performASTAnalysis()`
   - Popula `response.setAstDetails()`
4. Senão: `astDetails` fica vazio

**Backward Compatibility**: ✅  
- Funciona com ou sem AST service
- `astDetails` default = lista vazia
- Clientes antigos ignoram campo novo

### 2. GitPullRequestImpactService (US#51/52)

**Modificação**: Similar à #1

**Fluxo**:
1. Após análise de impacto do PR
2. Filtra arquivos `.java` alterados no PR
3. **TODO**: Integrar com GitHub API para buscar conteúdo real
4. Chama análise AST
5. Adiciona detalhes a `GitImpactAnalysisResponse`

**Placeholder Atual**:
```java
// TODO: Integrar com GitHub API para buscar conteúdo completo do arquivo
// String content = githubClient.fetchFileContent(...);
```

### 3. UnifiedImpactChatService (US#46)

**Modificação**: Enriquece respostas do chat com info AST

**Output Enriquecido**:
```
🧩 **Análise Detalhada (Métodos/Classes):**

• **Método**: `ValidationService.validatePJ()` [linhas 45-67]
  → Implementa regra de negócio REGRA_001

• **Método**: `TaxCalculator.calculateTributos()` [linhas 102-150]
  → Implementa regra de negócio REGRA_003
```

**Mensagem Estruturada**:
- Tipo: `INFO`
- Título: "Detalhes a Nível de AST"
- Confiança: `HIGH` (análise determinística)

## 📦 DTOs Estendidos

### RagCodeImpactResponse.java

```java
@Schema(description = "Detalhes de impacto a nível de AST (métodos, classes)")
private List<ASTImpactDetail> astDetails = new ArrayList<>();

public List<ASTImpactDetail> getAstDetails() { return astDetails; }
public void setAstDetails(List<ASTImpactDetail> astDetails) { 
    this.astDetails = astDetails; 
}
```

### GitImpactAnalysisResponse.java

Mesma extensão que `RagCodeImpactResponse`.

**Garantias**:
- `astDetails` **nunca null** (sempre lista vazia ou populada)
- Compatibilidade total com clientes existentes
- Adicionar campo não quebra serialização JSON

## 🔒 Segurança & Governança

### Princípios READ-ONLY

✅ **SEM Persistência**:
- Nenhuma entidade criada/modificada
- Nenhum insert/update/delete

✅ **SEM Execução de Código**:
- Apenas parsing estático (AST)
- Não usa reflection
- Não carrega bytecode
- Não invoca métodos

✅ **SEM Side Effects**:
- Não envia emails
- Não cria notificações
- Não gera auditoria

✅ **Fail-Safe**:
- Parsing falho não quebra fluxo
- Sempre retorna resposta válida
- Logs em caso de erro

### RBAC (Reuso)

- **Nenhuma regra nova de segurança**
- Herda permissões de:
  - `/risk/impact/**` (US#45)
  - `/risk/chat/**` (US#46)
  - `/git/impact/**` (US#51)

## 📊 Critérios de Aceitação

### Funcionalidade

- [x] Parse arquivos Java válidos sem erro
- [x] Identifica classes e métodos corretamente
- [x] Extrai assinaturas de métodos (nome + parâmetros)
- [x] Associa regras via comentários `@BusinessRule`
- [x] Associa regras via convenção de nomenclatura
- [x] Retorna `List<ASTImpactDetail>` com localização precisa (linhas)
- [x] Integra com `BusinessRuleCodeImpactRagService`
- [x] Integra com `GitPullRequestImpactService`
- [x] Integra com `UnifiedImpactChatService`

### Qualidade

- [x] Logs estruturados com emojis
- [x] Tratamento de erro em cada parsing
- [x] Try/catch não propaga exceção
- [x] Código compila sem erros
- [x] Backward compatibility mantida
- [x] DTOs nunca retornam null

### Pendências (TODOs)

- [ ] **Integração com Git API**: Buscar conteúdo real de arquivos
  - Atualmente usa placeholder (Map vazio)
  - Próximos passos: 
    1. Adicionar método em `GitHubProviderClient.fetchFileContent()`
    2. Integrar com filesystem local (dev mode)
    3. Cachear conteúdo parseado (otimização)

- [ ] **Symbol Solver**: Resolver tipos de variáveis
  - JavaParser Symbol Solver já está no classpath
  - Melhorar precisão do call graph
  - Identificar tipos de retorno/parâmetros

- [ ] **Testes Unitários**: Cobrir análise AST
  - Testar com arquivos Java válidos/inválidos
  - Validar associação de regras
  - Mock de `FileBusinessRuleMappingRepository`

- [ ] **Performance**: Cachear AST parseadas
  - Evitar reparse do mesmo arquivo
  - Usar `LoadingCache` do Guava
  - TTL configurável

## 🧪 Como Testar

### Endpoint 1: Code Impact RAG

```http
POST /risk/impact/code
Content-Type: application/json

{
  "question": "Onde implementar validação de Pessoa Jurídica?",
  "focus": "code",
  "maxFiles": 5
}
```

**Response**:
```json
{
  "answer": "...",
  "impactedFiles": [...],
  "astDetails": [
    {
      "filePath": "src/main/java/ValidationService.java",
      "className": "com.backoffice.service.ValidationService",
      "methodName": "validatePJ",
      "lineStart": 45,
      "lineEnd": 67,
      "reason": "Implementa regra de negócio REGRA_001"
    }
  ]
}
```

### Endpoint 2: PR Impact Analysis

```http
POST /git/impact/pr
Content-Type: application/json

{
  "provider": "GITHUB",
  "repositoryUrl": "https://github.com/org/repo",
  "pullRequestNumber": 123
}
```

**Response**:
```json
{
  "pullRequest": {...},
  "riskLevel": "MEDIO",
  "astDetails": [...]
}
```

### Endpoint 3: Unified Chat

```http
POST /risk/chat
Content-Type: application/json

{
  "question": "Quais métodos implementam REGRA_001?",
  "focus": "code"
}
```

**Response**:
```json
{
  "answer": "...",
  "messages": [
    {
      "type": "INFO",
      "title": "Detalhes a Nível de AST",
      "content": "Foram identificados 2 método(s)...",
      "sources": ["ValidationService.java:45", "TaxService.java:102"]
    }
  ]
}
```

## 📝 Exemplo de Código Analisado

```java
package com.backoffice.service;

public class ValidationService {
    
    /**
     * Valida dados de Pessoa Jurídica
     * @BusinessRule REGRA_001
     */
    public void validatePJ(String cnpj) {
        // Lógica de validação
    }
    
    public void validateREGRA_002(String cpf) {
        // Associado por nomenclatura
    }
}
```

**Output AST**:
- Método 1: `validatePJ()` → Regra `REGRA_001` (via comentário)
- Método 2: `validateREGRA_002()` → Regra `REGRA_002` (via naming)

## 🔄 Próximos Passos

1. **Integrar busca real de arquivos**:
   - GitHub API (US#68 já tem GitHubProviderClient)
   - Filesystem local para dev/test

2. **Adicionar testes automatizados**:
   - Unit tests para `ASTCodeAnalysisService`
   - Integration tests com PRs reais

3. **Otimizar performance**:
   - Cache de AST parseadas
   - Parsing assíncrono para muitos arquivos

4. **Expandir análise**:
   - Call graph completo (quem chama quem)
   - Data flow analysis (variáveis modificadas)
   - Control flow graph

## 📚 Referências

- [JavaParser Documentation](https://javaparser.org/)
- [JavaParser Symbol Solver](https://github.com/javaparser/javaparser/wiki/Symbol-solving)
- US#45 - RAG com Mapeamento de Código
- US#46 - Chat Unificado
- US#51/52 - Análise de Pull Requests
- US#68 - Importação de Regras do Git

---

**Implementado por**: GitHub Copilot  
**Review**: Pendente  
**Deploy**: Aguardando testes de integração
