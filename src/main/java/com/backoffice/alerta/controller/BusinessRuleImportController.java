package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.BusinessRuleImportRequest;
import com.backoffice.alerta.dto.BusinessRuleImportResponse;
import com.backoffice.alerta.importer.BusinessRuleImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * US#68 - Controller REST para importação automática de regras de negócio do Git
 * 
 * Segurança: ADMIN apenas
 * READ-ONLY: Nunca altera o Git, apenas lê
 */
@RestController
@RequestMapping("/risk/rules")
@Tag(name = "Business Rule Import", description = "US#68 - Importação automática de regras de negócio do Git (GitHub/GitLab)")
public class BusinessRuleImportController {

    private static final Logger log = LoggerFactory.getLogger(BusinessRuleImportController.class);

    private final BusinessRuleImportService importService;

    public BusinessRuleImportController(BusinessRuleImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/import")
    // Segurança já configurada no SecurityConfig: .requestMatchers(HttpMethod.POST, "/risk/rules/import").hasRole("ADMIN")
    @Operation(
        summary = "Importa regras de negócio de repositório Git",
        description = """
            **US#68 - Importação Automática de Regras**
            
            Funcionalidades:
            - ✅ Lê repositórios GitHub e GitLab (READ-ONLY)
            - ✅ Detecta regras em comentários, Markdown e YAML
            - ✅ Deduplica por ruleId (CREATE ou UPDATE)
            - ✅ Integra com Vector Store (US#66)
            - ✅ Suporta dry-run (simulação sem persistência)
            
            **Segurança:**
            - 🔒 ADMIN apenas
            - 📖 READ-ONLY absoluto
            - ❌ Nunca escreve no Git
            - ❌ Nunca executa código
            
            **Formatos Suportados:**
            
            1. **Comentários em código (Java):**
            ```java
            // @BusinessRule
            // id: REGRA_VALIDACAO_PIX
            // name: Validação de Chave PIX
            // description: Valida formato da chave PIX
            // domain: PAGAMENTOS
            // criticality: HIGH
            ```
            
            2. **Markdown:**
            ```markdown
            ## Business Rule: REGRA_VALIDACAO_PIX
            - **Name**: Validação de Chave PIX
            - **Description**: Valida formato da chave PIX
            - **Domain**: PAGAMENTOS
            - **Criticality**: HIGH
            ```
            
            3. **YAML:**
            ```yaml
            businessRule:
              id: REGRA_VALIDACAO_PIX
              name: Validação de Chave PIX
              description: Valida formato da chave PIX
              domain: PAGAMENTOS
              criticality: HIGH
            ```
            """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados da importação",
            required = true,
            content = @Content(
                schema = @Schema(implementation = BusinessRuleImportRequest.class),
                examples = {
                    @ExampleObject(
                        name = "Dry-Run (Simulação)",
                        summary = "Modo simulação - não persiste dados",
                        value = """
                            {
                              "projectId": "550e8400-e29b-41d4-a716-446655440001",
                              "provider": "GITHUB",
                              "repositoryUrl": "https://github.com/empresa/backoffice-pagamentos",
                              "branch": "main",
                              "dryRun": true
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Importação Real",
                        summary = "Modo produção - persiste regras",
                        value = """
                            {
                              "projectId": "550e8400-e29b-41d4-a716-446655440001",
                              "provider": "GITHUB",
                              "repositoryUrl": "https://github.com/empresa/backoffice-pagamentos",
                              "branch": "main",
                              "dryRun": false
                            }
                            """
                    )
                }
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Importação concluída com sucesso",
                content = @Content(schema = @Schema(implementation = BusinessRuleImportResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Requisição inválida (projeto não encontrado, URL inválida, etc.)"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Token Git não configurado ou inválido"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Acesso negado - apenas ADMINs"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Erro interno do servidor"
            )
        }
    )
    public ResponseEntity<BusinessRuleImportResponse> importRules(
            @RequestBody BusinessRuleImportRequest request) {
        
        try {
            log.info("📥 [US#68] POST /risk/rules/import | project={} | repo={} | dryRun={}", 
                    request.getProjectId(), request.getRepositoryUrl(), request.isDryRun());

            // Validação básica
            if (request.getProjectId() == null) {
                log.warn("⚠️ [US#68] projectId obrigatório");
                return ResponseEntity.badRequest().build();
            }

            if (request.getProvider() == null) {
                log.warn("⚠️ [US#68] provider obrigatório");
                return ResponseEntity.badRequest().build();
            }

            if (request.getRepositoryUrl() == null || request.getRepositoryUrl().isBlank()) {
                log.warn("⚠️ [US#68] repositoryUrl obrigatório");
                return ResponseEntity.badRequest().build();
            }

            // Executar importação
            BusinessRuleImportResponse response = importService.importRules(request);

            log.info("✅ [US#68] Importação concluída | detectadas={} | criadas={} | atualizadas={}", 
                    response.getRulesDetected(), response.getRulesCreated(), response.getRulesUpdated());

            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            // Token não configurado ou inválido
            log.error("❌ [US#68] Token Git não configurado | error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            
        } catch (IllegalArgumentException e) {
            // Projeto não encontrado, URL inválida, etc.
            log.error("❌ [US#68] Requisição inválida | error={}", e.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (Exception e) {
            log.error("❌ [US#68] Erro inesperado na importação | error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
