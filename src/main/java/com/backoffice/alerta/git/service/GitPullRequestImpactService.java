package com.backoffice.alerta.git.service;

import com.backoffice.alerta.ast.ASTCodeAnalysisService;
import com.backoffice.alerta.ast.ASTImpactDetail;
import com.backoffice.alerta.git.GitProvider;
import com.backoffice.alerta.git.client.DummyGitProviderClient;
import com.backoffice.alerta.git.client.GitHubProviderClient;
import com.backoffice.alerta.git.client.GitLabProviderClient;
import com.backoffice.alerta.git.client.GitProviderClient;
import com.backoffice.alerta.git.dto.*;
import com.backoffice.alerta.project.domain.Project;
import com.backoffice.alerta.project.dto.ProjectContext;
import com.backoffice.alerta.project.repository.ProjectRepository;
import com.backoffice.alerta.service.BusinessImpactAnalysisService;
import com.backoffice.alerta.dto.BusinessImpactRequest;
import com.backoffice.alerta.dto.BusinessImpactResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * US#51 + US#52 - Service para análise de impacto de Pull Requests
 * 
 * ⚠️ READ-ONLY TOTAL:
 * - Não persiste nada
 * - Não cria auditoria
 * - Não cria SLA
 * - Não envia notificações
 * 
 * US#52: Seleção dinâmica de providers:
 * - GitHub com token → GitHubProviderClient (REAL)
 * - GitLab com token → GitLabProviderClient (REAL)
 * - Sem token → DummyGitProviderClient (fallback)
 * 
 * Fluxo:
 * 1. Selecionar provider adequado
 * 2. Validar projectId (se presente)
 * 3. Buscar PR via GitProviderClient
 * 4. Extrair paths de arquivos alterados
 * 5. Montar BusinessImpactRequest
 * 6. Chamar BusinessImpactAnalysisService
 * 7. Retornar resposta consolidada
 */
@Service
public class GitPullRequestImpactService {

    private static final Logger log = LoggerFactory.getLogger(GitPullRequestImpactService.class);

    @Value("${git.github.token:}")
    private String githubToken;

    @Value("${git.gitlab.token:}")
    private String gitlabToken;

    private final DummyGitProviderClient dummyClient;
    private final GitHubProviderClient githubClient;
    private final GitLabProviderClient gitlabClient;
    private final BusinessImpactAnalysisService businessImpactService;
    private final ProjectRepository projectRepository;
    
    @Autowired(required = false)
    private ASTCodeAnalysisService astCodeAnalysisService;

    public GitPullRequestImpactService(
            DummyGitProviderClient dummyClient,
            GitHubProviderClient githubClient,
            GitLabProviderClient gitlabClient,
            BusinessImpactAnalysisService businessImpactService,
            ProjectRepository projectRepository) {
        this.dummyClient = dummyClient;
        this.githubClient = githubClient;
        this.gitlabClient = gitlabClient;
        this.businessImpactService = businessImpactService;
        this.projectRepository = projectRepository;
    }

    /**
     * US#52: Seleciona provider adequado baseado no tipo e disponibilidade de token
     * 
     * Lógica:
     * - GITHUB + token configurado → GitHubProviderClient (REAL)
     * - GITLAB + token configurado → GitLabProviderClient (REAL)
     * - Caso contrário → DummyGitProviderClient (fallback)
     */
    private GitProviderClient selectProvider(GitProvider provider) {
        if (provider == GitProvider.GITHUB && isTokenConfigured(githubToken)) {
            log.info("🔗 [US#52] Usando GitHubProviderClient REAL");
            return githubClient;
        }

        if (provider == GitProvider.GITLAB && isTokenConfigured(gitlabToken)) {
            log.info("🔗 [US#52] Usando GitLabProviderClient REAL");
            return gitlabClient;
        }

        log.info("🔄 [US#52] Fallback para DummyGitProviderClient (token não configurado)");
        return dummyClient;
    }

    /**
     * Verifica se token está configurado
     */
    private boolean isTokenConfigured(String token) {
        return token != null && !token.trim().isEmpty();
    }

    /**
     * Analisa impacto de um Pull Request
     * 
     * @param request dados do PR a analisar
     * @return análise consolidada com contexto de projeto (US#50)
     */
    public GitImpactAnalysisResponse analyzePullRequest(GitPullRequestRequest request) {
        log.info("🔍 Iniciando análise de PR #{} do repositório {} (Provider: {})", 
                request.getPullRequestNumber(), request.getRepositoryUrl(), request.getProvider());

        // US#52: Selecionar provider adequado
        GitProviderClient selectedClient = selectProvider(request.getProvider());

        // US#50: Validar projeto se especificado
        Project project = null;
        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Projeto não encontrado: " + request.getProjectId()));
            log.info("🔎 Análise escopada para Projeto: {} ({})", 
                    project.getName(), request.getProjectId());
        } else {
            log.info("🌐 Análise GLOBAL (sem escopo de projeto)");
        }

