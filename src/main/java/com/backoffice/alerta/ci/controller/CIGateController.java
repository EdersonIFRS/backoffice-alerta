package com.backoffice.alerta.ci.controller;

import com.backoffice.alerta.ci.dto.CIGateRequest;
import com.backoffice.alerta.ci.dto.CIGateResponse;
import com.backoffice.alerta.ci.service.CIGateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * US#53 - Controller para CI/CD Gate de Risco
 * 
 * Endpoint usado por pipelines de CI/CD (GitHub Actions, GitLab CI)
 * para validar Pull Requests/Merge Requests antes do merge.
 * 
 * ⚠️ READ-ONLY:
 * - Não persiste dados
 * - Não cria auditoria
 * - Não envia notificações
 * - Apenas decisão consultiva
 * 
 * 🔐 Acesso: ADMIN, RISK_MANAGER, ENGINEER
 */
@RestController
@RequestMapping("/risk/ci")
@Tag(name = "CI/CD Gate", description = "Gate de risco para pipelines CI/CD (US#53)")
public class CIGateController {

    private final CIGateService ciGateService;

    public CIGateController(CIGateService ciGateService) {
        this.ciGateService = ciGateService;
    }

    @PostMapping("/gate")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER', 'ENGINEER')")
    @Operation(
        summary = "Validar PR/MR como gate de risco em pipeline CI/CD",
        description = """
            Analisa Pull Request ou Merge Request e retorna decisão para gate de pipeline.
            
            **⚠️ READ-ONLY:**
            - Não persiste dados
            - Não cria auditoria
            - Não envia notificações
            - Apenas análise consultiva
            
            **🔐 Acesso:** ADMIN, RISK_MANAGER, ENGINEER
            
            **📊 Exit Codes para CI/CD:**
            - `0`: APROVADO - Pipeline pode continuar
            - `1`: APROVADO_COM_RESTRICOES - Warning, mas pipeline continua
            - `2`: BLOQUEADO - Pipeline deve falhar
            
            **🔗 Reutiliza serviços (US#51/52):**
            - GitPullRequestImpactService
            - BusinessImpactAnalysisService
            
            **🌍 Modos (US#50):**
            - GLOBAL: Sem projectId (todas as regras)
            - SCOPED: Com projectId (apenas regras do projeto)
            
            **🔄 Fallback automático:**
            - Se provider indisponível → exitCode=1, decision=APROVADO_COM_RESTRICOES
            
            **🎯 Casos de uso:**
            - GitHub Actions gate
            - GitLab CI gate
            - Validação automatizada pré-merge
            - Política organizacional de risco
            """,
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Análise concluída (sucesso ou fallback)",
                content = @Content(
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "GitHub + SCOPED (Aprovado)",
                            description = "PR aprovado em projeto específico",
                            value = """
                                {
                                  "provider": "GITHUB",
                                  "repositoryUrl": "https://github.com/acme/backoffice",
                                  "pullRequestNumber": "123",
                                  "projectId": "550e8400-e29b-41d4-a716-446655440010",
                                  "environment": "PRODUCTION",
                                  "changeType": "FEATURE"
                                }
                                """
                        ),
                        @ExampleObject(
                            name = "GitLab + GLOBAL (Warning)",
                            description = "MR com restrições, modo global",
                            value = """
                                {
                                  "provider": "GITLAB",
                                  "repositoryUrl": "https://gitlab.com/acme/portal",
                                  "pullRequestNumber": "42",
                                  "environment": "STAGING",
                                  "changeType": "REFACTOR"
                                }
                                """
                        ),
                        @ExampleObject(
                            name = "Fallback (Provider indisponível)",
                            description = "Token não configurado, fallback automático",
                            value = """
                                {
                                  "provider": "GITHUB",
                                  "repositoryUrl": "https://github.com/acme/backoffice",
                                  "pullRequestNumber": "999",
                                  "environment": "PRODUCTION",
                                  "changeType": "HOTFIX"
                                }
                                """
                        )
                    }
                )
            ),
            @ApiResponse(responseCode = "400", description = "Request inválido"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
        }
    )
    public ResponseEntity<CIGateResponse> analyzeGate(@Valid @RequestBody CIGateRequest request) {
        CIGateResponse response = ciGateService.analyzeGate(request);
        return ResponseEntity.ok(response);
    }
}
