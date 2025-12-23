# US#52 - Integração Real com GitHub e GitLab

## 📋 Resumo

Implementação de integração **READ-ONLY** com GitHub e GitLab para leitura de Pull Requests reais, mantendo 100% de compatibilidade com o comportamento existente.

---

## ✅ Funcionalidades Implementadas

### 1️⃣ GitHubProviderClient
- ✅ Implementação real usando GitHub REST API
- ✅ Autenticação via Personal Access Token
- ✅ Busca dados básicos do Pull Request
- ✅ Busca arquivos alterados
- ✅ Mapeamento de status (OPEN, MERGED, CLOSED)
- ✅ Tratamento de erros (401, 403, 404)
- ✅ Logs informativos

### 2️⃣ GitLabProviderClient
- ✅ Implementação real usando GitLab API v4
- ✅ Autenticação via Personal Access Token
- ✅ Busca dados básicos do Merge Request
- ✅ Busca arquivos alterados (changes)
- ✅ Mapeamento de status (OPEN, MERGED, CLOSED)
- ✅ Tratamento de erros (401, 403, 404)
- ✅ Logs informativos

### 3️⃣ Seleção Dinâmica de Provider
**Modificado:** `GitPullRequestImpactService`

Lógica implementada:
```java
if (provider == GITHUB && token configurado) {
    → GitHubProviderClient (REAL)
}
else if (provider == GITLAB && token configurado) {
    → GitLabProviderClient (REAL)
}
else {
    → DummyGitProviderClient (FALLBACK)
}
```

### 4️⃣ Configuração de Tokens
**Arquivos atualizados:**
- `application-dev.yml`
- `application-demo.yml`
- `application-prod.yml`

```yaml
git:
  github:
    token: ${GITHUB_TOKEN:}
  gitlab:
    token: ${GITLAB_TOKEN:}
```

### 5️⃣ Documentação Swagger
**Atualizado:** `GitPullRequestImpactController`

- ✅ Exemplos para GitHub REAL
- ✅ Exemplos para GitLab REAL
- ✅ Exemplos de fallback (Dummy)
- ✅ Instruções de configuração de tokens

---

## 🔐 Segurança

### Tokens
- ❌ **NUNCA** hardcoded
- ✅ Apenas via variáveis de ambiente
- ✅ Valores vazios por padrão (`${GITHUB_TOKEN:}`)

### Permissões Necessárias

**GitHub Token (Personal Access Token):**
- `repo` - Acesso a repositórios privados
- `read:user` - Informações do usuário

**GitLab Token (Personal Access Token):**
- `read_api` - Leitura via API
- `read_repository` - Leitura de repositórios

---

## 🚀 Como Usar

### 1. Configurar Tokens (Opcional)

**Linux/Mac:**
```bash
export GITHUB_TOKEN="ghp_yourGitHubTokenHere"
export GITLAB_TOKEN="glpat_yourGitLabTokenHere"
```

**Windows (PowerShell):**
```powershell
$env:GITHUB_TOKEN="ghp_yourGitHubTokenHere"
$env:GITLAB_TOKEN="glpat_yourGitLabTokenHere"
```

### 2. Executar Aplicação

```bash
# Com tokens configurados
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Ou via JAR
java -jar target/backoffice-alerta.jar --spring.profiles.active=dev
```

### 3. Testar Endpoint

**Com GitHub (REAL):**
```bash
curl -X POST http://localhost:8080/risk/git/pull-request/analyze \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "provider": "GITHUB",
    "repositoryUrl": "https://github.com/owner/repo",
    "pullRequestNumber": "123"
  }'
```

**Com GitLab (REAL):**
```bash
curl -X POST http://localhost:8080/risk/git/pull-request/analyze \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "provider": "GITLAB",
    "repositoryUrl": "https://gitlab.com/owner/repo",
    "pullRequestNumber": "456"
  }'
```

**Sem Token (Fallback para Dummy):**
```bash
# Sem configurar GITHUB_TOKEN ou GITLAB_TOKEN
# Sistema usa automaticamente DummyGitProviderClient
curl -X POST http://localhost:8080/risk/git/pull-request/analyze \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "provider": "GITHUB",
    "repositoryUrl": "https://github.com/demo/example",
    "pullRequestNumber": "999"
  }'
```

---

## 📊 Logs

### GitHub REAL
```
🔗 [US#52] Usando GitHubProviderClient REAL
🔗 [GITHUB] Conectando ao GitHub para buscar PR #123 do repositório https://github.com/owner/repo
📄 [GITHUB] Pull Request encontrado: feat: Adicionar validação
📂 [GITHUB] 5 arquivo(s) alterado(s) carregados
✅ Análise concluída: 3 regra(s) impactada(s)
```

### GitLab REAL
```
🔗 [US#52] Usando GitLabProviderClient REAL
🔗 [GITLAB] Conectando ao GitLab para buscar MR #456 do repositório https://gitlab.com/owner/repo
📄 [GITLAB] Merge Request encontrado: fix: Corrigir cálculo
📂 [GITLAB] 3 arquivo(s) alterado(s) carregados
✅ Análise concluída: 2 regra(s) impactada(s)
```

