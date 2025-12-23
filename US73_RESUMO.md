# ✅ US#73 - Deploy do Ecossistema (Ambiente Web) + Observabilidade Básica

## 📦 Resumo da Implementação

**Status:** ✅ COMPLETO - Pronto para commit e testes

**Objetivo:** Deploy completo do ecossistema backoffice-alerta em ambiente web usando Docker com observabilidade básica.

---

## 🎯 Escopo Implementado

### ✅ O que FOI implementado (conforme especificação):

1. **Containerização Docker**
   - Multi-stage builds (otimização de imagem)
   - Backend: Maven build + JRE Alpine runtime
   - Frontend: npm build + Nginx runtime
   - Non-root users (segurança)
   - Health checks integrados

2. **Docker Compose**
   - 3 serviços: postgres (pgvector), backend, frontend
   - Volumes persistentes para PostgreSQL
   - Network interna (backoffice-network)
   - Health checks com depends_on conditions
   - Restart policy: unless-stopped

3. **Persistência Real**
   - PostgreSQL 16 + pgvector extension
   - Substituiu H2 (desenvolvimento)
   - Suporte a embeddings (US#66 Vector DB)
   - Flyway migrations ativado

4. **Configuração de Ambiente**
   - 100% environment variables
   - Arquivo .env.example documentado
   - Nenhuma credencial hardcoded
   - Tokens Git via env vars (US#51/52, US#68, US#72)
   - Embedding provider configurável (sentence-transformer | openai)

5. **Observabilidade Básica**
   - Logs em formato JSON estruturado
   - Correlation ID (X-Correlation-ID header)
   - Spring Boot Actuator (/actuator/health, /info, /metrics, /prometheus)
   - Health checks em todos os serviços
   - Métricas Prometheus-compatible

6. **Segurança**
   - JWT obrigatório (mantido)
   - RBAC intacto (mantido)
   - CORS configurável via env
   - Non-root users nos containers
   - Secrets via environment variables

7. **Documentação**
   - DEPLOY_WEB.md completo (quick start, troubleshooting, comandos)
   - Instruções de setup, uso, observabilidade
   - Troubleshooting para problemas comuns

### ❌ O que NÃO foi implementado (fora do escopo):

- Multi-tenant (US futura)
- Kubernetes / Helm (deployment avançado)
- Autoscaling horizontal
- Billing / metering
- SSO enterprise (Okta/Azure AD)
- Infrastructure monitoring (Prometheus/Grafana setup)
- Application Performance Monitoring (APM)
- ELK/Loki log aggregation
- Service mesh (Istio/Linkerd)
- CI/CD pipelines

---

## 📂 Arquivos Criados/Modificados

### Novos Arquivos (8):

1. **Dockerfile** (root)
   - Multi-stage: Maven build + JRE runtime
   - Profile: web
   - Health check: /actuator/health
   - Port: 8080

2. **frontend/Dockerfile**
   - Multi-stage: npm build + Nginx
   - Health check: /health
   - Port: 80 (mapped to 3000)

3. **frontend/nginx.conf**
   - SPA routing (React Router)
   - Gzip compression
   - Static cache (1 year)
   - Health endpoint

4. **docker-compose.yml**
   - 3 services orchestration
   - Volumes: postgres_data
   - Network: backoffice-network
   - Health checks + depends_on

5. **.env.example**
   - Template com todas as variáveis
   - PostgreSQL, JWT, Git tokens, Embeddings, CORS
   - Documentado com explicações

6. **src/main/resources/application-web.yml**
   - Profile Spring para web
   - PostgreSQL config (não H2)
   - Actuator endpoints
   - JSON logging
   - Git providers config
   - Embeddings provider config
   - Vector DB config

7. **src/main/java/com/backoffice/alerta/config/CorrelationIdFilter.java**
   - Servlet filter para correlation ID
   - Extrai/gera X-Correlation-ID
   - Adiciona ao MDC para logging
   - Retorna no response header

8. **DEPLOY_WEB.md**
   - Documentação completa de deployment
   - Quick start, troubleshooting, comandos
   - Checklist de segurança para produção

### Arquivos Modificados (2):

1. **.dockerignore**
   - Exclui target/, .git/, node_modules/, .env
   - Otimiza build context

2. **pom.xml**
   - Adicionado: spring-boot-starter-actuator
   - Adicionado: micrometer-registry-prometheus
   - Para observabilidade (métricas)

---

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────────────────┐
│                   Docker Compose                     │
├─────────────────────────────────────────────────────┤
│                                                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │
│  │  Frontend   │  │   Backend   │  │  PostgreSQL │ │
│  │  (Nginx)    │──┤ (Spring)    │──┤  + pgvector │ │
│  │  Port 3000  │  │  Port 8080  │  │  Port 5432  │ │
│  └─────────────┘  └─────────────┘  └─────────────┘ │
│         │                │                 │         │
│         └────────────────┴─────────────────┘         │
│              backoffice-network (bridge)             │
│                                                       │
│  Volumes:                                            │
│  - postgres_data (persistente)                       │
└─────────────────────────────────────────────────────┘
```

**Fluxo de Startup:**
1. PostgreSQL inicia primeiro, health check valida (pg_isready)
2. Backend aguarda postgres healthy, inicia, health check valida (/actuator/health)
3. Frontend aguarda backend, inicia, health check valida (/health)

---

## 🚀 Como Usar

### Quick Start

```bash
# 1. Copiar env
cp .env.example .env

# 2. Editar variáveis (obrigatórias)
# - POSTGRES_PASSWORD
# - JWT_SECRET (256+ bits)
# - GITHUB_TOKEN (ghp_...)

# 3. Subir ambiente
docker compose up -d

# 4. Ver logs
docker compose logs -f

# 5. Acessar
# Frontend: http://localhost:3000
# Backend:  http://localhost:8080
# Swagger:  http://localhost:8080/swagger-ui.html
# Health:   http://localhost:8080/actuator/health
```

### Testar Onboarding (US#72)

```bash
# Login
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

---

## 📊 Observabilidade

### Logs JSON

```bash
# Ver logs estruturados
docker compose logs backend | jq .

# Filtrar por correlation ID
docker compose logs backend | jq 'select(.correlation_id == "abc-123")'

# Filtrar por level
docker compose logs backend | jq 'select(.level == "ERROR")'
```

### Health Checks

```bash
# Backend
curl http://localhost:8080/actuator/health

# Frontend
curl http://localhost:3000/health

# PostgreSQL
docker compose exec postgres pg_isready -U postgres
```

### Métricas

```bash
# Listar métricas
curl http://localhost:8080/actuator/metrics

# JVM memory
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# HTTP requests
curl http://localhost:8080/actuator/metrics/http.server.requests

# Prometheus format
curl http://localhost:8080/actuator/prometheus
```

### Correlation ID

```bash
# Request com correlation ID
curl -H "X-Correlation-ID: test-123" \
  http://localhost:8080/actuator/health

# Logs terão "correlation_id": "test-123"
docker compose logs backend | grep test-123
```

---

## 🧪 Validação

### Checklist de Testes

- [ ] `docker compose up -d` funciona sem erros
- [ ] Todos os containers ficam (healthy)
- [ ] Frontend acessível em http://localhost:3000
- [ ] Backend acessível em http://localhost:8080
- [ ] Swagger acessível em http://localhost:8080/swagger-ui.html
- [ ] Login funciona (admin/admin123)
- [ ] Onboarding (US#72) funciona via Swagger
- [ ] Comparação LLM (US#71) funciona via Swagger
- [ ] Logs aparecem em formato JSON
- [ ] Correlation ID presente nos logs
- [ ] Health checks respondem 200
- [ ] Métricas acessíveis em /actuator/metrics
- [ ] Dados persistem após `docker compose down && docker compose up`
- [ ] Embeddings persistem (US#66 Vector DB)

### Validar Persistência

```bash
# 1. Criar dados via API
curl -X POST http://localhost:8080/api/projects \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","description":"Persistência","owner":"Admin","repositoryUrl":"https://github.com/test/test","active":true}'

# 2. Parar
docker compose down

# 3. Subir
docker compose up -d

# 4. Verificar projeto existe
curl http://localhost:8080/api/projects \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🔐 Segurança (Produção)

### Checklist

- [ ] Alterar todas as senhas padrão
- [ ] JWT_SECRET com 256+ bits aleatórios
- [ ] POSTGRES_PASSWORD forte
- [ ] GitHub/GitLab tokens com permissões mínimas (repo:read)
- [ ] CORS_ALLOWED_ORIGINS apenas domínios confiáveis
- [ ] HTTPS habilitado (adicionar reverse proxy)
- [ ] Firewall configurado (apenas portas necessárias)
- [ ] Volumes com backup automático
- [ ] Logs sendo coletados centralmente
- [ ] Métricas sendo monitoradas (alertas)

### HTTPS (Produção)

Adicionar reverse proxy (Nginx/Traefik/Caddy):

```yaml
# Exemplo: docker-compose.prod.yml
traefik:
  image: traefik:v2.10
  command:
    - "--providers.docker=true"
    - "--entrypoints.websecure.address=:443"
    - "--certificatesresolvers.letsencrypt.acme.email=seu@email.com"
  ports:
    - "443:443"
  volumes:
    - /var/run/docker.sock:/var/run/docker.sock:ro
```

---

## 🐛 Troubleshooting Comum

### Backend não sobe

```bash
# Ver logs detalhados
docker compose logs backend

# Verificar conectividade
docker compose exec backend ping postgres

# Verificar variáveis
docker compose exec backend env | grep SPRING
```

**Causas:**
- PostgreSQL não pronto (aguarde health check)
- Credenciais erradas no .env
- Porta 8080 já em uso

### Frontend CORS error

```bash
# Verificar CORS no backend
docker compose exec backend env | grep CORS

# Editar .env
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://seu-dominio.com

# Restart
docker compose restart backend
```

### Onboarding falha (Git connectivity)

```bash
# Verificar token
docker compose exec backend env | grep GITHUB_TOKEN

# Testar token manualmente
curl -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/user
```

**Causas:**
- Token inválido
- Token sem permissão repo:read
- URL do repositório incorreta

---

## 📈 Próximos Passos (Pós US#73)

1. **Testes Automatizados em Docker**
   - Adicionar profile test ao docker-compose
   - Testes E2E com containers

2. **CI/CD Pipeline**
   - GitHub Actions / GitLab CI
   - Build automático de imagens
   - Push para registry (Docker Hub / AWS ECR)

3. **Monitoring Avançado** (US futura)
   - Prometheus + Grafana
   - Dashboards customizados
   - Alertas baseados em métricas

4. **Logging Agregado** (US futura)
   - ELK Stack / Loki + Grafana
   - Pesquisa centralizada de logs
   - Retenção de logs

5. **Kubernetes** (US futura)
   - Helm charts
   - Autoscaling horizontal
   - Rolling updates
   - Secrets management (Vault)

---

## 🎉 Conclusão

**US#73 implementada com sucesso!**

✅ Deploy completo em Docker
✅ PostgreSQL + pgvector persistente
✅ Observabilidade básica (logs, health, métricas)
✅ 100% configurável via environment
✅ Documentação completa
✅ Backward compatible com US#48-72
✅ Pronto para testes com projetos reais

**Comando de teste:**
```bash
cp .env.example .env
# Edite GITHUB_TOKEN, POSTGRES_PASSWORD, JWT_SECRET
docker compose up -d
# Acesse http://localhost:3000
```

---

**Documentação Completa:** [DEPLOY_WEB.md](./DEPLOY_WEB.md)

**Autor:** GitHub Copilot
**Data:** 2024
**Referência:** US#73 - Deploy do Ecossistema (Ambiente Web) + Observabilidade Básica
