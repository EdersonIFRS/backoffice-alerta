# Business Rule: RULE_AUTH_TOKEN_ROTATION

## Metadata

| Field | Value |
|-------|-------|
| **Rule ID** | RULE_AUTH_TOKEN_ROTATION |
| **Name** | JWT and Refresh Token Rotation Policy |
| **Domain** | SECURITY |
| **Criticality** | HIGH |
| **Owner** | auth-team |

## Description

Esta regra define a política de rotação de tokens JWT (access tokens) e refresh tokens no sistema soft-pilot-auth. A rotação regular de tokens é essencial para limitar a janela de exposição em caso de comprometimento de credenciais e para manter a integridade das sessões de usuário.

O sistema utiliza uma arquitetura de tokens de curta duração (access tokens) combinados com tokens de renovação (refresh tokens). Access tokens expiram rapidamente para minimizar riscos de uso indevido, enquanto refresh tokens permitem obter novos access tokens sem re-autenticação completa.

A rotação de refresh tokens segue o padrão de "refresh token rotation" onde cada uso de refresh token gera um novo par de tokens, invalidando o anterior. Isso permite detecção de roubo de tokens: se um token roubado for usado após o legítimo, ambos são invalidados forçando re-autenticação.

## Validation Criteria

- [ ] **Access Token (JWT):**
  - Tempo de vida: 15 minutos
  - Algoritmo: RS256 (RSA + SHA-256)
  - Claims obrigatórios: sub, email, role, company_id, exp, iat, iss
- [ ] **Refresh Token:**
  - Tempo de vida: 7 dias
  - Rotação obrigatória a cada uso
  - Token anterior invalidado após rotação bem-sucedida
- [ ] Detecção de reuso de refresh token invalida toda a família de tokens
- [ ] Tokens são armazenados de forma segura (httpOnly cookies ou secure storage)
- [ ] Refresh tokens são vinculados ao dispositivo/fingerprint quando possível
- [ ] Logout invalida todos os tokens ativos da sessão

## Examples

### ✅ Valid Scenario

```
Usuário: dev@empresa.com
Login: 09:00

Timeline de Tokens:
09:00 - Login bem-sucedido
        Access Token #1 gerado (exp: 09:15)
        Refresh Token #1 gerado (exp: 7 dias)

09:14 - Access token próximo de expirar
        Cliente solicita renovação com Refresh Token #1
        
09:14 - Servidor valida Refresh Token #1 ✓
        Access Token #2 gerado (exp: 09:29)
        Refresh Token #2 gerado (novo)
        Refresh Token #1 invalidado

Resultado: ROTAÇÃO BEM-SUCEDIDA
```

### ❌ Invalid Scenario (Token Reuse Attack)

```
Cenário: Atacante roubou Refresh Token #1

Timeline:
09:00 - Usuário legítimo faz login
        Refresh Token #1 emitido

09:14 - Usuário legítimo renova tokens
        Refresh Token #2 emitido
        Refresh Token #1 invalidado

09:30 - Atacante tenta usar Refresh Token #1 (roubado)
        Sistema detecta reuso de token inválido ✗
        TODA família de tokens invalidada
        Usuário legítimo forçado a re-autenticar

Resultado: TOKENS INVALIDADOS - POSSÍVEL ATAQUE DETECTADO
Log: "Refresh token reuse detected for user dev@empresa.com. All tokens revoked."
Alerta: Enviado para equipe de segurança
```

### ❌ Invalid Scenario (Access Token Expirado)

```
Usuário: analista@empresa.com
Access Token gerado: 09:00 (exp: 09:15)
Tentativa de uso: 09:20

Request:
GET /api/data
Authorization: Bearer eyJhbG...

Response:
{
  "error": {
    "code": "TOKEN_EXPIRED",
    "message": "Access token expired"
  }
}

Resultado: REQUISIÇÃO REJEITADA
Ação: Cliente deve usar refresh token para obter novo access token
```

## Related Rules

- [RULE_AUTH_SESSION_TIMEOUT](./RULE_AUTH_SESSION_TIMEOUT.md) - Timeout de sessão
- [RULE_AUTH_MFA_ENFORCEMENT](./RULE_AUTH_MFA_ENFORCEMENT.md) - MFA na re-autenticação

## References

- [RFC 7519 - JSON Web Token (JWT)](https://datatracker.ietf.org/doc/html/rfc7519)
- [RFC 6749 - OAuth 2.0 Authorization Framework](https://datatracker.ietf.org/doc/html/rfc6749)
- [OWASP JWT Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html)
- [Auth0 - Refresh Token Rotation](https://auth0.com/docs/secure/tokens/refresh-tokens/refresh-token-rotation)
- CWE-384: Session Fixation
