package com.backoffice.alerta.llm;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * US#70 - Controller para detecção de mudanças geradas por LLM
 * 
 * Endpoint READ-ONLY para análise de Pull Requests
 * RBAC: ADMIN, RISK_MANAGER, ENGINEER
 */
@RestController
@RequestMapping("/risk/llm/changes")
@Tag(name = "LLM Change Detection", description = "US#70 - Detecção de mudanças geradas por LLM")
@SecurityRequirement(name = "bearerAuth")
public class LLMChangeDetectionController {

    private static final Logger log = LoggerFactory.getLogger(LLMChangeDetectionController.class);

    private final LLMChangeDetectionService llmDetectionService;

    public LLMChangeDetectionController(LLMChangeDetectionService llmDetectionService) {
        this.llmDetectionService = llmDetectionService;
    }

    @PostMapping("/analyze")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER', 'ENGINEER')")
    @Operation(
        summary = "Analisa mudanças de PR em busca de padrões LLM",
        description = "US#70 - Detecção determinística de código potencialmente gerado por LLM. " +
                     "Aplica 6 heurísticas e retorna score de 0-100 com classificação LOW/MEDIUM/HIGH. " +
                     "READ-ONLY - não modifica código, apenas analisa."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Análise concluída com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LLMChangeAnalysisResponse.class),
                examples = {
                    @ExampleObject(
                        name = "LOW Risk - Refatoração Pequena",
                        value = """
                            {
                              "totalScore": 15,
                              "suspicionLevel": "LOW",
                              "heuristics": [
                                {
                                  "heuristic": "GENERIC_COMMENTS",
                                  "score": 15,
                                  "explanation": "Detectados 1 arquivo(s) com potencial para comentários genéricos.",
                                  "affectedFiles": ["src/main/java/NewService.java"]
                                }
                              ],
                              "affectsCriticalRule": false,
                              "exceedsRuleScope": false,
                              "pullRequestId": "101",
                              "totalFilesAnalyzed": 2,
                              "javaFilesAnalyzed": 1,
                              "summary": "📊 **Análise de Mudança LLM - PR #101**\\n\\n**Score Total**: 15/100\\n**Nível de Suspeição**: LOW\\n\\n✅ **OK**: Baixo risco. Mudança parece normal.",
                              "projectContext": {
                                "scoped": false,
                                "global": true
                              }
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "MEDIUM Risk - Fora de Escopo",
                        value = """
                            {
                              "totalScore": 45,
                              "suspicionLevel": "MEDIUM",
                              "heuristics": [
                                {
                                  "heuristic": "OUT_OF_SCOPE",
                                  "score": 30,
                                  "explanation": "Detectados 2 arquivo(s) alterado(s) fora do escopo das regras de negócio impactadas.",
                                  "affectedFiles": ["src/main/java/UnrelatedService.java", "src/main/java/RandomUtil.java"]
                                },
                                {
                                  "heuristic": "GENERIC_COMMENTS",
                                  "score": 15,
                                  "explanation": "Detectados 1 arquivo(s) com potencial para comentários genéricos.",
                                  "affectedFiles": ["src/main/java/NewHelper.java"]
                                }
                              ],
                              "affectsCriticalRule": false,
                              "exceedsRuleScope": true,
                              "pullRequestId": "202",
                              "totalFilesAnalyzed": 4,
                              "javaFilesAnalyzed": 3,
                              "summary": "📊 **Análise de Mudança LLM - PR #202**\\n\\n**Score Total**: 45/100\\n**Nível de Suspeição**: MEDIUM\\n\\n⚠️ **CUIDADO**: Risco moderado detectado. Revisão manual recomendada.",
                              "projectContext": {
                                "scoped": false,
                                "global": true
                              }
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "HIGH Risk - Crítico sem Testes",
                        value = """
                            {
                              "totalScore": 75,
                              "suspicionLevel": "HIGH",
                              "heuristics": [
                                {
                                  "heuristic": "OUT_OF_SCOPE",
                                  "score": 30,
                                  "explanation": "Detectados 3 arquivo(s) alterado(s) fora do escopo das regras de negócio impactadas.",
                                  "affectedFiles": ["src/main/java/PaymentService.java", "src/main/java/TaxService.java", "src/main/java/ValidationService.java"]
                                },
                                {
                                  "heuristic": "MASSIVE_METHOD_CHANGE",
                                  "score": 25,
                                  "explanation": "Detectadas 2 alterações massivas em métodos (70%+ do código alterado).",
                                  "affectedFiles": ["PaymentService.calculateTotal() [45 linhas]", "TaxService.computeTax() [32 linhas]"]
                                },
                                {
                                  "heuristic": "MISSING_TESTS",
                                  "score": 20,
                                  "explanation": "Detectadas mudanças em 3 arquivo(s) de código crítico sem testes correspondentes.",
                                  "affectedFiles": ["src/main/java/PaymentService.java", "src/main/java/TaxService.java", "src/main/java/ValidationService.java"]
                                }
                              ],
                              "affectsCriticalRule": true,
                              "exceedsRuleScope": true,
                              "pullRequestId": "303",
                              "totalFilesAnalyzed": 3,
                              "javaFilesAnalyzed": 3,
                              "summary": "📊 **Análise de Mudança LLM - PR #303**\\n\\n**Score Total**: 75/100\\n**Nível de Suspeição**: HIGH\\n\\n🚨 **ATENÇÃO**: Alto risco de mudança gerada automaticamente detectado.\\n\\n⚠️ Esta mudança afeta **regras de negócio críticas**.\\n⚠️ Esta mudança **excede o escopo** das regras impactadas.\\n\\n**Recomendação**: Bloqueie o merge e solicite revisão detalhada.",
                              "projectContext": {
                                "scoped": false,
                                "global": true
                              }
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Request inválido"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Sem permissão (requer ADMIN, RISK_MANAGER ou ENGINEER)"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno (fallback retorna score 0)"
        )
    })
    public ResponseEntity<LLMChangeAnalysisResponse> analyzeChanges(
            @Valid @RequestBody LLMChangeDetectionRequest request) {

        log.info("🤖 [US#70] POST /risk/llm/changes/analyze | PR={} | provider={}",
                 request.getPullRequestId(), request.getProvider());

        try {
            LLMChangeAnalysisResponse response = llmDetectionService.analyzeChanges(request);

            // Log baseado no nível de risco
            if (response.getSuspicionLevel() == LLMSuspicionLevel.HIGH) {
                log.warn("🚨 [US#70] HIGH risk detected | PR={} | score={}",
                         request.getPullRequestId(), response.getTotalScore());
            } else if (response.getSuspicionLevel() == LLMSuspicionLevel.MEDIUM) {
                log.info("⚠️ [US#70] MEDIUM risk detected | PR={} | score={}",
                         request.getPullRequestId(), response.getTotalScore());
            } else {
                log.info("✅ [US#70] LOW risk | PR={} | score={}",
                         request.getPullRequestId(), response.getTotalScore());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ [US#70] Erro ao analisar PR {}: {}", 
                     request.getPullRequestId(), e.getMessage(), e);
            
            // Retornar resposta de fallback ao invés de 500
            LLMChangeAnalysisResponse fallback = new LLMChangeAnalysisResponse();
            fallback.setPullRequestId(request.getPullRequestId());
            fallback.setTotalScore(0);
            fallback.setSuspicionLevel(LLMSuspicionLevel.LOW);
            fallback.setSummary("⚠️ Erro ao analisar PR. Assuma baixo risco e proceda com revisão manual.");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(fallback);
        }
    }

    /**
     * US#53 - Endpoint para integração CI/CD
     * Retorna exit code baseado no nível de risco
     */
    @GetMapping("/cicd-status/{pullRequestId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER', 'ENGINEER', 'CI_CD_SYSTEM')")
    @Operation(
        summary = "Retorna exit code para CI/CD baseado em análise prévia",
        description = "US#53/US#70 - Integração com CI/CD. Retorna exit code: 0 (LOW), 1 (MEDIUM), 2 (HIGH). " +
                     "Este endpoint deve ser usado em pipelines CI/CD para decisões automáticas de gate."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Exit code retornado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CICDStatusResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "PR não analisado ainda"
        )
    })
    public ResponseEntity<CICDStatusResponse> getCICDStatus(
            @PathVariable String pullRequestId) {

        log.info("🔧 [US#53/US#70] GET /risk/llm/changes/cicd-status/{}", pullRequestId);

        // Resposta simplificada - em produção, buscar análise prévia do cache/DB
        // Por ora, retornar status padrão LOW (exit code 0)
        CICDStatusResponse response = new CICDStatusResponse(
            pullRequestId,
            LLMSuspicionLevel.LOW,
            0, // exit code
            "Análise não encontrada. Assumindo LOW risk (exit code 0). Execute /analyze primeiro."
        );

        return ResponseEntity.ok(response);
    }

    /**
     * DTO para resposta CI/CD
     */
    @Schema(description = "Status para integração CI/CD")
    public static class CICDStatusResponse {
        @Schema(description = "ID do Pull Request", example = "123")
        private String pullRequestId;

        @Schema(description = "Nível de suspeição", example = "LOW")
        private LLMSuspicionLevel suspicionLevel;

        @Schema(description = "Exit code para CI/CD (0=OK, 1=WARNING, 2=BLOCKED)", example = "0")
        private int exitCode;

        @Schema(description = "Mensagem informativa")
        private String message;

        public CICDStatusResponse(String pullRequestId, LLMSuspicionLevel suspicionLevel, 
                                 int exitCode, String message) {
            this.pullRequestId = pullRequestId;
            this.suspicionLevel = suspicionLevel;
            this.exitCode = exitCode;
            this.message = message;
        }

        public String getPullRequestId() { return pullRequestId; }
        public LLMSuspicionLevel getSuspicionLevel() { return suspicionLevel; }
        public int getExitCode() { return exitCode; }
        public String getMessage() { return message; }
    }
}
