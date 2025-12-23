# Business Rule: RULE_AUTH_SESSION_TIMEOUT

## Metadata

| Field | Value |
|-------|-------|
| **Rule ID** | RULE_AUTH_SESSION_TIMEOUT |
| **Name** | Session Inactivity Timeout Policy |
| **Domain** | SECURITY |
| **Criticality** | MEDIUM |
| **Owner** | auth-team |

## Description

Esta regra define o tempo máximo que uma sessão de usuário pode permanecer inativa antes de ser automaticamente encerrada. O timeout de sessão é uma medida crítica de segurança que protege contra acesso não autorizado em dispositivos abandonados ou compartilhados.

O sistema implementa dois tipos de timeout: timeout de inatividade (idle timeout) e timeout absoluto (absolute timeout). O timeout de inatividade é resetado a cada interação do usuário com o sistema, enquanto o timeout absoluto força re-autenticação após um período fixo independente de atividade.

A política considera diferentes perfis de uso e níveis de sensibilidade. Usuários administrativos têm timeouts mais curtos devido ao maior risco associado às suas permissões. O sistema emite avisos antes da expiração para permitir que usuários salvem seu trabalho e renovem a sessão.

## Validation Criteria

- [ ] **Timeout de inatividade (idle):**
  - Roles admin/manager: 15 minutos
  - Role user: 30 minutos
- [ ] **Timeout absoluto (máximo de sessão):**
  - Todos os roles: 24 horas
- [ ] Aviso de expiração exibido 2 minutos antes do timeout
- [ ] Usuário pode renovar sessão clicando em "Continuar" no aviso
- [ ] Após timeout, usuário é redirecionado para login com mensagem explicativa
- [ ] Sessões inativas são invalidadas no servidor (não apenas no cliente)
- [ ] Refresh tokens são revogados junto com a sessão

## Examples

### ✅ Valid Scenario

```
Usuário: analista@empresa.com
Role: user
Timeout configurado: 30 minutos de inatividade

Timeline:
09:00 - Login realizado
09:15 - Última atividade (clique em botão)
09:43 - Sistema exibe aviso: "Sua sessão expirará em 2 minutos"
09:44 - Usuário clica em "Continuar"
09:44 - Timer de inatividade resetado

Resultado: SESSÃO MANTIDA
```

### ❌ Invalid Scenario

```
Usuário: admin@empresa.com
Role: admin
Timeout configurado: 15 minutos de inatividade

Timeline:
14:00 - Login realizado
14:10 - Última atividade
14:23 - Sistema exibe aviso de expiração
14:25 - Aviso ignorado (usuário ausente)
14:25 - Sessão expirada automaticamente
14:30 - Usuário retorna e tenta ação
14:30 - Redirecionado para login

Resultado: SESSÃO ENCERRADA
Mensagem: "Sua sessão expirou por inatividade. Por favor, faça login novamente."
```

### ❌ Invalid Scenario (Timeout absoluto)

```
Usuário: gerente@empresa.com
Role: manager
Sessão iniciada: 08:00 do dia anterior

Timeline:
Dia 1, 08:00 - Login realizado
Dia 2, 07:59 - Usuário ainda ativo (renovando idle timeout)
Dia 2, 08:00 - 24 horas desde login inicial
Dia 2, 08:00 - Sessão encerrada (timeout absoluto)

Resultado: RE-AUTENTICAÇÃO OBRIGATÓRIA
Mensagem: "Sua sessão atingiu o tempo máximo. Por favor, faça login novamente."
```

## Related Rules

- [RULE_AUTH_TOKEN_ROTATION](./RULE_AUTH_TOKEN_ROTATION.md) - Rotação de tokens
- [RULE_AUTH_MFA_ENFORCEMENT](./RULE_AUTH_MFA_ENFORCEMENT.md) - MFA após re-autenticação

## References

- [OWASP Session Management Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html)
- [NIST SP 800-63B Section 7.2 - Session Bindings](https://pages.nist.gov/800-63-3/sp800-63b.html#sec7)
- CWE-613: Insufficient Session Expiration
- PCI DSS Requirement 8.1.8 - Session timeout after 15 minutes of inactivity
