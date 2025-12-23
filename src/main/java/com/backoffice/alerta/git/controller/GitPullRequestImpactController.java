package com.backoffice.alerta.git.controller;

import com.backoffice.alerta.git.dto.GitImpactAnalysisResponse;
import com.backoffice.alerta.git.dto.GitPullRequestRequest;
import com.backoffice.alerta.git.service.GitPullRequestImpactService;
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
 * US#51 + US#52 - Controller para análise de impacto de Pull Requests
 * 
 * ⚠️ READ-ONLY TOTAL:
 * - Não persiste dados
 * - Não altera código
 * - Apenas analisa impacto
 * 
 * 🔐 Acesso: ADMIN, RISK_MANAGER, ENGINEER
 * 
 * US#52: Suporta integração REAL com GitHub e GitLab
 */
@RestController
@RequestMapping("/risk/git")
@Tag(name = "Git PR Analysis", description = "Análise de impacto de Pull Requests (Read-Only) - US#51 + US#52")
public class GitPullRequestImpactController {

    private final GitPullRequestImpactService gitPullRequestImpactService;

    public GitPullRequestImpactController(GitPullRequestImpactService gitPullRequestImpactService) {
        this.gitPullRequestImpactService = gitPullRequestImpactService;
    }

    @PostMapping("/pull-request/analyze")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER', 'ENGINEER')")
    @Operation(
        summary = "Analisar impacto de Pull Request",
        description = """
            Analisa o impacto de negócio de um Pull Request baseado nos arquivos alterados.
            
            **⚠️ READ-ONLY:**
            - Não clona repositório
            - Não altera código
            - Não persiste dados
            - Não cria auditoria
            - Apenas analisa metadados
            
            **🔐 Acesso:** ADMIN, RISK_MANAGER, ENGINEER
            
            **📊 Análise:**
            - Identifica regras de negócio impactadas
            - Calcula nível de risco
            - Sugere decisão
            - Respeita contexto de projeto (US#50)
            
            **🔗 US#52 - Integração REAL:**
            - GitHub: Com token configurado → busca PR real via API
            - GitLab: Com token configurado → busca MR real via API
            - Sem token: Fallback automático para dados simulados (DummyClient)
            
            **🔑 Configuração:**
            - GITHUB_TOKEN: Variável de ambiente para GitHub
            - GITLAB_TOKEN: Variável de ambiente para GitLab
            
            **🎯 Casos de uso:**
            - Análise automatizada de PRs reais
            - Identificação de impacto antes do merge
            - Suporte a decisões de release
            - Consultoria sem escrita em Git
            """,
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Análise concluída com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "PR Real GitHub (US#52)",
                            description = "Pull Request REAL do GitHub (requer GITHUB_TOKEN)",
                            value = """
                                {
                                  "provider": "GITHUB",
                                  "repositoryUrl": "https://github.com/company/payment-backoffice",
                                  "pullRequestNumber": "123"
                                }
                                """
                        ),
                        @ExampleObject(
                            name = "MR Real GitLab (US#52)",
                            description = "Merge Request REAL do GitLab (requer GITLAB_TOKEN)",
                            value = """
                                {
                                  "provider": "GITLAB",
                                  "repositoryUrl": "https://gitlab.com/company/customer-portal",
                                  "pullRequestNumber": "456"
                                }
                                """
                        ),
                        @ExampleObject(
                            name = "PR Simulado (Fallback)",
                            description = "Dados simulados quando token não configurado",
                            value = """
                                {
                                  "provider": "GITHUB",
                                  "repositoryUrl": "https://github.com/demo/example",
                                  "pullRequestNumber": "999"
                                }
                                """
                        ),
                        @ExampleObject(
                            name = "PR com Projeto (Scoped - US#50)",
                            description = "Pull Request escopado a um projeto específico",
                            value = """
                                {
                                  "provider": "GITHUB",
                                  "repositoryUrl": "https://github.com/company/payment-backoffice",
                                  "pullRequestNumber": "789",
                                  "projectId": "b394f1c1-4a51-42ca-89e4-14353eaa37e1"
                                }
                                """
                        )
                    }
                )
            ),
            @ApiResponse(responseCode = "400", description = "Request inválido"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
        }
    )
    public ResponseEntity<GitImpactAnalysisResponse> analyzePullRequest(
            @Valid @RequestBody GitPullRequestRequest request) {
        
        GitImpactAnalysisResponse response = gitPullRequestImpactService.analyzePullRequest(request);
        return ResponseEntity.ok(response);
    }
}
