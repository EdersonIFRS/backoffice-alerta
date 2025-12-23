package com.backoffice.alerta.ci.service;

import com.backoffice.alerta.ci.dto.CIGateRequest;
import com.backoffice.alerta.ci.dto.CIGateResponse;
import com.backoffice.alerta.dto.BusinessImpactRequest;
import com.backoffice.alerta.dto.BusinessImpactResponse;
import com.backoffice.alerta.dto.RiskDecisionRequest;
import com.backoffice.alerta.dto.RiskDecisionResponse;
import com.backoffice.alerta.git.dto.GitImpactAnalysisResponse;
import com.backoffice.alerta.git.dto.GitPullRequestRequest;
import com.backoffice.alerta.git.service.GitPullRequestImpactService;
import com.backoffice.alerta.project.domain.Project;
import com.backoffice.alerta.project.dto.ProjectContext;
import com.backoffice.alerta.project.repository.ProjectRepository;
import com.backoffice.alerta.rules.FinalDecision;
import com.backoffice.alerta.service.BusinessImpactAnalysisService;
import com.backoffice.alerta.service.RiskDecisionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * US#53 - Service para CI/CD Gate de Risco
 * 
 * ⚠️ READ-ONLY ABSOLUTO:
 * - NÃO cria auditorias
 * - NÃO cria SLAs
 * - NÃO envia notificações
 * - NÃO comenta em PR/MR
 * - NÃO faz commit ou merge
 * 
 * Reutiliza serviços existentes:
 * - GitPullRequestImpactService (US#51/52)
 * - BusinessImpactAnalysisService
 * - RiskDecisionService
 * 
 * Fluxo:
 * 1. Resolve ProjectContext (GLOBAL ou SCOPED)
 * 2. Chama GitPullRequestImpactService para buscar PR
 * 3. Chama BusinessImpactAnalysisService para análise
 * 4. Mapeia decisão para exitCode
 * 5. Retorna CIGateResponse
 */
@Service
public class CIGateService {

    private static final Logger log = LoggerFactory.getLogger(CIGateService.class);

    private final GitPullRequestImpactService gitService;
    private final BusinessImpactAnalysisService impactService;
    private final ProjectRepository projectRepository;

    public CIGateService(GitPullRequestImpactService gitService,
                        BusinessImpactAnalysisService impactService,
                        ProjectRepository projectRepository) {
        this.gitService = gitService;
        this.impactService = impactService;
        this.projectRepository = projectRepository;
    }

    /**
     * Analisa PR/MR e retorna decisão de gate
     * 
     * @param request dados do CI/CD
     * @return response com exitCode para pipeline
     */
    public CIGateResponse analyzeGate(CIGateRequest request) {
        log.info("🔁 CI GATE | provider={} | repo={} | pr={} | env={} | changeType={}", 
                request.getProvider(), 
                request.getRepositoryUrl(), 
                request.getPullRequestNumber(),
                request.getEnvironment(),
                request.getChangeType());

        try {
            // 1. Resolver ProjectContext (US#50)
            ProjectContext projectContext = resolveProjectContext(request);

            // 2. Buscar dados do PR via GitPullRequestImpactService
            GitPullRequestRequest gitRequest = new GitPullRequestRequest();
            gitRequest.setProvider(request.getProvider());
            gitRequest.setRepositoryUrl(request.getRepositoryUrl());
            gitRequest.setPullRequestNumber(request.getPullRequestNumber());
            gitRequest.setProjectId(request.getProjectId());

            GitImpactAnalysisResponse gitAnalysis = gitService.analyzePullRequest(gitRequest);

            // 3. Extrair arquivos alterados
            List<String> changedFiles = request.getChangedFiles();
            if (changedFiles == null || changedFiles.isEmpty()) {
                changedFiles = gitAnalysis.getPullRequest().getChangedFiles().stream()
                        .map(f -> f.getFilePath())
                        .collect(Collectors.toList());
            }

            // 4. Análise de impacto de negócio
            BusinessImpactRequest impactRequest = new BusinessImpactRequest();
            impactRequest.setPullRequestId("CI-" + request.getPullRequestNumber());
            impactRequest.setChangedFiles(changedFiles);
            impactRequest.setProjectId(request.getProjectId());

            BusinessImpactResponse impactResponse = impactService.analyze(impactRequest);

            // 5. Mapear para decisão final e exitCode
            String riskLevel = gitAnalysis.getRiskLevel();
            String finalDecision = gitAnalysis.getFinalDecision();
            int exitCode = mapDecisionToExitCode(finalDecision);

            // 6. Gerar summary
            int impactedRules = impactResponse.getImpactedBusinessRules().size();
            String summary = generateSummary(finalDecision, riskLevel, impactedRules);

            // 7. Gerar reasonCodes
            List<String> reasonCodes = generateReasonCodes(finalDecision, riskLevel, impactedRules, request);

            // 8. Gerar actionsRequired
            List<String> actionsRequired = generateActionsRequired(finalDecision, riskLevel);

            // 9. Construir response
            CIGateResponse response = new CIGateResponse(
                    finalDecision,
                    riskLevel,
                    exitCode,
                    summary,
                    projectContext
            );

            response.setReasonCodes(reasonCodes);
            response.setActionsRequired(actionsRequired);
            response.setProvider(request.getProvider());
            response.setPullRequestNumber(request.getPullRequestNumber());
            response.setRepositoryUrl(request.getRepositoryUrl());

            log.info("✅ CI GATE | decision={} | risk={} | exitCode={} | impactedRules={}", 
                    finalDecision, riskLevel, exitCode, impactedRules);

            return response;

        } catch (IllegalStateException e) {
            // Provider indisponível (token não configurado ou erro de conexão)
            log.warn("⚠️ CI GATE | Provider indisponível: {}", e.getMessage());
            return buildFallbackResponse(request, "CI_PROVIDER_UNAVAILABLE");

        } catch (IllegalArgumentException e) {
            // Projeto ou PR não encontrado
            log.warn("⚠️ CI GATE | Recurso não encontrado: {}", e.getMessage());
            return buildFallbackResponse(request, "RESOURCE_NOT_FOUND");

        } catch (Exception e) {
            // Erro genérico
            log.error("❌ CI GATE | Erro inesperado: {}", e.getMessage(), e);
            return buildFallbackResponse(request, "ANALYSIS_ERROR");
        }
    }

    /**
     * Resolve contexto de projeto (GLOBAL ou SCOPED)
     */
    private ProjectContext resolveProjectContext(CIGateRequest request) {
        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Projeto não encontrado: " + request.getProjectId()));
            
            log.info("🔎 CI GATE | Modo SCOPED | Projeto: {} ({})", 
                    project.getName(), request.getProjectId());
            
            return ProjectContext.scoped(project.getId(), project.getName());
        }

        log.info("🌐 CI GATE | Modo GLOBAL");
        return ProjectContext.global();
    }

    /**
     * Mapeia decisão final para exit code do CI/CD
     * 
     * 0 = APROVADO (pipeline continua)
     * 1 = APROVADO_COM_RESTRICOES (warning, mas pipeline continua)
     * 2 = BLOQUEADO (pipeline deve falhar)
     */
    private int mapDecisionToExitCode(String finalDecision) {
        if (finalDecision == null) {
            return 1; // Default: warning
        }

        switch (finalDecision.toUpperCase()) {
            case "APROVADO":
                return 0;
            case "APROVADO_COM_OBSERVACOES":
            case "APROVADO_COM_RESTRICOES":
            case "REQUER_REVISAO_EXECUTIVA":
                return 1;
            case "BLOQUEADO":
                return 2;
            default:
                return 1;
        }
    }

    /**
     * Gera resumo textual da análise
     */
    private String generateSummary(String decision, String riskLevel, int impactedRules) {
        StringBuilder summary = new StringBuilder();

        if ("APROVADO".equals(decision)) {
            summary.append("✅ PR aprovado. ");
        } else if ("BLOQUEADO".equals(decision)) {
            summary.append("❌ PR bloqueado. ");
        } else {
            summary.append("⚠️ PR aprovado com restrições. ");
        }

        if (impactedRules > 0) {
            summary.append(String.format("%d regra(s) de negócio impactada(s). ", impactedRules));
        } else {
            summary.append("Nenhuma regra crítica impactada. ");
        }

        summary.append(String.format("Risco: %s.", riskLevel));

        return summary.toString();
    }

    /**
     * Gera códigos de razão padronizados
     */
    private List<String> generateReasonCodes(String decision, String riskLevel, 
                                            int impactedRules, CIGateRequest request) {
        List<String> codes = new ArrayList<>();

        // Decisão
        codes.add("DECISION_" + decision.toUpperCase().replace(" ", "_"));

        // Risco
        codes.add("RISK_LEVEL_" + riskLevel.toUpperCase());

        // Impacto
        if (impactedRules > 0) {
            codes.add("BUSINESS_RULES_IMPACTED");
        } else {
            codes.add("NO_CRITICAL_IMPACT");
        }

        // Ambiente
        codes.add("ENV_" + request.getEnvironment().name());

        // Tipo de mudança
        codes.add("CHANGE_TYPE_" + request.getChangeType().name());

        return codes;
    }

    /**
     * Gera ações requeridas baseadas na decisão
     */
    private List<String> generateActionsRequired(String decision, String riskLevel) {
        List<String> actions = new ArrayList<>();

        if ("BLOQUEADO".equals(decision)) {
            actions.add("Revisar mudanças com time de engenharia");
            actions.add("Reduzir impacto em regras críticas");
            actions.add("Obter aprovação executiva antes do merge");
        } else if ("APROVADO_COM_RESTRICOES".equals(decision) || 
                   "REQUER_REVISAO_EXECUTIVA".equals(decision)) {
            actions.add("Garantir cobertura de testes");
            actions.add("Validar com owners das regras impactadas");
            
            if ("ALTO".equals(riskLevel) || "CRITICO".equals(riskLevel)) {
                actions.add("Monitorar deploy em tempo real");
            }
        }

        return actions;
    }

    /**
     * Constrói response de fallback quando provider está indisponível
     */
    private CIGateResponse buildFallbackResponse(CIGateRequest request, String reasonCode) {
        ProjectContext projectContext = request.getProjectId() != null
                ? ProjectContext.scoped(request.getProjectId(), "Unknown")
                : ProjectContext.global();

        CIGateResponse response = new CIGateResponse(
                "APROVADO_COM_RESTRICOES",
                "DESCONHECIDO",
                1, // Warning
                "⚠️ Análise não pôde ser concluída. Provider indisponível ou erro de configuração.",
                projectContext
        );

        List<String> reasonCodes = new ArrayList<>();
        reasonCodes.add(reasonCode);
        reasonCodes.add("FALLBACK_MODE");
        response.setReasonCodes(reasonCodes);

        List<String> actions = new ArrayList<>();
        actions.add("Verificar configuração de tokens (GITHUB_TOKEN ou GITLAB_TOKEN)");
        actions.add("Validar acesso ao repositório");
        actions.add("Revisar manualmente o Pull Request");
        response.setActionsRequired(actions);

        response.setProvider(request.getProvider());
        response.setPullRequestNumber(request.getPullRequestNumber());
        response.setRepositoryUrl(request.getRepositoryUrl());

        log.info("🔄 CI GATE | Fallback mode | exitCode=1 | reason={}", reasonCode);

        return response;
    }
}