        // 1. Buscar metadados do PR (read-only)
        GitPullRequestData prData = selectedClient.fetchPullRequest(request);
        log.info("📄 PR {} obtido: {} arquivo(s) alterado(s)", 
                prData.getPullRequestId(), prData.getChangedFiles().size());

        // 2. Extrair paths de arquivos alterados
        List<String> changedFilePaths = prData.getChangedFiles().stream()
            .map(GitPullRequestFile::getFilePath)
            .collect(Collectors.toList());

        log.info("📊 Arquivos para análise: {}", changedFilePaths);

        // 3. Montar request para análise de impacto
        BusinessImpactRequest impactRequest = new BusinessImpactRequest();
        impactRequest.setPullRequestId(prData.getPullRequestId()); // ID do PR
        impactRequest.setChangedFiles(changedFilePaths);
        impactRequest.setProjectId(request.getProjectId()); // US#50

        // 4. Chamar serviço de análise de impacto (reutilização 100%)
        BusinessImpactResponse impactResponse = businessImpactService.analyze(impactRequest);

        log.info("✅ Análise concluída: {} regra(s) impactada(s)", 
                impactResponse.getImpactedBusinessRules().size());

        // 5. Construir resposta consolidada
        Map<String, Object> impactSummary = new HashMap<>();
        impactSummary.put("impactedRules", impactResponse.getImpactedBusinessRules());
        impactSummary.put("totalImpactedRules", impactResponse.getImpactedBusinessRules().size());
        impactSummary.put("changedFiles", prData.getChangedFiles());
        impactSummary.put("totalChangedFiles", prData.getChangedFiles().size());

        // Determinar risco e decisão (simplificado)
        String riskLevel = determineRiskLevel(impactResponse);
        String finalDecision = determineFinalDecision(riskLevel);

        // US#50: ProjectContext
        ProjectContext projectContext = project != null
            ? ProjectContext.scoped(project.getId(), project.getName())
            : ProjectContext.global();

        GitImpactAnalysisResponse response = new GitImpactAnalysisResponse(
            prData,
            projectContext,
            riskLevel,
            finalDecision,
            impactSummary
        );
        
        // US#69: Adicionar análise AST para arquivos Java do PR
        if (astCodeAnalysisService != null) {
            try {
                List<ASTImpactDetail> astDetails = performASTAnalysisOnPR(prData);
                response.setAstDetails(astDetails);
                log.info("🧩 [US#69] Análise AST do PR concluída | detalhes={}", astDetails.size());
            } catch (Exception e) {
                log.warn("⚠️ [US#69] Erro na análise AST do PR, continuando sem detalhes AST: {}", e.getMessage());
                response.setAstDetails(new ArrayList<>());
            }
        } else {
            response.setAstDetails(new ArrayList<>());
        }
        
        return response;
    }
    
    /**
     * US#69 - Realiza análise AST em arquivos Java do PR
     */
    private List<ASTImpactDetail> performASTAnalysisOnPR(GitPullRequestData prData) {
        // Filtrar apenas arquivos .java
        List<GitPullRequestFile> javaFiles = prData.getChangedFiles().stream()
            .filter(f -> f.getFilePath().endsWith(".java"))
            .collect(Collectors.toList());
        
        if (javaFiles.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Em produção, buscar conteúdo real dos arquivos via GitHub API
        // Por ora, usaremos o diff patch se disponível
        Map<String, String> javaFileContents = new HashMap<>();
        for (GitPullRequestFile file : javaFiles) {
            // TODO: Integrar com GitHub API para buscar conteúdo completo do arquivo
            // String content = githubClient.fetchFileContent(prData.getRepositoryUrl(), file.getFilePath(), prData.getSourceBranch());
            // javaFileContents.put(file.getFilePath(), content);
            log.debug("📄 [US#69] Arquivo Java no PR identificado: {}", file.getFilePath());
        }
        
        // Se não temos conteúdo real, retornar lista vazia
        if (javaFileContents.isEmpty()) {
            log.debug("ℹ️ [US#69] Nenhum conteúdo de arquivo disponível para análise AST no PR");
            return new ArrayList<>();
        }
        
        return astCodeAnalysisService.analyzeFiles(javaFileContents);
    }

    /**
     * Determina nível de risco baseado no impacto
     */
    private String determineRiskLevel(BusinessImpactResponse impactResponse) {
        int impactedRulesCount = impactResponse.getImpactedBusinessRules().size();

        if (impactedRulesCount == 0) {
            return "BAIXO";
        } else if (impactedRulesCount <= 2) {
            return "MEDIO";
        } else if (impactedRulesCount <= 5) {
            return "ALTO";
        } else {
            return "CRITICO";
        }
    }

    /**
     * Determina decisão final baseada no risco
     */
    private String determineFinalDecision(String riskLevel) {
        switch (riskLevel) {
            case "BAIXO":
                return "APROVADO";
            case "MEDIO":
                return "APROVADO_COM_OBSERVACOES";
            case "ALTO":
                return "APROVADO_COM_RESTRICOES";
            case "CRITICO":
                return "REQUER_REVISAO_EXECUTIVA";
            default:
                return "ANALISE_MANUAL_NECESSARIA";
        }
    }
}
