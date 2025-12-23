# 🚀 Deploy Web - Backoffice Alerta (US#73)

## 📋 Visão Geral

Este guia documenta o deploy do ecossistema completo em ambiente web usando Docker.

**Componentes:**
- ✅ Backend (Spring Boot)
- ✅ Frontend (React + Nginx)
- ✅ PostgreSQL com pgvector
- ✅ Observabilidade básica (logs, health, métricas)

---

## 🔧 Pré-requisitos

### Software Necessário

- **Docker**: versão 20.10+
- **Docker Compose**: versão 2.0+
- **Git**: para clonar o repositório

### Verificar Instalação

```bash
docker --version
docker compose version
```

---

## ⚙️ Configuração Inicial

### 1. Clonar Repositório

```bash
git clone <repository-url>
cd backoffice-alerta
```

### 2. Configurar Variáveis de Ambiente

```bash
# Copiar exemplo
cp .env.example .env

# Editar variáveis obrigatórias
nano .env  # ou vim, code, etc.
```

### 3. Variáveis Obrigatórias

Edite o arquivo `.env` com suas credenciais:

```bash
# PostgreSQL
POSTGRES_DB=backoffice_alerta
POSTGRES_USER=postgres
POSTGRES_PASSWORD=SuaSenhaSegura123!

# JWT (mínimo 256 bits)
JWT_SECRET=sua-chave-jwt-secreta-minimo-256-bits-aqui
JWT_EXPIRATION=86400000

# GitHub Token (obrigatório para US#51/52, US#68, US#72)
GITHUB_TOKEN=ghp_seu_token_pessoal_github

# GitLab Token (opcional)
GITLAB_TOKEN=glpat-seu-token-gitlab

# Embeddings
EMBEDDING_PROVIDER=sentence-transformer
# OPENAI_API_KEY=sk-... (se usar OpenAI)

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost

# Frontend
VITE_API_URL=http://localhost:8080
```

**⚠️ IMPORTANTE:**
- Nunca commite o arquivo `.env`
- Use senhas fortes em produção
- Tokens GitHub/GitLab são **obrigatórios** para funcionalidades de análise de repositórios

---

## 🚀 Como Subir o Ambiente

### Opção 1: Build e Start (Primeira vez)

```bash
# Build das imagens
docker compose build

# Subir todos os serviços
docker compose up -d

# Ver logs
docker compose logs -f
```

### Opção 2: Rebuild e Restart

```bash
# Rebuild forçado
docker compose build --no-cache

# Restart
docker compose up -d --force-recreate
```

### Opção 3: Apenas Start (imagens já criadas)

```bash
docker compose up -d
```

---

## 🌐 URLs de Acesso

Após subir o ambiente:

| Serviço | URL | Descrição |
|---------|-----|-----------|
| **Frontend** | http://localhost:3000 | Interface web React |
| **Backend API** | http://localhost:8080 | API REST |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | Documentação interativa |
| **Health Check** | http://localhost:8080/actuator/health | Status do backend |
| **Metrics** | http://localhost:8080/actuator/metrics | Métricas Spring Boot |
| **PostgreSQL** | localhost:5432 | Banco de dados |

**Credenciais padrão:**
- **Admin**: `admin` / `admin123`
- **Risk Manager**: `riskmanager` / `risk123`
- **Engineer**: `engineer` / `eng123`

---

## 📊 Verificar Status

### Health Checks

```bash
# Backend
curl http://localhost:8080/actuator/health

# Frontend
curl http://localhost:3000/health

# PostgreSQL (dentro do container)
docker compose exec postgres pg_isready -U postgres
```

### Logs

```bash
# Todos os serviços
docker compose logs -f

# Apenas backend
docker compose logs -f backend

# Apenas frontend
docker compose logs -f frontend

# Apenas PostgreSQL
docker compose logs -f postgres
```

### Status dos Containers

```bash
docker compose ps
```

**Esperado:**
```
NAME                   STATUS          PORTS
backoffice-backend     Up (healthy)    0.0.0.0:8080->8080/tcp
backoffice-frontend    Up (healthy)    0.0.0.0:3000->80/tcp
backoffice-postgres    Up (healthy)    0.0.0.0:5432->5432/tcp
```