### Fallback (Dummy)
```
🔄 [US#52] Fallback para DummyGitProviderClient (token não configurado)
🔍 [DUMMY] Buscando PR #999 do repositório https://github.com/demo/example (GITHUB)
📄 [DUMMY] PR PR-2024-999 retornado com 4 arquivo(s) alterado(s)
✅ Análise concluída: 1 regra(s) impactada(s)
```

### Erros Comuns
```
❌ [GITHUB] Token inválido ou expirado (401 Unauthorized)
❌ [GITHUB] Acesso negado - verifique permissões do token (403 Forbidden)
❌ [GITHUB] Repositório ou PR não encontrado (404 Not Found)
⚠️ [GITHUB] Token não configurado. Use variável de ambiente GITHUB_TOKEN
```

---

## ⚠️ Garantias READ-ONLY

### ✅ O que a US#52 FAZ:
- ✅ Lê metadados de Pull Requests
- ✅ Busca lista de arquivos alterados
- ✅ Identifica status do PR/MR
- ✅ Extrai autor, branches, título

### ❌ O que a US#52 NÃO FAZ:
- ❌ Criar commits
- ❌ Comentar em PRs/MRs
- ❌ Criar webhooks
- ❌ Persistir dados Git
- ❌ Alterar código externo
- ❌ Fazer merge
- ❌ Aprovar/rejeitar PRs

---

## 🧪 Testes

### Cenário 1: GitHub com Token Válido
- Provider: `GITHUB`
- Token: Configurado
- Resultado: ✅ Usa `GitHubProviderClient` (REAL)

### Cenário 2: GitLab com Token Válido
- Provider: `GITLAB`
- Token: Configurado
- Resultado: ✅ Usa `GitLabProviderClient` (REAL)

### Cenário 3: GitHub sem Token
- Provider: `GITHUB`
- Token: Não configurado
- Resultado: ✅ Usa `DummyGitProviderClient` (FALLBACK)

### Cenário 4: GitLab sem Token
- Provider: `GITLAB`
- Token: Não configurado
- Resultado: ✅ Usa `DummyGitProviderClient` (FALLBACK)

### Cenário 5: Token Inválido
- Token: Expirado ou sem permissões
- Resultado: ❌ Erro 401/403 com mensagem clara

### Cenário 6: PR Inexistente
- PR Number: 999999 (não existe)
- Resultado: ❌ Erro 404 com mensagem clara

---

## 📂 Arquivos Criados/Modificados

### Criados:
1. `GitHubProviderClient.java` - Integração real com GitHub
2. `GitLabProviderClient.java` - Integração real com GitLab
3. `US52_INTEGRACAO_GIT.md` - Esta documentação

### Modificados:
1. `GitPullRequestImpactService.java` - Seleção dinâmica de provider
2. `application-dev.yml` - Configuração de tokens
3. `application-demo.yml` - Configuração de tokens
4. `application-prod.yml` - Configuração de tokens
5. `GitPullRequestImpactController.java` - Documentação Swagger atualizada

### Preservados (sem alteração):
1. `DummyGitProviderClient.java` - ✅ Mantido 100%
2. `GitProviderClient.java` - ✅ Interface inalterada
3. Todos os serviços existentes - ✅ Backward compatible

---

## ✅ Critérios de Aceite

- [x] Analisar PR real do GitHub
- [x] Analisar MR real do GitLab
- [x] Fallback automático para Dummy quando token ausente
- [x] Impact analysis funcionando normalmente
- [x] ProjectContext GLOBAL e SCOPED preservados
- [x] DummyClient permanece funcional
- [x] Nenhuma regressão nas US anteriores
- [x] Tokens apenas via variáveis de ambiente
- [x] Logs informativos implementados
- [x] Documentação Swagger atualizada

---

## 🎯 Compatibilidade

### US#48 - Cadastro de Projetos
✅ Compatível - Nenhuma alteração

### US#49 - Associação de Regras
✅ Compatível - Nenhuma alteração

### US#50 - Contextualização
✅ Compatível - ProjectContext preservado

### US#51 - Integração Git
✅ **100% Backward Compatible**
- DummyClient continua funcionando
- Interface não alterada
- Fluxo de análise idêntico

---

## 🔄 Próximos Passos (Fora do Escopo)

- [ ] Cache de respostas da API Git
- [ ] Rate limiting
- [ ] Retry automático
- [ ] Suporte a Bitbucket
- [ ] GitHub Enterprise Server
- [ ] GitLab Self-Hosted
- [ ] Métricas de uso
- [ ] Dashboard de integrações

---

## 📞 Suporte

Para dúvidas sobre configuração de tokens:
- GitHub: https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token
- GitLab: https://docs.gitlab.com/ee/user/profile/personal_access_tokens.html

---

**Implementado por:** GitHub Copilot  
**Data:** 2025-12-20  
**Versão:** 1.0.0
