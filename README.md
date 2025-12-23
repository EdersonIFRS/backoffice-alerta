# Backoffice Alerta - API de Análise de Risco

API REST enterprise para análise de risco de Pull Requests com autenticação JWT, persistência JPA e frontend executivo React.

## Stack Tecnológica

### Backend
- Java 21
- Spring Boot 3.2.0
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL / H2
- Flyway Migrations
- Swagger/OpenAPI

### Frontend
- React 18 + TypeScript
- Material UI
- Axios
- React Router
- Recharts
- Vite

## Ambientes Disponíveis

### 🟢 DEV (Desenvolvimento)
```bash
.\start-server.bat
# ou
java -jar target\backoffice-alerta-1.0.0.jar --spring.profiles.active=dev
```
- H2 in-memory: `jdbc:h2:mem:backoffice_alerta_dev`
- H2 Console: http://localhost:8080/h2-console
- Sem seed de dados

### 🎯 DEMO (Demonstração)
```bash
.\start-demo.bat
# ou
java -jar target\backoffice-alerta-1.0.0.jar --spring.profiles.active=demo
```
- H2 in-memory: `jdbc:h2:mem:backoffice_alerta_demo`
- **Seed automático de dados realistas** (US#32)
- Dashboard executivo populado
- Métricas e KRIs funcionais
- Ideal para demonstrações

### 🔴 PROD (Produção)
```bash
java -jar target\backoffice-alerta-1.0.0.jar --spring.profiles.active=prod
```
- PostgreSQL: `jdbc:postgresql://localhost:5432/backoffice_alerta`
- Sem seed de dados (segurança)

## Como Executar

### 1. Backend Spring Boot

```bash
# Compilar
.\mvnw.cmd clean package -DskipTests

# Executar em DEV
.\start-server.bat

# Executar em DEMO (com dados)
.\start-demo.bat
```

### 2. Frontend React

```bash
cd frontend
npm install
npm run dev
```

Frontend disponível em: **http://localhost:3000**

Backend disponível em: **http://localhost:8080**

Swagger UI: **http://localhost:8080/swagger-ui.html**

## Endpoint

### POST /risk/analyze

Analisa o risco de um Pull Request e retorna um score.

**Exemplo de Request:**

```json
{
  "pullRequestId": "PR-12345",
  "files": [
    {
      "fileName": "PaymentService.java",
      "linesChanged": 120,
      "isCritical": true
    },
    {
      "fileName": "UserController.java",
      "linesChanged": 80,
      "isCritical": false
    }
  ],
  "hasTests": false,
  "incidentHistory": 3
}
```

**Exemplo de Response:**

```json
{
  "pullRequestId": "PR-12345",
  "riskScore": 85,
  "riskLevel": "CRÍTICO",
  "explanation": [
    "Arquivo crítico detectado: PaymentService.java (+30 pontos)",
    "Arquivo com mais de 100 linhas alteradas: PaymentService.java (+20 pontos)",
    "Arquivo com 50-100 linhas alteradas: UserController.java (+10 pontos)",
    "Pull Request sem testes (+20 pontos)",
    "Histórico de 3 incidente(s) (+15 pontos)"
  ]
}
```

## Regras de Negócio

- Arquivo crítico: +30 pontos
- Linhas alteradas >100: +20 pontos
- Linhas entre 50 e 100: +10 pontos
- Sem teste: +20 pontos
- Histórico de incidentes: +5 por incidente (máximo 20 pontos)
- Score máximo: 100 pontos

## Níveis de Risco

- **BAIXO**: 0-29 pontos
- **MÉDIO**: 30-59 pontos
- **ALTO**: 60-79 pontos
- **CRÍTICO**: 80-100 pontos