---

## 🧪 Testar Funcionalidades

### 1. Testar Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### 2. Testar Onboarding (US#72)

```bash
# Obter token
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.token')

# Criar projeto
PROJECT_ID=$(curl -s -X POST http://localhost:8080/api/projects \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Project",
    "description": "Projeto de teste",
    "owner": "Admin",
    "repositoryUrl": "https://github.com/seu-repo/projeto",
    "active": true
  }' | jq -r '.id')

# Iniciar onboarding
curl -X POST http://localhost:8080/risk/projects/onboarding/start \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"projectId\": \"$PROJECT_ID\",
    \"provider\": \"GITHUB\",
    \"repositoryUrl\": \"https://github.com/seu-repo/projeto\",
    \"branch\": \"main\"
  }"
```

### 3. Acessar Frontend

Abra http://localhost:3000 no navegador e faça login.

---

## 🛑 Parar Ambiente

```bash
# Parar containers (mantém volumes)
docker compose down

# Parar e remover volumes (⚠️ apaga dados)
docker compose down -v

# Parar e remover tudo
docker compose down -v --rmi all
```

---

## 🔧 Troubleshooting

### ❌ Backend não sobe

**Sintoma:** Container backend para imediatamente

**Solução:**
```bash
# Ver logs detalhados
docker compose logs backend

# Verificar conectividade com PostgreSQL
docker compose exec backend ping postgres

# Verificar variáveis de ambiente
docker compose exec backend env | grep SPRING
```

**Causas comuns:**
- PostgreSQL não está pronto (aguarde health check)
- Credenciais incorretas no `.env`
- Porta 8080 já em uso

### ❌ Frontend não carrega

**Sintoma:** ERR_CONNECTION_REFUSED ou página em branco

**Solução:**
```bash
# Verificar se Nginx está rodando
docker compose exec frontend nginx -t

# Verificar logs
docker compose logs frontend

# Testar health check
curl http://localhost:3000/health
```

**Causas comuns:**
- Backend não está pronto
- CORS mal configurado
- Build do React falhou

### ❌ PostgreSQL não persiste dados

**Sintoma:** Dados somem após restart

**Solução:**
```bash
# Verificar volumes
docker volume ls | grep postgres

# Inspecionar volume
docker volume inspect backoffice-alerta_postgres_data

# Verificar montagem
docker compose exec postgres df -h /var/lib/postgresql/data
```

### ❌ Erro "GitHub Token invalid"

**Sintoma:** Onboarding falha na etapa Git

**Solução:**
1. Verificar token no `.env`
2. Validar permissões do token (repo:read)
3. Testar token manualmente:
```bash
curl -H "Authorization: token $GITHUB_TOKEN" \
  https://api.github.com/user
```

### ❌ Embeddings não funcionam

**Sintoma:** RAG status = LIMITED no onboarding

**Solução:**
```bash
# Verificar provider
docker compose exec backend env | grep EMBEDDING_PROVIDER

# Logs de embedding
docker compose logs backend | grep "US#65\|embedding"
```

**Causas comuns:**
- Sentence Transformer não instalado
- OpenAI API key inválida
- Modelo não baixado

### ❌ Porta já em uso

**Sintoma:** Error: port is already allocated

**Solução:**
```bash
# Verificar porta 8080
lsof -i :8080  # Linux/Mac
netstat -ano | findstr :8080  # Windows

# Matar processo
kill -9 <PID>  # Linux/Mac
taskkill /PID <PID> /F  # Windows

# Ou alterar porta no docker-compose.yml
```

---

## 📦 Persistência de Dados

### Volumes Criados

```bash
docker volume ls
```

