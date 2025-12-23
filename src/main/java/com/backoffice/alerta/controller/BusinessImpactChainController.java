package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.BusinessImpactChainResponse;
import com.backoffice.alerta.dto.BusinessImpactRequest;
import com.backoffice.alerta.service.BusinessRuleImpactChainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para análise de impacto cruzado com cadeia de dependências
 * 
 * ⚠️ IMPORTANTE: 100% READ-ONLY
 * - NÃO recalcula risco
 * - NÃO altera decisões
 * - NÃO cria notificações
 * - NÃO cria SLAs
 * - NÃO cria auditorias
 * - Apenas analisa e explica cadeias de dependência
 * 
 * US#36 - Análise de Impacto Cruzado (Cadeia de Regras Afetadas)
 */
@RestController
@RequestMapping("/risk/business-impact")
@Tag(name = "Business Impact Chain", description = "Análise de impacto cruzado e cadeia de dependências (Read-Only)")
public class BusinessImpactChainController {
    
    private static final Logger log = LoggerFactory.getLogger(BusinessImpactChainController.class);
    
    private final BusinessRuleImpactChainService impactChainService;
    
    public BusinessImpactChainController(BusinessRuleImpactChainService impactChainService) {
        this.impactChainService = impactChainService;
    }
    
    @PostMapping("/chain")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER', 'ENGINEER')")
    @Operation(
        summary = "🔗 Analisar impacto cruzado com cadeia de dependências",
        description = """
            Analisa o impacto completo de um Pull Request considerando não apenas regras diretamente 
            impactadas, mas também regras que dependem delas (impacto indireto) e regras em cascata 
            (múltiplos níveis de dependência).
            
            **Casos de uso:**
            - "Este PR altera PaymentService, que afeta InvoiceService, que afeta ReportService"
            - "Mudança na regra de cálculo pode impactar regras de validação downstream"
            - "Identificar alcance total de mudanças em regras críticas"
            
            **Níveis de impacto:**
            - **DIRECT**: Arquivo alterado implementa esta regra diretamente
            - **INDIRECT**: Esta regra depende de uma regra diretamente impactada (1 nível)
            - **CASCADE**: Esta regra está a 2-3 níveis de distância (efeito cascata)
            
            **Proteções:**
            - Detecta e evita loops infinitos
            - Limita profundidade máxima a 3 níveis
            - Não duplica regras já analisadas
            
            **⚠️ READ-ONLY**: Não altera decisões, não recalcula risco, não gera notificações.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Análise de impacto cruzado realizada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BusinessImpactChainResponse.class),
                examples = @ExampleObject(
                    name = "Exemplo de análise com cadeia",
                    value = """
                        {
                          "pullRequestId": "PR-456",
                          "directImpacts": [
                            {
                              "businessRuleId": "BR-PAYMENT-001",
                              "ruleName": "REGRA_VALIDACAO_PAGAMENTO",
                              "impactLevel": "DIRECT",
                              "dependencyPath": ["BR-PAYMENT-001"],
                              "criticality": "CRITICA",
                              "ownerships": [
                                {
                                  "teamName": "Payment Team",
                                  "role": "PRIMARY_OWNER"
                                }
                              ]
                            }
                          ],
                          "indirectImpacts": [
                            {
                              "businessRuleId": "BR-INVOICE-002",
                              "ruleName": "REGRA_GERACAO_FATURA",
                              "impactLevel": "INDIRECT",
                              "dependencyPath": ["BR-PAYMENT-001", "BR-INVOICE-002"],
                              "criticality": "ALTA",
                              "ownerships": [
                                {
                                  "teamName": "Billing Team",
                                  "role": "PRIMARY_OWNER"
                                }
                              ]
                            }
                          ],
                          "cascadeImpacts": [
                            {
                              "businessRuleId": "BR-REPORT-003",
                              "ruleName": "REGRA_RELATORIO_FINANCEIRO",
                              "impactLevel": "CASCADE",
                              "dependencyPath": ["BR-PAYMENT-001", "BR-INVOICE-002", "BR-REPORT-003"],
                              "criticality": "MEDIA",
                              "ownerships": [
                                {
                                  "teamName": "Analytics Team",
                                  "role": "PRIMARY_OWNER"
                                }
                              ]
                            }
                          ],
                          "summary": {
                            "totalRulesAffected": 3,
                            "highestCriticality": "CRITICA",
                            "requiresExecutiveAttention": true
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Request inválido (PR ID ou arquivos ausentes)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acesso negado (requer ADMIN, RISK_MANAGER ou ENGINEER)",
            content = @Content(mediaType = "application/json")
        )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Dados do Pull Request para análise de impacto cruzado",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = BusinessImpactRequest.class),
            examples = @ExampleObject(
                name = "Exemplo de request",
                value = """
                    {
                      "pullRequestId": "PR-456",
                      "changedFiles": [
                        "src/main/java/com/app/payment/PaymentService.java",
                        "src/main/java/com/app/payment/PaymentValidator.java"
                      ]
                    }
                    """
            )
        )
    )
    public ResponseEntity<BusinessImpactChainResponse> analyzeImpactChain(
            @RequestBody BusinessImpactRequest request) {
        
        log.info("🔗 [CHAIN-API] Recebendo análise de impacto cruzado para PR: {}", 
            request.getPullRequestId());
        
        if (request.getChangedFiles() == null || request.getChangedFiles().isEmpty()) {
            log.warn("⚠️ [CHAIN-API] Request inválido: nenhum arquivo fornecido");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            BusinessImpactChainResponse response = impactChainService.analyzeImpactChain(request);
            
            log.info("✅ [CHAIN-API] Análise concluída: {} regras afetadas no total ({}D, {}I, {}C)",
                response.getSummary().getTotalRulesAffected(),
                response.getDirectImpacts().size(),
                response.getIndirectImpacts().size(),
                response.getCascadeImpacts().size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ [CHAIN-API] Erro ao analisar impacto cruzado: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
