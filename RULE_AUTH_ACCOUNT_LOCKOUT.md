# Business Rule: RULE_AUTH_ACCOUNT_LOCKOUT

## Metadata

| Field | Value |
|-------|-------|
| **Rule ID** | RULE_AUTH_ACCOUNT_LOCKOUT |
| **Name** | Account Lockout After Failed Attempts |
| **Domain** | SECURITY |
| **Criticality** | HIGH |
| **Owner** | auth-team |

## Description

Esta regra define a política de bloqueio de conta após tentativas falhas de autenticação no sistema soft-pilot-auth. O bloqueio de conta é uma medida de segurança crítica que protege contra ataques de força bruta direcionados a contas específicas, complementando o rate limiting por IP.

A política implementa bloqueio progressivo baseado no número de tentativas falhas consecutivas. Após um limite inicial, a conta é temporariamente bloqueada por períodos crescentes. Isso permite que usuários legítimos que erraram a senha algumas vezes ainda possam tentar novamente após uma breve espera, enquanto dificulta significativamente ataques automatizados.

O sistema diferencia entre bloqueio temporário (automático) e bloqueio permanente (administrativo). Bloqueios temporários expiram automaticamente, enquanto bloqueios permanentes requerem intervenção de um administrador. Notificações são enviadas ao usuário e aos administradores em casos suspeitos.

## Validation Criteria

- [ ] **Limites de tentativas falhas:**
  - 5 tentativas: bloqueio de 1 minuto
  - 10 tentativas: bloqueio de 5 minutos
  - 15 tentativas: bloqueio de 15 minutos
  - 20 tentativas: bloqueio de 1 hora
  - 25+ tentativas: bloqueio de 24 horas + alerta de segurança
- [ ] Contador resetado após login bem-sucedido
- [ ] Contador resetado após período de 24 horas sem tentativas
- [ ] Email de notificação enviado ao usuário após 5 tentativas falhas
- [ ] Alerta para equipe de segurança após 15 tentativas falhas
- [ ] Bloqueio permanente requer ação manual de admin (is_active = false)
- [ ] Usuário pode desbloquear via "Esqueci minha senha" após bloqueio temporário

## Examples

### ✅ Valid Scenario

```
Usuário: vendedor@empresa.com
Status: Ativo
Tentativas falhas: 3

Tentativa 4:
Email: vendedor@empresa.com
Senha: "SenhaCorreta@123!"

Validação:
✓ Conta não está bloqueada
✓ Senha correta

Resultado: LOGIN BEM-SUCEDIDO
Ação: Contador de tentativas falhas resetado para 0
```

### ❌ Invalid Scenario (Bloqueio temporário)

```
Usuário: usuario@empresa.com
Histórico de tentativas (últimos 10 minutos):
  1. 10:00:00 - Falha
  2. 10:00:30 - Falha
  3. 10:01:00 - Falha
  4. 10:01:30 - Falha
  5. 10:02:00 - Falha → Email de aviso enviado
  
Tentativa 6 (10:02:30):

Response (HTTP 423 Locked):
{
  "error": {
    "code": "ACCOUNT_LOCKED",
    "message": "Conta temporariamente bloqueada devido a múltiplas tentativas falhas. Tente novamente em 1 minuto ou use 'Esqueci minha senha'.",
    "locked_until": "2024-12-22T10:03:00Z",
    "attempts": 5,
    "unlock_options": ["wait", "password_reset"]
  }
}

Resultado: CONTA BLOQUEADA (1 minuto)
Email enviado: "Detectamos múltiplas tentativas de login na sua conta..."
```

### ❌ Invalid Scenario (Escalação de bloqueio)

```
Usuário: vitima@empresa.com
Histórico:
  - 5 tentativas → bloqueio 1 min (expirou)
  - Mais 5 tentativas → bloqueio 5 min (expirou)
  - Mais 5 tentativas → bloqueio 15 min (atual)

Tentativa durante bloqueio:

Response (HTTP 423 Locked):
{
  "error": {
    "code": "ACCOUNT_LOCKED",
    "message": "Conta bloqueada por 15 minutos devido a tentativas excessivas.",
    "locked_until": "2024-12-22T10:30:00Z",
    "attempts": 15,
    "escalation_level": 3
  }
}

Resultado: BLOQUEIO ESCALADO
Alerta: Enviado para equipe de segurança
Log: "SECURITY: Account vitima@empresa.com locked - 15 failed attempts - possible targeted attack"
```

### ❌ Invalid Scenario (Bloqueio severo)

```
Usuário: alvo@empresa.com
Tentativas falhas nas últimas 24h: 25

Response (HTTP 423 Locked):
{
  "error": {
    "code": "ACCOUNT_LOCKED_SEVERE",
    "message": "Conta bloqueada por 24 horas por segurança. Entre em contato com o suporte se você é o proprietário desta conta.",
    "locked_until": "2024-12-23T10:00:00Z",
    "support_required": true
  }
}

Resultado: BLOQUEIO 24 HORAS
Ações automáticas:
1. Email de alerta enviado ao usuário
2. Ticket de segurança criado automaticamente
3. Notificação para administradores da empresa
4. Log de segurança detalhado gerado
```

## Related Rules

- [RULE_AUTH_RATE_LIMITING](./RULE_AUTH_RATE_LIMITING.md) - Rate limiting por IP
- [RULE_AUTH_PASSWORD_STRENGTH](./RULE_AUTH_PASSWORD_STRENGTH.md) - Requisitos de senha
- [RULE_AUTH_MFA_ENFORCEMENT](./RULE_AUTH_MFA_ENFORCEMENT.md) - MFA como proteção adicional

## References

- [OWASP Authentication Cheat Sheet - Account Lockout](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html#account-lockout)
- [NIST SP 800-63B Section 5.2.2 - Throttling](https://pages.nist.gov/800-63-3/sp800-63b.html#throttle)
- CWE-307: Improper Restriction of Excessive Authentication Attempts
- CWE-799: Improper Control of Interaction Frequency
- Microsoft Security Baseline - Account Lockout Policy
- PCI DSS Requirement 8.1.6 - Lockout after not more than 6 attempts
