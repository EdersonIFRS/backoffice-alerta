# Business Rule: RULE_AUTH_PASSWORD_STRENGTH

## Metadata

| Field | Value |
|-------|-------|
| **Rule ID** | RULE_AUTH_PASSWORD_STRENGTH |
| **Name** | Password Complexity Requirements |
| **Domain** | SECURITY |
| **Criticality** | HIGH |
| **Owner** | auth-team |

## Description

Esta regra define os requisitos mínimos de complexidade para senhas de usuários no sistema soft-pilot-auth. Senhas fortes são a primeira linha de defesa contra ataques de força bruta e comprometimento de credenciais.

A política de senha foi projetada seguindo as recomendações do NIST SP 800-63B e OWASP Authentication Cheat Sheet. O objetivo é equilibrar segurança robusta com usabilidade, evitando requisitos excessivamente complexos que levem usuários a adotar práticas inseguras como anotação de senhas.

Todas as senhas devem atender aos critérios mínimos antes de serem aceitas pelo sistema. A validação ocorre tanto no frontend (feedback imediato ao usuário) quanto no backend (validação autoritativa). Senhas que não atendem aos requisitos são rejeitadas com mensagens de erro claras indicando quais critérios falharam.

## Validation Criteria

- [ ] Comprimento mínimo de 12 caracteres
- [ ] Pelo menos 1 letra maiúscula (A-Z)
- [ ] Pelo menos 1 letra minúscula (a-z)
- [ ] Pelo menos 1 número (0-9)
- [ ] Pelo menos 1 caractere especial (!@#$%^&*()_+-=[]{}|;':\",./<>?)
- [ ] Não pode conter o email do usuário ou partes dele
- [ ] Não pode conter sequências óbvias (123456, qwerty, etc.)
- [ ] Não pode estar em listas de senhas vazadas conhecidas (haveibeenpwned check)

## Examples

### ✅ Valid Scenario

```
Email: joao.silva@empresa.com
Senha: "M1nh@Senh@Segur@2024!"

Validação:
✓ 21 caracteres (≥12)
✓ Contém maiúsculas: M, S, S
✓ Contém minúsculas: i, n, h, a, e, n, h, a, e, g, u, r, a
✓ Contém números: 1, 2, 0, 2, 4
✓ Contém especiais: @, @, @, !
✓ Não contém partes do email
✓ Não é sequência conhecida

Resultado: ACEITA
```

### ❌ Invalid Scenario

```
Email: joao.silva@empresa.com
Senha: "Joao2024"

Validação:
✗ 8 caracteres (< 12 mínimo)
✓ Contém maiúsculas: J
✓ Contém minúsculas: o, a, o
✓ Contém números: 2, 0, 2, 4
✗ Não contém caractere especial
✗ Contém parte do email (joao)

Resultado: REJEITADA
Mensagem: "A senha deve ter no mínimo 12 caracteres, incluir pelo menos um caractere especial e não pode conter partes do seu email."
```

## Related Rules

- [RULE_AUTH_PASSWORD_HISTORY](./RULE_AUTH_PASSWORD_HISTORY.md) - Histórico de senhas anteriores
- [RULE_AUTH_ACCOUNT_LOCKOUT](./RULE_AUTH_ACCOUNT_LOCKOUT.md) - Bloqueio após tentativas falhas

## References

- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- [NIST SP 800-63B Digital Identity Guidelines](https://pages.nist.gov/800-63-3/sp800-63b.html)
- [Have I Been Pwned API](https://haveibeenpwned.com/API/v3)
- CWE-521: Weak Password Requirements
