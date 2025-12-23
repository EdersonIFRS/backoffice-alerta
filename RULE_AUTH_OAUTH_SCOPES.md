# Business Rule: RULE_AUTH_OAUTH_SCOPES

## Metadata

| Field | Value |
|-------|-------|
| **Rule ID** | RULE_AUTH_OAUTH_SCOPES |
| **Name** | OAuth Scopes and Permission Restrictions |
| **Domain** | AUTHORIZATION |
| **Criticality** | HIGH |
| **Owner** | auth-team |

## Description

Esta regra define os escopos OAuth permitidos e as restrições de permissões para autenticação federada no sistema soft-pilot-auth, especialmente para integração com Microsoft Azure AD. A correta configuração de escopos é essencial para o princípio de menor privilégio, solicitando apenas as permissões necessárias para o funcionamento da aplicação.

O sistema utiliza OAuth 2.0 com PKCE (Proof Key for Code Exchange) para fluxos de autenticação via Microsoft. Os escopos são cuidadosamente selecionados para obter informações de perfil do usuário sem acesso excessivo aos dados da conta Microsoft. Escopos que permitem acesso a emails, calendário ou outros dados sensíveis não são solicitados.

A política também define como permissões OAuth são mapeadas para roles internos do sistema. Usuários autenticados via SSO Microsoft herdam permissões baseadas em seu domínio de email corporativo e configurações da empresa no sistema, não nos escopos OAuth concedidos.

## Validation Criteria

- [ ] **Escopos Microsoft OAuth permitidos:**
  - `openid` - Autenticação básica (obrigatório)
  - `profile` - Nome e informações básicas de perfil
  - `email` - Endereço de email do usuário
  - `User.Read` - Leitura do perfil do usuário logado
- [ ] **Escopos PROIBIDOS (nunca solicitar):**
  - `Mail.Read`, `Mail.ReadWrite` - Acesso a emails
  - `Calendars.Read`, `Calendars.ReadWrite` - Acesso a calendário
  - `Files.Read`, `Files.ReadWrite` - Acesso a OneDrive
  - `Directory.Read.All` - Acesso ao diretório corporativo
  - Qualquer escopo `.All` ou `admin_consent`
- [ ] Tokens OAuth são validados no callback antes de criar sessão
- [ ] Domínio de email deve corresponder a empresa cadastrada no sistema
- [ ] Refresh tokens Microsoft são armazenados criptografados

## Examples

### ✅ Valid Scenario

```
Usuário inicia login via Microsoft
Empresa: SoftDesign (domínio: softdesign.com.br)

Redirect para Microsoft:
https://login.microsoftonline.com/common/oauth2/v2.0/authorize
  ?client_id=xxx
  &response_type=code
  &redirect_uri=https://app.com/auth/callback
  &scope=openid profile email User.Read
  &code_challenge=xxx
  &code_challenge_method=S256

Microsoft retorna autorização:
- Email: joao@softdesign.com.br
- Nome: João Silva
- Microsoft ID: abc-123-xxx

Sistema valida:
✓ Escopos concedidos são apenas os permitidos
✓ Domínio softdesign.com.br existe no sistema
✓ Empresa SoftDesign está ativa

Resultado: LOGIN SSO BEM-SUCEDIDO
Role atribuído: user (baseado em configuração da empresa, não OAuth)
```

### ❌ Invalid Scenario (Domínio não autorizado)

```
Usuário: maria@empresa-externa.com
Escopos concedidos: openid, profile, email, User.Read

Validação:
✓ Escopos estão corretos
✗ Domínio empresa-externa.com não cadastrado no sistema

Response:
{
  "error": {
    "code": "DOMAIN_NOT_AUTHORIZED",
    "message": "Seu domínio de email não está autorizado. Entre em contato com o administrador."
  }
}

Resultado: LOGIN REJEITADO
Log: "OAuth login rejected - domain not authorized: empresa-externa.com"
```

### ❌ Invalid Scenario (Escopo não permitido detectado)

```
Cenário: Aplicação maliciosa tenta solicitar escopos extras

Request interceptado com escopos:
scope=openid profile email User.Read Mail.Read

Validação:
✗ Escopo Mail.Read não é permitido pela política

Ação:
1. Request rejeitado antes de enviar para Microsoft
2. Alerta de segurança gerado
3. Investigação de possível comprometimento

Resultado: FLUXO ABORTADO
Log: "SECURITY ALERT: Unauthorized OAuth scope requested: Mail.Read"
```

## Related Rules

- [RULE_AUTH_MFA_ENFORCEMENT](./RULE_AUTH_MFA_ENFORCEMENT.md) - MFA após SSO
- [RULE_AUTH_TOKEN_ROTATION](./RULE_AUTH_TOKEN_ROTATION.md) - Rotação de tokens

## References

- [Microsoft Identity Platform Scopes](https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-permissions-and-consent)
- [OAuth 2.0 Security Best Current Practice](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-security-topics)
- [RFC 7636 - PKCE](https://datatracker.ietf.org/doc/html/rfc7636)
- [OWASP OAuth Security Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/OAuth2_Security_Cheat_Sheet.html)
- CWE-863: Incorrect Authorization
