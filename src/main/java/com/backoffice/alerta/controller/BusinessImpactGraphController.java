package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.BusinessImpactGraphResponse;
import com.backoffice.alerta.dto.BusinessImpactRequest;
import com.backoffice.alerta.service.BusinessImpactGraphService;
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
 * Controller REST para visualização de impacto sistêmico (grafo de dependências)
 * 
 * ⚠️ IMPORTANTE: 100% READ-ONLY e VISUALIZAÇÃO
 * - NÃO recalcula risco
 * - NÃO altera decisões
 * - NÃO cria notificações
 * - NÃO cria SLAs
 * - NÃO cria auditorias
 * - Apenas organiza dados em formato visual (grafo)
 * 
 * US#37 - Visualização de Impacto Sistêmico (Mapa de Dependências)
 */
@RestController
@RequestMapping("/risk/business-impact")
@Tag(name = "Business Impact Graph", description = "Visualização de impacto sistêmico (Read-Only)")
public class BusinessImpactGraphController {
    
    private static final Logger log = LoggerFactory.getLogger(BusinessImpactGraphController.class);
    
    private final BusinessImpactGraphService graphService;
    
    public BusinessImpactGraphController(BusinessImpactGraphService graphService) {
        this.graphService = graphService;
    }
    
    @PostMapping("/graph")
    @Operation(
        summary = "🗺️ Gerar grafo visual de impacto sistêmico",
        description = """
            Gera um grafo interativo mostrando todas as regras de negócio impactadas por um Pull Request,
            incluindo dependências e cadeias de impacto (direto/indireto/cascata).
            
            **Objetivo:**
            Permitir visualização executiva do alcance sistêmico de mudanças, com:
            - Nós coloridos por nível de impacto (azul/amarelo/vermelho)
            - Arestas mostrando tipo de dependência (FEEDS, DEPENDS_ON, etc)
            - Alertas visuais para regras críticas e com histórico de incidentes
            
            **Formato de resposta:**
            - `nodes`: Lista de regras (nós do grafo) com metadados visuais
            - `edges`: Lista de dependências (arestas do grafo)
            - `summary`: Sumário executivo com contadores
            
            **Uso no frontend:**
            - React Flow: `nodes` e `edges` podem ser usados diretamente
            - Vis Network: Formato compatível
            - Recharts: Requer transformação adicional
            
            **⚠️ READ-ONLY**: 
            - Não altera dados
            - Não recalcula risco
            - Apenas organiza informações existentes para visualização
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Grafo de impacto gerado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BusinessImpactGraphResponse.class),
                examples = @ExampleObject(
                    name = "Exemplo de grafo completo",
                    value = """
                        {
                          "pullRequestId": "PR-789",
                          "nodes": [
                            {
                              "ruleId": "BR-PAYMENT-001",
                              "ruleName": "REGRA_VALIDACAO_PAGAMENTO",
                              "domain": "PAYMENT",
                              "criticality": "CRITICA",
                              "impactLevel": "DIRECT",
                              "ownerships": [
                                {
                                  "teamName": "Payment Team",
                                  "role": "PRIMARY_OWNER"
                                }
                              ],
                              "hasIncidents": true
                            },
                            {
                              "ruleId": "BR-INVOICE-002",
                              "ruleName": "REGRA_GERACAO_FATURA",
                              "domain": "BILLING",
                              "criticality": "ALTA",
                              "impactLevel": "INDIRECT",
                              "ownerships": [
                                {
                                  "teamName": "Billing Team",
                                  "role": "PRIMARY_OWNER"
                                }
                              ],
                              "hasIncidents": false
                            },
                            {
                              "ruleId": "BR-REPORT-003",
                              "ruleName": "REGRA_RELATORIO_FINANCEIRO",
                              "domain": "BILLING",
                              "criticality": "MEDIA",
                              "impactLevel": "CASCADE",
                              "ownerships": [
                                {
                                  "teamName": "Analytics Team",
                                  "role": "PRIMARY_OWNER"
                                }
                              ],
                              "hasIncidents": false
                            }
                          ],
                          "edges": [
                            {
                              "sourceRuleId": "BR-PAYMENT-001",
                              "targetRuleId": "BR-INVOICE-002",
                              "dependencyType": "FEEDS"
                            },
                            {
                              "sourceRuleId": "BR-INVOICE-002",
                              "targetRuleId": "BR-REPORT-003",
                              "dependencyType": "FEEDS"
                            }
                          ],
                          "summary": {
                            "totalRules": 3,
                            "direct": 1,
                            "indirect": 1,
                            "cascade": 1,
                            "criticalRules": 1,
                            "requiresExecutiveAttention": false
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
        description = "Dados do Pull Request para geração do grafo",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = BusinessImpactRequest.class),
            examples = @ExampleObject(
                name = "Exemplo de request",
                value = """
                    {
                      "pullRequestId": "PR-789",
                      "changedFiles": [
                        "src/main/java/com/app/payment/PaymentService.java",
                        "src/main/java/com/app/payment/PaymentValidator.java"
                      ]
                    }
                    """
            )
        )
    )
    public ResponseEntity<BusinessImpactGraphResponse> generateImpactGraph(
            @RequestBody BusinessImpactRequest request) {
        
        log.info("🗺️ [GRAPH-API] Recebendo request de grafo para PR: {}", 
            request.getPullRequestId());
        
        if (request.getChangedFiles() == null || request.getChangedFiles().isEmpty()) {
            log.warn("⚠️ [GRAPH-API] Request inválido: nenhum arquivo fornecido");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            BusinessImpactGraphResponse response = graphService.generateImpactGraph(request);
            
            log.info("✅ [GRAPH-API] Grafo gerado: {} nós, {} arestas",
                response.getNodes().size(),
                response.getEdges().size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ [GRAPH-API] Erro ao gerar grafo: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
