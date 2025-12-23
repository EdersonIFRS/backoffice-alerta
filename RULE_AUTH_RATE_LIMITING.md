# Business Rule: RULE_AUTH_RATE_LIMITING

## Metadata

| Field | Value |
|-------|-------|
| **Rule ID** | RULE_AUTH_RATE_LIMITING |
| **Name** | Authentication Rate Limiting Policy |
| **Domain** | SECURITY |
| **Criticality** | HIGH |
| **Owner** | auth-team |

## Description

Esta regra define os limites de taxa (rate limiting) para tentativas de autenticação no sistema soft-pilot-auth. O rate limiting é uma defesa crítica contra ataques de força bruta, credential stuffing e denial of service direcionados ao sistema de autenticação.

A política implementa múltiplas camadas de limitação: por IP, por conta de usuário e global. Cada camada tem seus próprios limites e janelas de tempo, permitindo flexibilidade para usuários legítimos enquanto bloqueia padrões de ataque. Limites são aplicados de forma progressiva, com penalidades crescentes para violações repetidas.

O sistema utiliza algoritmos de sliding window para contagem precisa de tentativas e implementa bypass para IPs confiáveis (como redes corporativas configuradas). Todas as violações de rate limit são logadas para análise de segurança e podem disparar alertas automáticos.

## Validation Criteria

- [ ] **Limite por IP (endpoints de login):**
  - Máximo: 10 tentativas por minuto
  - Máximo: 50 tentativas por hora
  - Penalidade: bloqueio progressivo (1min, 5min, 15min, 1h)
- [ ] **Limite por conta de usuário:**
  - Máximo: 5 tentativas falhas por minuto
  - Máximo: 20 tentativas falhas por hora
  - Ver também: RULE_AUTH_ACCOUNT_LOCKOUT
- [ ] **Limite global (proteção DDoS):**
  - Máximo: 1000 requests/minuto no endpoint /auth/login
  - Ação: degradação graceful com filas
- [ ] Headers de rate limit incluídos nas respostas (X-RateLimit-*)
- [ ] Tentativas de bypass (header spoofing) são detectadas e logadas
- [ ] IPs em allowlist corporativa têm limites relaxados (2x)

## Examples

### ✅ Valid Scenario

```
IP: 200.158.xxx.xxx
Conta: usuario@empresa.com
Janela: último minuto

Tentativas no minuto:
1. 10:00:00 - Falha (senha incorreta)
2. 10:00:15 - Falha (senha incorreta)
3. 10:00:30 - Sucesso ✓

Response Headers:
X-RateLimit-Limit: 10
X-RateLimit-Remaining: 7
X-RateLimit-Reset: 1703160060

Resultado: ACESSO PERMITIDO
```

### ❌ Invalid Scenario (Rate Limit por IP)

```
IP: 45.33.xxx.xxx (suspeito)
Janela: último minuto

Tentativas:
1-10. 10:00:00 a 10:00:30 - Diferentes emails, todas falhas
11.   10:00:35 - Tentativa adicional

Response (HTTP 429):
{
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "Too many requests. Please try again in 60 seconds.",
    "retry_after": 60
  }
}

Headers:
X-RateLimit-Limit: 10
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1703160120
Retry-After: 60

Resultado: BLOQUEADO POR 1 MINUTO
Log: "Rate limit exceeded for IP 45.33.xxx.xxx - possible brute force attack"
```

### ❌ Invalid Scenario (Penalidade Progressiva)

```
IP: 185.220.xxx.xxx (TOR exit node)

Histórico de violações (última hora):
1ª violação: 09:00 - Bloqueio 1 minuto
2ª violação: 09:05 - Bloqueio 5 minutos
3ª violação: 09:15 - Bloqueio 15 minutos
4ª violação: 09:35 - Bloqueio 1 hora

Response (HTTP 429):
{
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "Your IP has been temporarily blocked due to excessive requests.",
    "retry_after": 3600,
    "escalation_level": 4
  }
}

Resultado: BLOQUEADO POR 1 HORA
Alerta: Enviado para SOC (Security Operations Center)
```

## Related Rules

- [RULE_AUTH_ACCOUNT_LOCKOUT](./RULE_AUTH_ACCOUNT_LOCKOUT.md) - Bloqueio de conta
- [RULE_AUTH_PASSWORD_STRENGTH](./RULE_AUTH_PASSWORD_STRENGTH.md) - Complexidade de senha

## References

- [OWASP Blocking Brute Force Attacks](https://owasp.org/www-community/controls/Blocking_Brute_Force_Attacks)
- [OWASP Rate Limiting](https://owasp.org/www-community/controls/Rate_Limiting)
- [RFC 6585 - Additional HTTP Status Codes (429)](https://datatracker.ietf.org/doc/html/rfc6585#section-4)
- [NIST SP 800-63B Section 5.2.2 - Rate Limiting](https://pages.nist.gov/800-63-3/sp800-63b.html)
- CWE-307: Improper Restriction of Excessive Authentication Attempts
