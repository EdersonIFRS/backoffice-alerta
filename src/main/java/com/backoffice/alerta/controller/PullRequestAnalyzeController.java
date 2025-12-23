package com.backoffice.alerta.controller;

import com.backoffice.alerta.adapter.PullRequestAdapter;
import com.backoffice.alerta.dto.PullRequestAnalyzeRequest;
import com.backoffice.alerta.dto.PullRequestRequest;
import com.backoffice.alerta.dto.RiskAnalysisResponse;
import com.backoffice.alerta.provider.PullRequestProvider;
import com.backoffice.alerta.provider.dto.PullRequestData;
import com.backoffice.alerta.service.RiskAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller para análise de risco baseada em Pull Request ID.
 * Busca dados externos via provider, converte através do adapter e reutiliza análise de risco.
 */
@RestController
@RequestMapping("/risk")
@Tag(name = "Análise de Risco", description = "Análise de risco de Pull Requests")
public class PullRequestAnalyzeController {

    private final PullRequestProvider mockProvider;
    private final PullRequestProvider gitHubProvider;
    private final PullRequestAdapter adapter;
    private final RiskAnalysisService riskAnalysisService;

    public PullRequestAnalyzeController(@Qualifier("mockPullRequestProvider") PullRequestProvider mockProvider,
                                       @Qualifier("gitHubPullRequestProvider") PullRequestProvider gitHubProvider,
                                       PullRequestAdapter adapter,
                                       RiskAnalysisService riskAnalysisService) {
        this.mockProvider = mockProvider;
        this.gitHubProvider = gitHubProvider;
        this.adapter = adapter;
        this.riskAnalysisService = riskAnalysisService;
    }

    @PostMapping("/analyze/pr")
    @Operation(
        summary = "Analisa risco de Pull Request por ID (Mock)",
        description = "Executa análise completa de risco a partir de um pullRequestId mockado, " +
                     "sem necessidade de envio manual de arquivos. Dados são buscados de provider simulado " +
                     "e convertidos automaticamente para o modelo interno. " +
                     "Usa as mesmas regras de risco dos demais endpoints."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Análise de risco executada com sucesso"
    )
    public ResponseEntity<RiskAnalysisResponse> analyzeByPullRequestId(@RequestBody PullRequestAnalyzeRequest request) {
        return executeAnalysis(mockProvider, request);
    }

    @PostMapping("/analyze/pr/github")
    @Operation(
        summary = "Analisa risco de Pull Request do GitHub",
        description = "Busca Pull Request real do GitHub via API e executa análise completa de risco. " +
                     "Requer configuração de GITHUB_TOKEN e GITHUB_REPOSITORY. " +
                     "Converte automaticamente dados do GitHub para modelo interno e aplica todas as regras de risco."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Análise executada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Pull Request não encontrado"),
        @ApiResponse(responseCode = "401", description = "Token GitHub inválido ou não configurado"),
        @ApiResponse(responseCode = "403", description = "Rate limit excedido ou acesso negado"),
        @ApiResponse(responseCode = "500", description = "Erro ao conectar com GitHub API")
    })
    public ResponseEntity<RiskAnalysisResponse> analyzeGitHubPullRequest(@RequestBody PullRequestAnalyzeRequest request) {
        return executeAnalysis(gitHubProvider, request);
    }

    private ResponseEntity<RiskAnalysisResponse> executeAnalysis(PullRequestProvider provider, 
                                                                 PullRequestAnalyzeRequest request) {
        // 1. Buscar dados externos via provider
        PullRequestData externalData = provider.fetch(request.getPullRequestId());

        // 2. Converter para modelo interno via adapter
        List<PullRequestRequest.FileChange> internalFiles = adapter.convertToInternalModel(externalData);

        // 3. Reutilizar serviço de análise existente
        PullRequestRequest analysisRequest = new PullRequestRequest(
            request.getPullRequestId(),
            internalFiles,
            request.getRuleVersion()
        );
        RiskAnalysisResponse response = riskAnalysisService.analyzeRisk(analysisRequest);

        return ResponseEntity.ok(response);
    }
}
