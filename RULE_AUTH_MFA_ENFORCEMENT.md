# Business Rule: RULE_AUTH_MFA_ENFORCEMENT

## Metadata

| Field | Value |
|-------|-------|
| **Rule ID** | RULE_AUTH_MFA_ENFORCEMENT |
| **Name** | Multi-Factor Authentication Enforcement Policy |
| **Domain** | AUTHENTICATION |
| **Criticality** | HIGH |
| **Owner** | auth-team |

## Description

Esta regra define quando a autenticação multi-fator (MFA) é obrigatória versus opcional no sistema soft-pilot-auth. MFA adiciona uma camada extra de segurança além da senha, reduzindo significativamente o risco de comprometimento de conta mesmo quando credenciais são vazadas.

A política de MFA é baseada no princípio de segurança proporcional ao risco. Usuários com acesso a dados sensíveis ou funções administrativas devem obrigatoriamente utilizar MFA, enquanto usuários com permissões limitadas podem optar por habilitá-lo voluntariamente.

O sistema suporta múltiplos fatores de autenticação: TOTP (Time-based One-Time Password) via aplicativos como Google Authenticator ou Authy, e autenticação via Microsoft Authenticator para usuários que fazem login via SSO Microsoft. A configuração de MFA deve ser concluída durante o primeiro login após a criação da conta para roles obrigatórios.

## Validation Criteria

- [ ] **Obrigatório para roles:** admin, manager
- [ ] **Opcional para roles:** user
- [ ] MFA deve ser configurado no primeiro login após criação de conta (para roles obrigatórios)
- [ ] Usuário não pode acessar recursos protegidos sem MFA ativo (quando obrigatório)
- [ ] Backup codes devem ser gerados durante setup de MFA (mínimo 10 códigos)
- [ ] Códigos TOTP têm validade de 30 segundos com janela de tolerância de ±1 período
- [ ] Máximo de 3 tentativas de código MFA antes de requerer nova autenticação primária

## Examples

### ✅ Valid Scenario

```
Usuário: admin@empresa.com
Role: admin
MFA Status: Configurado (TOTP via Google Authenticator)

Fluxo de Login:
1. Usuário insere email e senha ✓
2. Sistema valida credenciais ✓
3. Sistema solicita código MFA ✓
4. Usuário insere código "847291" do app ✓
5. Sistema valida código (dentro da janela de 30s) ✓
6. Login concluído com sucesso ✓

Resultado: ACESSO PERMITIDO
```

### ❌ Invalid Scenario

```
Usuário: gerente@empresa.com
Role: manager
MFA Status: Não configurado

Fluxo de Login:
1. Usuário insere email e senha ✓
2. Sistema valida credenciais ✓
3. Sistema detecta role "manager" requer MFA ✓
4. Sistema detecta MFA não configurado ✗
5. Usuário redirecionado para setup obrigatório de MFA

Resultado: ACESSO BLOQUEADO ATÉ CONFIGURAR MFA
Mensagem: "Sua conta requer autenticação multi-fator. Configure o MFA para continuar."
```

### ❌ Invalid Scenario (Código expirado)

```
Usuário: admin@empresa.com
Role: admin
MFA Status: Configurado

Fluxo:
1. Credenciais validadas ✓
2. Código MFA inserido: "123456"
3. Código gerado há 2 minutos (expirado) ✗

Resultado: CÓDIGO REJEITADO
Mensagem: "Código inválido ou expirado. Utilize o código atual do seu aplicativo autenticador."
```

## Related Rules

- [RULE_AUTH_SESSION_TIMEOUT](./RULE_AUTH_SESSION_TIMEOUT.md) - Expiração de sessão
- [RULE_AUTH_ACCOUNT_LOCKOUT](./RULE_AUTH_ACCOUNT_LOCKOUT.md) - Bloqueio após tentativas falhas
- [RULE_AUTH_OAUTH_SCOPES](./RULE_AUTH_OAUTH_SCOPES.md) - Escopos OAuth (SSO Microsoft)

## References

- [OWASP Multi-Factor Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Multifactor_Authentication_Cheat_Sheet.html)
- [NIST SP 800-63B Section 5.1.3 - Multi-Factor Authenticators](https://pages.nist.gov/800-63-3/sp800-63b.html#sec5)
- [RFC 6238 - TOTP: Time-Based One-Time Password Algorithm](https://datatracker.ietf.org/doc/html/rfc6238)
- LGPD Art. 46 - Medidas de segurança para proteção de dados pessoais
