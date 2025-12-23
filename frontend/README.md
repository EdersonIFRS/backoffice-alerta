# Frontend Executivo de Risco - US#31

Frontend Web executivo **read-only** para visualização de riscos, decisões, SLAs, auditorias, notificações e métricas.

## 🚀 Stack Tecnológica

- **React 18** com TypeScript
- **Vite** - Build tool
- **Material UI (MUI)** - Componentes UI
- **Axios** - Cliente HTTP
- **React Router** - Roteamento
- **Recharts** - Gráficos

## 🔐 Segurança

- JWT armazenado **APENAS em memória** (não usa localStorage)
- Interceptors Axios para adicionar token automaticamente
- Tratamento de 401 (redirect para login) e 403 (acesso negado)
- RBAC respeitado em todas as rotas

## 📋 Rotas do Frontend

| Rota | Descrição | Roles Permitidos |
|------|-----------|------------------|
| `/login` | Login | Público |
| `/` | Dashboard Executivo | Todos autenticados |
| `/audits` | Auditorias & Decisões | ADMIN, RISK_MANAGER, VIEWER |
| `/slas` | SLAs Ativos | ADMIN, RISK_MANAGER |
| `/notifications` | Notificações | Todos autenticados |
| `/metrics` | Métricas de Confiabilidade | ADMIN, RISK_MANAGER, VIEWER |
| `/simulation` | Simulação de Riscos | ADMIN, RISK_MANAGER |
| `/impact-graph` | Impacto Sistêmico | ADMIN, RISK_MANAGER, ENGINEER |
| `/executive-impact` | Impacto Executivo | ADMIN, RISK_MANAGER |
| `/timeline` | Timeline de Decisão | ADMIN, RISK_MANAGER |
| `/historical-comparison` | Comparação Histórica | ADMIN, RISK_MANAGER |
| `/risk-chat` | **Chat de Risco (US#47)** | **ADMIN, RISK_MANAGER, ENGINEER** |

## 🎯 Funcionalidades

### 1. Login
- Autenticação via `POST /auth/login`
- Feedback visual de erros
- Credenciais de teste disponíveis na tela

### 2. Dashboard Executivo
- Status de confiança do sistema
- KRIs: accuracyRate, falsePositiveRate, falseNegativeRate, incidentAfterApprovalRate
- Alertas ativos com severidade
- Top 5 regras problemáticas
- Filtro de ambiente (GLOBAL / PRODUCTION_ONLY)

### 3. Auditorias
- Tabela com todas as auditorias
- Badges coloridos para decisões e níveis de risco
- Detalhes ao clicar: regras impactadas, incidentes, restrições

### 4. SLAs
- Tabela de SLAs ativos
- Highlight vermelho para SLAs vencidos
- Status, deadline, nível de escalonamento

### 5. Notificações
- Lista de notificações organizacionais
- Severidade, canal, time, mensagem
- Link para auditoria relacionada

### 6. Métricas
- Score de confiança geral
- Gráfico de barras com KPIs
- Tendências detectadas com ícones visuais

### 7. Chat de Risco (US#47) ⭐ **NOVO**
- Interface conversacional para análise de impacto
- Perguntas sobre regras de negócio, código e responsáveis
- Mensagens estruturadas por tipo:
  - **INFO** (azul) - Informações contextuais
  - **WARNING** (laranja) - Alertas e riscos
  - **ACTION** (verde) - Próximos passos recomendados
- Indicador de confiança (HIGH/MEDIUM/LOW)
- Foco opcional: BUSINESS, TECHNICAL, EXECUTIVE
- **Exemplos de perguntas:**
  - "Onde alterar o cálculo de horas para Pessoa Jurídica?"
  - "Quais riscos existem ao mudar regras de pagamento?"
  - "Quem preciso avisar antes de alterar validação de CPF?"
  - "Alterar validação de CPF já causou incidente em produção?"
- Fallback amigável em caso de erro
- Read-only: não persiste histórico de conversa

## 📦 Como Rodar

### Pré-requisitos
- Node.js 18+
- Backend Spring Boot rodando em `http://localhost:8080`

### Instalação

```bash
cd frontend
npm install
```

### Desenvolvimento

```bash
npm run dev
```

O frontend estará disponível em: **http://localhost:3000**

### Build de Produção

```bash
npm run build
npm run preview
```

## 🔧 Configuração

### Proxy para Backend

O Vite está configurado para fazer proxy das requisições `/auth` e `/risk` para `http://localhost:8080`.

Se o backend estiver em outra URL, edite `vite.config.ts`:

```typescript
server: {
  proxy: {
    '/auth': {
      target: 'http://SEU_BACKEND:PORTA',
      changeOrigin: true
    },
    '/risk': {
      target: 'http://SEU_BACKEND:PORTA',
      changeOrigin: true
    }
  }
}
```

## 👤 Usuários de Teste

| Usuário | Senha | Role |
|---------|-------|------|
| admin | admin123 | ADMIN |
| risk | risk123 | RISK_MANAGER |
| viewer | view123 | VIEWER |
| engineer | eng123 | ENGINEER |

## 🏗️ Arquitetura

```
frontend/
├── src/
│   ├── components/        # Componentes reutilizáveis
│   │   ├── Layout.tsx
│   │   ├── ProtectedRoute.tsx
│   │   ├── StatusBadge.tsx
│   │   ├── RiskChatMessage.tsx    # US#47
│   │   ├── RiskChatInput.tsx      # US#47
│   │   └── RiskChatConversation.tsx # US#47
│   ├── context/           # Context API
│   │   └── AuthContext.tsx
│   ├── pages/             # Páginas principais
│   │   ├── Login.tsx
│   │   ├── Dashboard.tsx
│   │   ├── Audits.tsx
│   │   ├── Slas.tsx
│   │   ├── Notifications.tsx
│   │   ├── Metrics.tsx
│   │   ├── Simulation.tsx
│   │   ├── ImpactGraph.tsx
│   │   ├── ExecutiveImpact.tsx
│   │   ├── Timeline.tsx
│   │   ├── HistoricalComparison.tsx
│   │   └── RiskChat.tsx          # US#47 - Chat de Risco
│   ├── routes/            # Configuração de rotas
│   │   └── index.tsx
│   ├── services/          # API clients
│   │   ├── api.ts
│   │   ├── executiveImpact.ts
│   │   ├── historicalComparison.ts
│   │   ├── impactGraph.ts
│   │   ├── timeline.ts
│   │   └── riskChat.ts            # US#47
│   ├── types/             # TypeScript types
│   │   ├── index.ts
│   │   ├── executiveImpact.ts
│   │   ├── historicalComparison.ts
│   │   ├── impactGraph.ts
│   │   ├── simulation.ts
│   │   ├── timeline.ts
│   │   └── riskChat.ts            # US#47
│   ├── App.tsx
│   └── main.tsx
├── package.json
├── tsconfig.json
└── vite.config.ts
```

## ⚠️ Importante

### O que este frontend NÃO faz:
- ❌ Criar dados
- ❌ Alterar dados
- ❌ Recalcular risco
- ❌ Replicar lógica de negócio
- ❌ Armazenar JWT em localStorage

### O que este frontend FAZ:
- ✅ Consome APIs existentes
- ✅ Visualiza dados de forma executiva
- ✅ Respeita RBAC
- ✅ Trata 401/403 corretamente
- ✅ JWT apenas em memória

## 📝 Endpoints Consumidos

- `POST /auth/login` - Autenticação
- `GET /risk/dashboard/executive` - Dashboard
- `GET /risk/audit` - Auditorias
- `GET /risk/sla` - SLAs
- `GET /risk/notifications` - Notificações
- `GET /risk/metrics` - Métricas
- `POST /risk/chat/query` - **Chat de Risco (US#47)**

## 🎨 UI/UX

- Design responsivo (desktop-first)
- Material Design com MUI
- Feedback visual para ações
- Loading states
- Tratamento de erros amigável

## 🔒 CORS

Se encontrar problemas de CORS, certifique-se de que o backend Spring Boot permite requisições de `http://localhost:3000`.

Adicione no `SecurityConfig.java`:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.addAllowedOrigin("http://localhost:3000");
    configuration.addAllowedMethod("*");
    configuration.addAllowedHeader("*");
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

## 📄 Licença

Este é um projeto interno da organização.