**Volumes:**
- `backoffice-alerta_postgres_data` - Dados PostgreSQL + Embeddings (US#66)

### Backup Manual

```bash
# Backup PostgreSQL
docker compose exec postgres pg_dump -U postgres backoffice_alerta > backup.sql

# Restore
docker compose exec -T postgres psql -U postgres backoffice_alerta < backup.sql
```

### Limpar Volumes (⚠️ Apaga dados)

```bash
docker compose down -v
docker volume prune
```

---

## 🔐 Segurança

### Checklist Produção

- [ ] Alterar senhas padrão
- [ ] JWT secret com 256+ bits
- [ ] PostgreSQL password forte
- [ ] Tokens Git com permissões mínimas
- [ ] CORS configurado corretamente
- [ ] HTTPS habilitado (adicionar reverse proxy)
- [ ] Firewall configurado
- [ ] Backup automático configurado
- [ ] Logs sendo coletados
- [ ] Metrics sendo monitorados

### HTTPS (Produção)

Para ambiente de produção, adicione um reverse proxy (Nginx/Traefik):

```yaml
# Exemplo com Traefik (adicionar ao docker-compose.yml)
traefik:
  image: traefik:v2.10
  command:
    - "--providers.docker=true"
    - "--entrypoints.web.address=:80"
    - "--entrypoints.websecure.address=:443"
  ports:
    - "80:80"
    - "443:443"
  volumes:
    - "/var/run/docker.sock:/var/run/docker.sock:ro"
```

---

## 📊 Observabilidade

### Métricas Disponíveis

```bash
# Listar métricas
curl http://localhost:8080/actuator/metrics

# Métrica específica
curl http://localhost:8080/actuator/metrics/jvm.memory.used
curl http://localhost:8080/actuator/metrics/http.server.requests
```

### Logs Estruturados (JSON)

```bash
# Logs em formato JSON
docker compose logs backend | jq .

# Filtrar por correlation ID
docker compose logs backend | jq 'select(.correlation_id == "abc-123")'

# Filtrar por US
docker compose logs backend | grep "US#72"
```

### Health Checks Customizados

Todos os health checks existentes continuam funcionando:
- `/actuator/health`
- `/risk/dashboard/executive/health` (US#60)
- `/risk/projects/onboarding/health` (US#72)

---

## 🚀 Comandos Principais

```bash
# Subir ambiente
docker compose up -d

# Ver logs
docker compose logs -f

# Status
docker compose ps

# Restart serviço específico
docker compose restart backend

# Rebuild
docker compose build --no-cache

# Parar
docker compose down

# Parar e limpar volumes
docker compose down -v

# Entrar no container
docker compose exec backend sh
docker compose exec postgres psql -U postgres backoffice_alerta

# Ver recursos
docker stats
```

---

## 🎯 Próximos Passos

Após ambiente funcional:

1. ✅ Testar US#72 (Onboarding) com projeto real
2. ✅ Validar US#71 (Comparação PRE vs POST)
3. ✅ Testar US#70 (LLM Detection)
4. ✅ Verificar persistência após restart
5. ✅ Configurar backup automático
6. ✅ Adicionar monitoring (Prometheus/Grafana - fora do escopo US#73)

---

## 📚 Referências

- **Docker Compose**: https://docs.docker.com/compose/
- **Spring Boot Actuator**: https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html
- **pgvector**: https://github.com/pgvector/pgvector
- **Nginx SPA**: https://angular.io/guide/deployment#server-configuration

---

## ⚠️ Notas Importantes

1. **Ambiente de Desenvolvimento**: Esta configuração é adequada para desenvolvimento e testes. Para produção, adicione:
   - HTTPS/TLS
   - Reverse proxy (Nginx/Traefik)
   - Secrets management (Vault, AWS Secrets)
   - Monitoring (Prometheus/Grafana)
   - Logging agregado (ELK, Loki)
   - Backup automático

2. **Persistência**: Dados PostgreSQL persistem em volumes Docker. Em produção, considere:
   - Backup para S3/Azure Blob
   - Replicação PostgreSQL
   - Snapshots regulares

3. **Escalabilidade**: Para escalar horizontalmente:
   - Adicionar load balancer
   - Múltiplas réplicas do backend
   - Redis para sessões compartilhadas
   - PostgreSQL read replicas

4. **Backward Compatibility**: Todas as USs anteriores (US#48-72) continuam funcionando normalmente.

---

**US#73 - Deploy Web Completo ✅**

Ambiente pronto para testes com projetos reais!
