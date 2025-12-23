# Business Rule: RULE_AUTH_PASSWORD_HISTORY

## Metadata

| Field | Value |
|-------|-------|
| **Rule ID** | RULE_AUTH_PASSWORD_HISTORY |
| **Name** | Password History and Reuse Prevention |
| **Domain** | SECURITY |
| **Criticality** | MEDIUM |
| **Owner** | auth-team |

## Description

Esta regra define a política de histórico de senhas no sistema soft-pilot-auth, impedindo que usuários reutilizem senhas recentes. A prevenção de reutilização de senhas é uma medida importante para garantir que credenciais comprometidas não sejam reintroduzidas no sistema.

O sistema mantém um histórico criptografado (hash) das últimas N senhas de cada usuário. Quando uma nova senha é definida, ela é comparada contra este histórico para verificar se já foi utilizada anteriormente. As senhas no histórico são armazenadas usando o mesmo algoritmo de hash das senhas ativas (bcrypt com salt único).

Esta política trabalha em conjunto com a política de complexidade de senha (RULE_AUTH_PASSWORD_STRENGTH) e políticas de expiração. Juntas, elas garantem que senhas sejam regularmente atualizadas com credenciais genuinamente novas e seguras.

## Validation Criteria

- [ ] Sistema armazena hash das últimas 12 senhas de cada usuário
- [ ] Nova senha não pode ser igual a nenhuma das 12 anteriores
- [ ] Comparação usa timing-safe comparison para evitar timing attacks
- [ ] Hashes de histórico são armazenados com bcrypt (cost factor 12)
- [ ] Histórico é mantido mesmo após reset de senha por admin
- [ ] Usuário é informado quantas senhas anteriores são bloqueadas
- [ ] Senha atual conta como parte do histórico (não pode "trocar" pela mesma)

## Examples

### ✅ Valid Scenario

```
Usuário: colaborador@empresa.com
Histórico de senhas (últimas 12):
  1. Senh@Atual2024!     (atual)
  2. Senh@Anterior2024!  (3 meses atrás)
  3. Segur@2023!!        (6 meses atrás)
  ... (mais 9 senhas antigas)

Tentativa de nova senha:
Nova: "Nov@Senha2025!!"

Validação:
✓ Atende requisitos de complexidade
✓ Não está no histórico de 12 senhas
✓ Não é similar à senha atual

Resultado: SENHA ALTERADA COM SUCESSO
Ação: Hash da senha anterior movido para histórico
```

### ❌ Invalid Scenario (Reutilização recente)

```
Usuário: analista@empresa.com
Histórico de senhas (últimas 12):
  1. Senh@Atual2024!     (atual)
  2. Minha$enha2024!     (2 meses atrás)
  3. Segura@2023!!       (4 meses atrás)
  ...

Tentativa de nova senha:
Nova: "Minha$enha2024!"

Validação:
✓ Atende requisitos de complexidade
✗ Senha encontrada no histórico (posição 2)

Response:
{
  "error": {
    "code": "PASSWORD_RECENTLY_USED",
    "message": "Esta senha foi utilizada recentemente. Escolha uma senha que não tenha sido usada nas últimas 12 vezes."
  }
}

Resultado: SENHA REJEITADA
```

### ❌ Invalid Scenario (Mesma senha atual)

```
Usuário: dev@empresa.com
Senha atual: "Develop3r@2024!"

Tentativa de "alterar" senha:
Nova: "Develop3r@2024!" (idêntica à atual)

Validação:
✗ Nova senha é igual à senha atual

Response:
{
  "error": {
    "code": "PASSWORD_SAME_AS_CURRENT",
    "message": "A nova senha deve ser diferente da senha atual."
  }
}

Resultado: SENHA REJEITADA
```

### ❌ Invalid Scenario (Variação mínima)

```
Usuário: gerente@empresa.com
Senha atual: "Gerente@2024!"

Tentativa de nova senha:
Nova: "Gerente@2025!" (apenas mudou o ano)

Validação (opcional - se similaridade check ativado):
✗ Nova senha muito similar à atual (>80% similaridade)

Response:
{
  "error": {
    "code": "PASSWORD_TOO_SIMILAR",
    "message": "A nova senha é muito similar à anterior. Escolha uma senha significativamente diferente."
  }
}

Resultado: SENHA REJEITADA (se check de similaridade ativo)
```

## Related Rules

- [RULE_AUTH_PASSWORD_STRENGTH](./RULE_AUTH_PASSWORD_STRENGTH.md) - Requisitos de complexidade
- [RULE_AUTH_ACCOUNT_LOCKOUT](./RULE_AUTH_ACCOUNT_LOCKOUT.md) - Bloqueio de conta

## References

- [NIST SP 800-63B Section 5.1.1.2 - Memorized Secret Verifiers](https://pages.nist.gov/800-63-3/sp800-63b.html#memsecretver)
- [OWASP Authentication Cheat Sheet - Password Storage](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)
- CWE-262: Not Using Password Aging
- CWE-263: Password Aging with Long Expiration
- PCI DSS Requirement 8.2.5 - Do not allow new password same as last four
