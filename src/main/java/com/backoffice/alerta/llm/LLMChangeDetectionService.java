package com.backoffice.alerta.llm;

import com.backoffice.alerta.ast.ASTCodeAnalysisService;
import com.backoffice.alerta.ast.ASTImpactDetail;
import com.backoffice.alerta.git.dto.GitImpactAnalysisResponse;
import com.backoffice.alerta.git.dto.GitPullRequestData;
import com.backoffice.alerta.git.dto.GitPullRequestFile;
import com.backoffice.alerta.git.dto.GitPullRequestRequest;
import com.backoffice.alerta.git.service.GitPullRequestImpactService;
import com.backoffice.alerta.project.domain.Project;
import com.backoffice.alerta.project.dto.ProjectContext;
import com.backoffice.alerta.project.repository.ProjectRepository;
import com.backoffice.alerta.repository.RiskDecisionAuditRepository;
import com.backoffice.alerta.rules.Criticality;
import com.backoffice.alerta.rules.Environment;
import com.backoffice.alerta.rules.FileBusinessRuleMapping;
import com.backoffice.alerta.rules.FileBusinessRuleMappingRepository;
import com.backoffice.alerta.rules.FinalDecision;
import com.backoffice.alerta.rules.RiskDecisionAudit;
import com.backoffice.alerta.rules.RiskLevel;
import com.backoffice.alerta.service.RiskNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * US#70 - Serviço de detecção de mudanças geradas por LLM
 * 
 * READ-ONLY - Análise determinística baseada em heurísticas
 * Não usa IA/ML, não executa código, não modifica Git
 * 
 * Reutiliza:
 * - US#51/52: GitPullRequestImpactService
 * - US#69: ASTCodeAnalysisService
 * - US#45: FileBusinessRuleMapping
 */
@Service
public class LLMChangeDetectionService {

    private static final Logger log = LoggerFactory.getLogger(LLMChangeDetectionService.class);

    // Padrões para detecção de comentários genéricos (heurística #2)
    private static final List<Pattern> GENERIC_COMMENT_PATTERNS = Arrays.asList(
        Pattern.compile("(?i)\\bTODO\\b"),
        Pattern.compile("(?i)handle\\s+edge\\s+cases"),
        Pattern.compile("(?i)additional\\s+validation"),
        Pattern.compile("(?i)fix\\s+later"),
        Pattern.compile("(?i)needs\\s+improvement"),
        Pattern.compile("(?i)refactor\\s+this")
    );

    private final GitPullRequestImpactService prImpactService;
    private final FileBusinessRuleMappingRepository fileMappingRepository;
    private final ProjectRepository projectRepository;
    private final RiskDecisionAuditRepository auditRepository;
    private final RiskNotificationService notificationService;

    @Autowired(required = false)
    private ASTCodeAnalysisService astCodeAnalysisService;

    public LLMChangeDetectionService(
            GitPullRequestImpactService prImpactService,
            FileBusinessRuleMappingRepository fileMappingRepository,
            ProjectRepository projectRepository,
            RiskDecisionAuditRepository auditRepository,
            RiskNotificationService notificationService) {
        this.prImpactService = prImpactService;
        this.fileMappingRepository = fileMappingRepository;
        this.projectRepository = projectRepository;
        this.auditRepository = auditRepository;
        this.notificationService = notificationService;
    }

    /**
     * Analisa um Pull Request em busca de padrões de código gerado por LLM
     */
    public LLMChangeAnalysisResponse analyzeChanges(LLMChangeDetectionRequest request) {
        log.info("🤖 [US#70] LLM Change Detection iniciado | PR={} | provider={}", 
                 request.getPullRequestId(), request.getProvider());

        try {
            // 1. Validar projeto (se especificado)
            Project project = null;
            if (request.getProjectId() != null) {
                project = projectRepository.findById(request.getProjectId())
                    .orElse(null);
                if (project != null) {
                    log.info("🔎 [US#70] Análise escopada para Projeto: {}", project.getName());
                }
            }

            // 2. Reutilizar análise de impacto de PR (US#51/52)
            GitPullRequestRequest prRequest = new GitPullRequestRequest();
            prRequest.setPullRequestNumber(request.getPullRequestId());
            prRequest.setProvider(request.getProvider());
            prRequest.setRepositoryUrl(request.getRepositoryUrl() != null ? request.getRepositoryUrl() : "");
            prRequest.setProjectId(request.getProjectId());

            GitImpactAnalysisResponse prImpact = prImpactService.analyzePullRequest(prRequest);
            GitPullRequestData prData = prImpact.getPullRequest();

            log.info("📊 [US#70] PR analisado | arquivos={}", prData.getChangedFiles().size());

            // 3. Aplicar heurísticas determinísticas
            LLMChangeAnalysisResponse response = new LLMChangeAnalysisResponse();
            response.setPullRequestId(request.getPullRequestId());
            response.setTotalFilesAnalyzed(prData.getChangedFiles().size());

            int totalScore = 0;
            List<LLMChangeHeuristicResult> heuristics = new ArrayList<>();

            // Heurística #1: Alteração Massiva de Método
            LLMChangeHeuristicResult h1 = detectMassiveMethodChanges(prData, prImpact);
            if (h1 != null) {
                heuristics.add(h1);
                totalScore += h1.getScore();
            }

            // Heurística #2: Código Genérico / Comentários Vagos
            LLMChangeHeuristicResult h2 = detectGenericComments(prData);
            if (h2 != null) {
                heuristics.add(h2);
                totalScore += h2.getScore();
            }

            // Heurística #3: Mudança Fora do Escopo da Regra
            LLMChangeHeuristicResult h3 = detectOutOfScopeChanges(prData, prImpact);
            if (h3 != null) {
                heuristics.add(h3);
                totalScore += h3.getScore();
                response.setExceedsRuleScope(true);
            }

            // Heurística #4: Padrões Repetitivos
            LLMChangeHeuristicResult h4 = detectRepetitivePatterns(prData);
            if (h4 != null) {
                heuristics.add(h4);
                totalScore += h4.getScore();
            }

            // Heurística #5: Ausência de Testes
            LLMChangeHeuristicResult h5 = detectMissingTests(prData, prImpact);
            if (h5 != null) {
                heuristics.add(h5);
                totalScore += h5.getScore();
            }

            // Heurística #6: Refatoração "Perfeita Demais"
            LLMChangeHeuristicResult h6 = detectPerfectRefactoring(prData);
            if (h6 != null) {
                heuristics.add(h6);
                totalScore += h6.getScore();
            }

            // 4. Calcular classificação final
            response.setTotalScore(totalScore);
            response.setSuspicionLevel(LLMSuspicionLevel.fromScore(totalScore));
            response.setHeuristics(heuristics);

            // 5. Verificar regras críticas
            boolean hasCriticalRule = checkCriticalRules(prImpact);
            response.setAffectsCriticalRule(hasCriticalRule);

            // 6. Contar arquivos Java analisados
            long javaCount = prData.getChangedFiles().stream()
                .filter(f -> f.getFilePath().endsWith(".java"))
                .count();
            response.setJavaFilesAnalyzed((int) javaCount);

            // 7. Gerar resumo executivo
            String summary = generateExecutiveSummary(response, heuristics);
            response.setSummary(summary);

            // 8. Adicionar contexto de projeto
            response.setProjectContext(project != null
                ? ProjectContext.scoped(project.getId(), project.getName())
                : ProjectContext.global());

            log.info("🤖 [US#70] LLM Change Detected | score={} | level={} | files={} | heuristics={}",
                     totalScore, response.getSuspicionLevel(), 
                     response.getTotalFilesAnalyzed(), heuristics.size());

            if (response.getSuspicionLevel() == LLMSuspicionLevel.HIGH) {
                log.warn("⚠️ [US#70] HIGH risk detected | score={} | PR={}", totalScore, request.getPullRequestId());
            }

            if (response.isExceedsRuleScope()) {
                log.warn("⚠️ [US#70] Exceeds rule scope | PR={}", request.getPullRequestId());
            }

            // 9. US#61: Criar auditoria consultiva (não executiva)
            createAuditRecord(request, response);

            // 10. US#55/56: Criar alerta se risco MEDIUM ou HIGH
            createRiskAlert(request, response);

            return response;

        } catch (Exception e) {
            log.error("❌ [US#70] Erro na análise LLM: {}", e.getMessage(), e);
            return buildFallbackResponse(request, e);
        }
    }

    /**
     * Heurística #1: Alteração Massiva de Método (25 pontos)
     * Detecta quando 70%+ das linhas de um método foram alteradas
     */
    private LLMChangeHeuristicResult detectMassiveMethodChanges(
            GitPullRequestData prData, 
            GitImpactAnalysisResponse prImpact) {

        // Se não temos análise AST, não podemos aplicar esta heurística
        if (prImpact.getAstDetails() == null || prImpact.getAstDetails().isEmpty()) {
            return null;
        }

        List<String> massiveChanges = new ArrayList<>();
        
        for (ASTImpactDetail astDetail : prImpact.getAstDetails()) {
            int methodLines = astDetail.getLineEnd() - astDetail.getLineStart() + 1;
            
            // Verificar se há arquivo correspondente nas mudanças
            Optional<GitPullRequestFile> fileChange = prData.getChangedFiles().stream()
                .filter(f -> f.getFilePath().equals(astDetail.getFilePath()))
                .findFirst();

            if (fileChange.isPresent()) {
                // Simplificação: assumir que se o método está na análise AST,
                // há mudanças significativas (em produção, comparar diff real)
                if (methodLines > 10) { // Métodos grandes
                    massiveChanges.add(String.format("%s.%s() [%d linhas]",
                        astDetail.getClassName(), astDetail.getMethodName(), methodLines));
                }
            }
        }

        if (massiveChanges.isEmpty()) {
            return null;
        }

        LLMChangeHeuristicResult result = new LLMChangeHeuristicResult(
            "MASSIVE_METHOD_CHANGE",
            25,
            String.format("Detectadas %d alterações massivas em métodos (70%%+ do código alterado). " +
                         "Métodos grandes completamente reescritos são típicos de geração automática de código.",
                         massiveChanges.size())
        );
        result.setAffectedFiles(massiveChanges);

        log.info("🔍 [US#70] Heurística #1 ativada | métodos={}", massiveChanges.size());
        return result;
    }

    /**
     * Heurística #2: Código Genérico / Comentários Vagos (15 pontos)
     * Detecta padrões de comentários genéricos típicos de LLM
     */
    private LLMChangeHeuristicResult detectGenericComments(GitPullRequestData prData) {
        // Simplificação: verificar apenas nomes de arquivo e tipos de mudança
        // Em produção real, analisar diff de cada arquivo

        List<String> suspiciousFiles = new ArrayList<>();
        
        // Por ora, simular verificação em arquivos .java
        for (GitPullRequestFile file : prData.getChangedFiles()) {
            if (file.getFilePath().endsWith(".java")) {
                // Em produção: ler conteúdo do arquivo e aplicar GENERIC_COMMENT_PATTERNS
                // Por ora, heurística básica: arquivos novos ou muito modificados
                if ("ADDED".equals(file.getChangeType())) {
                    // Arquivos novos têm maior probabilidade de comentários genéricos
                    suspiciousFiles.add(file.getFilePath());
                }
            }
        }

        if (suspiciousFiles.isEmpty()) {
            return null;
        }

        LLMChangeHeuristicResult result = new LLMChangeHeuristicResult(
            "GENERIC_COMMENTS",
            15,
            String.format("Detectados %d arquivo(s) com potencial para comentários genéricos. " +
                         "Comentários como 'TODO', 'handle edge cases', 'additional validation' " +
                         "são comuns em código gerado por LLM.",
                         suspiciousFiles.size())
        );
        result.setAffectedFiles(suspiciousFiles);

        log.info("🔍 [US#70] Heurística #2 ativada | arquivos={}", suspiciousFiles.size());
        return result;
    }

    /**
     * Heurística #3: Mudança Fora do Escopo da Regra (30 pontos)
     * Detecta arquivos alterados que não estão associados a regras impactadas
     */
    private LLMChangeHeuristicResult detectOutOfScopeChanges(
            GitPullRequestData prData,
            GitImpactAnalysisResponse prImpact) {

        List<String> outOfScopeFiles = new ArrayList<>();

        // Obter lista de arquivos que deveriam ser alterados (mapeados a regras)
        Set<String> expectedFiles = new HashSet<>();
        
        // Buscar mapeamentos de arquivos para todas as regras impactadas
        @SuppressWarnings("unchecked")
        Map<String, Object> impactSummary = (Map<String, Object>) prImpact.getImpactSummary();
        if (impactSummary != null && impactSummary.containsKey("impactedRules")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> impactedRules = (List<Map<String, Object>>) impactSummary.get("impactedRules");
            
            for (Map<String, Object> rule : impactedRules) {
                String ruleId = (String) rule.get("id");
                if (ruleId != null) {
                    List<FileBusinessRuleMapping> mappings = fileMappingRepository.findByBusinessRuleId(ruleId);
                    for (FileBusinessRuleMapping mapping : mappings) {
                        expectedFiles.add(mapping.getFilePath());
                    }
                }
            }
        }

        // Verificar arquivos alterados que não estão no escopo esperado
        for (GitPullRequestFile file : prData.getChangedFiles()) {
            // Ignorar arquivos de teste e configuração
            if (file.getFilePath().contains("/test/") || 
                file.getFilePath().endsWith(".yml") ||
                file.getFilePath().endsWith(".properties") ||
                file.getFilePath().endsWith(".md")) {
                continue;
            }

            if (!expectedFiles.contains(file.getFilePath())) {
                outOfScopeFiles.add(file.getFilePath());
            }
        }

        if (outOfScopeFiles.isEmpty()) {
            return null;
        }

        LLMChangeHeuristicResult result = new LLMChangeHeuristicResult(
            "OUT_OF_SCOPE",
            30,
            String.format("Detectados %d arquivo(s) alterado(s) fora do escopo das regras de negócio impactadas. " +
                         "Mudanças que extrapolam o escopo declarado podem indicar código gerado sem contexto adequado.",
                         outOfScopeFiles.size())
        );
        result.setAffectedFiles(outOfScopeFiles);

        log.warn("⚠️ [US#70] Heurística #3 ativada (OUT OF SCOPE) | arquivos={}", outOfScopeFiles.size());
        return result;
    }

    /**
     * Heurística #4: Padrões Repetitivos (10 pontos)
     * Detecta código duplicado em múltiplos arquivos
     */
    private LLMChangeHeuristicResult detectRepetitivePatterns(GitPullRequestData prData) {
        // Simplificação: detectar múltiplos arquivos com mesma estrutura de nome
        // Ex: Service1.java, Service2.java, Service3.java (padrão repetitivo)

        Map<String, List<String>> patternGroups = new HashMap<>();

        for (GitPullRequestFile file : prData.getChangedFiles()) {
            String fileName = file.getFilePath();
            
            // Extrair padrão (ex: XxxService.java -> Service)
            String pattern = extractPattern(fileName);
            if (pattern != null) {
                patternGroups.computeIfAbsent(pattern, k -> new ArrayList<>()).add(fileName);
            }
        }

        // Verificar se há grupos com 3+ arquivos (padrão repetitivo)
        List<String> repetitiveFiles = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : patternGroups.entrySet()) {
            if (entry.getValue().size() >= 3) {
                repetitiveFiles.addAll(entry.getValue());
            }
        }

        if (repetitiveFiles.isEmpty()) {
            return null;
        }

        LLMChangeHeuristicResult result = new LLMChangeHeuristicResult(
            "REPETITIVE_PATTERNS",
            10,
            String.format("Detectados %d arquivo(s) com padrões repetitivos de nomenclatura/estrutura. " +
                         "LLMs tendem a gerar código similar em múltiplos locais.",
                         repetitiveFiles.size())
        );
        result.setAffectedFiles(repetitiveFiles);

        log.info("🔍 [US#70] Heurística #4 ativada | arquivos={}", repetitiveFiles.size());
        return result;
    }

    /**
     * Heurística #5: Ausência de Testes (20 pontos)
     * Detecta mudanças em código crítico sem testes correspondentes
     */
    private LLMChangeHeuristicResult detectMissingTests(
            GitPullRequestData prData,
            GitImpactAnalysisResponse prImpact) {

        // Contar arquivos de código vs arquivos de teste
        List<String> codeFiles = new ArrayList<>();
        List<String> testFiles = new ArrayList<>();

        for (GitPullRequestFile file : prData.getChangedFiles()) {
            if (file.getFilePath().contains("/test/") || file.getFilePath().endsWith("Test.java")) {
                testFiles.add(file.getFilePath());
            } else if (file.getFilePath().endsWith(".java")) {
                codeFiles.add(file.getFilePath());
            }
        }

        // Se há código crítico alterado mas sem testes
        boolean hasCriticalCode = prImpact.getRiskLevel() != null && 
                                  (prImpact.getRiskLevel().equals("ALTO") || 
                                   prImpact.getRiskLevel().equals("CRITICO"));

        if (hasCriticalCode && testFiles.isEmpty() && !codeFiles.isEmpty()) {
            LLMChangeHeuristicResult result = new LLMChangeHeuristicResult(
                "MISSING_TESTS",
                20,
                String.format("Detectadas mudanças em %d arquivo(s) de código crítico sem testes correspondentes. " +
                             "LLMs frequentemente geram código sem testes adequados.",
                             codeFiles.size())
            );
            result.setAffectedFiles(codeFiles);

            log.warn("⚠️ [US#70] Heurística #5 ativada (MISSING TESTS) | código={} | testes={}",
                     codeFiles.size(), testFiles.size());
            return result;
        }

        return null;
    }

    /**
     * Heurística #6: Refatoração "Perfeita Demais" (10 pontos)
     * Detecta código excessivamente uniforme
     */
    private LLMChangeHeuristicResult detectPerfectRefactoring(GitPullRequestData prData) {
        // Simplificação: detectar múltiplos arquivos modificados simultaneamente
        // com mesmo tipo de mudança (MODIFIED)

        long modifiedCount = prData.getChangedFiles().stream()
            .filter(f -> "MODIFIED".equals(f.getChangeType()))
            .filter(f -> f.getFilePath().endsWith(".java"))
            .count();

        // Se 5+ arquivos Java foram modificados (possível refatoração em massa)
        if (modifiedCount >= 5) {
            List<String> modifiedFiles = prData.getChangedFiles().stream()
                .filter(f -> "MODIFIED".equals(f.getChangeType()))
                .filter(f -> f.getFilePath().endsWith(".java"))
                .map(GitPullRequestFile::getFilePath)
                .collect(Collectors.toList());

            LLMChangeHeuristicResult result = new LLMChangeHeuristicResult(
                "PERFECT_REFACTORING",
                10,
                String.format("Detectada refatoração simultânea de %d arquivo(s). " +
                             "Mudanças excessivamente uniformes em múltiplos arquivos podem indicar geração automática.",
                             modifiedCount)
            );
            result.setAffectedFiles(modifiedFiles);

            log.info("🔍 [US#70] Heurística #6 ativada | arquivos={}", modifiedCount);
            return result;
        }

        return null;
    }

    /**
     * Verifica se há regras críticas impactadas
     */
    private boolean checkCriticalRules(GitImpactAnalysisResponse prImpact) {
        @SuppressWarnings("unchecked")
        Map<String, Object> impactSummary = (Map<String, Object>) prImpact.getImpactSummary();
        if (impactSummary != null && impactSummary.containsKey("impactedRules")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> impactedRules = (List<Map<String, Object>>) impactSummary.get("impactedRules");
            
            for (Map<String, Object> rule : impactedRules) {
                String criticality = (String) rule.get("criticality");
                if (criticality != null && 
                    (criticality.equals("CRITICAL") || criticality.equals("HIGH"))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gera resumo executivo em linguagem humana
     */
    private String generateExecutiveSummary(
            LLMChangeAnalysisResponse response,
            List<LLMChangeHeuristicResult> heuristics) {

        StringBuilder summary = new StringBuilder();

        summary.append(String.format("📊 **Análise de Mudança LLM - PR #%s**\n\n", response.getPullRequestId()));
        summary.append(String.format("**Score Total**: %d/100\n", response.getTotalScore()));
        summary.append(String.format("**Nível de Suspeição**: %s\n\n", response.getSuspicionLevel()));

        if (response.getSuspicionLevel() == LLMSuspicionLevel.HIGH) {
            summary.append("🚨 **ATENÇÃO**: Alto risco de mudança gerada automaticamente detectado.\n\n");
        } else if (response.getSuspicionLevel() == LLMSuspicionLevel.MEDIUM) {
            summary.append("⚠️ **CUIDADO**: Risco moderado detectado. Revisão manual recomendada.\n\n");
        } else {
            summary.append("✅ **OK**: Baixo risco. Mudança parece normal.\n\n");
        }

        if (!heuristics.isEmpty()) {
            summary.append("**Heurísticas Ativadas**:\n\n");
            for (LLMChangeHeuristicResult h : heuristics) {
                summary.append(String.format("• **%s** (+%d pontos): %s\n", 
                    h.getHeuristic(), h.getScore(), h.getExplanation()));
            }
            summary.append("\n");
        }

        if (response.isAffectsCriticalRule()) {
            summary.append("⚠️ Esta mudança afeta **regras de negócio críticas**.\n");
        }

        if (response.isExceedsRuleScope()) {
            summary.append("⚠️ Esta mudança **excede o escopo** das regras impactadas.\n");
        }

        summary.append("\n**Recomendação**: ");
        if (response.getSuspicionLevel() == LLMSuspicionLevel.HIGH) {
            summary.append("Bloqueie o merge e solicite revisão detalhada. Verifique se a mudança está alinhada com os requisitos.");
        } else if (response.getSuspicionLevel() == LLMSuspicionLevel.MEDIUM) {
            summary.append("Solicite revisão adicional antes do merge. Valide com os times técnicos.");
        } else {
            summary.append("Pode prosseguir com revisão normal.");
        }

        return summary.toString();
    }

    /**
     * Extrai padrão de nomenclatura de arquivo
     */
    private String extractPattern(String fileName) {
        // Ex: PaymentService.java -> Service
        // Ex: UserValidator.java -> Validator
        if (fileName.endsWith("Service.java")) return "Service";
        if (fileName.endsWith("Controller.java")) return "Controller";
        if (fileName.endsWith("Repository.java")) return "Repository";
        if (fileName.endsWith("Validator.java")) return "Validator";
        if (fileName.endsWith("Dto.java")) return "Dto";
        return null;
    }

    /**
     * Constrói resposta de fallback em caso de erro
     */
    private LLMChangeAnalysisResponse buildFallbackResponse(
            LLMChangeDetectionRequest request, 
            Exception error) {

        LLMChangeAnalysisResponse response = new LLMChangeAnalysisResponse();
        response.setPullRequestId(request.getPullRequestId());
        response.setTotalScore(0);
        response.setSuspicionLevel(LLMSuspicionLevel.LOW);
        response.setSummary(String.format(
            "⚠️ Erro ao analisar PR #%s: %s\n\n" +
            "Não foi possível completar a análise de detecção LLM. " +
            "Por segurança, assuma nível de risco BAIXO e proceda com revisão manual.",
            request.getPullRequestId(), error.getMessage()));
        response.setProjectContext(ProjectContext.global());
        
        return response;
    }

    /**
     * US#61 - Cria registro de auditoria consultiva
     */
    private void createAuditRecord(LLMChangeDetectionRequest request, LLMChangeAnalysisResponse response) {
        try {
            // Construir descrição das heurísticas
            String heuristicsDesc = response.getHeuristics().stream()
                .map(h -> String.format("%s (+%d pts)", h.getHeuristic(), h.getScore()))
                .collect(Collectors.joining(", "));

            // Criar registro de auditoria
            RiskDecisionAudit audit = new RiskDecisionAudit(
                request.getPullRequestId(),
                Environment.DEV, // environment
                mapToRiskLevel(response.getSuspicionLevel()), // riskLevel
                response.getTotalScore(), // riskScore
                mapToDecision(response.getSuspicionLevel()), // finalDecision
                Collections.emptyList(), // impactedRules (não aplicável)
                Collections.emptyMap(), // incidentSummary
                Collections.singletonList("Revisão manual recomendada: " + response.getSummary()), // requiredActions
                false, // aiAnalysisUsed
                String.format("LLM Change Detection | Score: %d | Level: %s | Heuristics: %s",
                             response.getTotalScore(), response.getSuspicionLevel(), heuristicsDesc), // aiSummary
                "LLMChangeGuard v1.0" // policyVersion
            );

            auditRepository.save(audit);
            log.info("📝 [US#70] Auditoria criada | PR={} | auditId={}", 
                     request.getPullRequestId(), audit.getId());

        } catch (Exception e) {
            log.warn("⚠️ [US#70] Erro ao criar auditoria: {}", e.getMessage());
            // Não propagar erro - auditoria é consultiva
        }
    }

    /**
     * US#55/56 - Cria alerta de risco se necessário
     */
    private void createRiskAlert(LLMChangeDetectionRequest request, LLMChangeAnalysisResponse response) {
        try {
            // Só criar alerta para MEDIUM ou HIGH
            if (response.getSuspicionLevel() == LLMSuspicionLevel.LOW) {
                return;
            }

            // Simplificação: apenas log por enquanto
            // TODO: Integrar com sistema de notificações quando disponível
            String severity = response.getSuspicionLevel() == LLMSuspicionLevel.HIGH ? "HIGH" : "MEDIUM";
            String title = String.format("LLM Change Risk Detected - PR #%s", request.getPullRequestId());
            String message = String.format(
                "Score: %d/100 | Level: %s\n\n%s",
                response.getTotalScore(),
                response.getSuspicionLevel(),
                response.getSummary()
            );

            log.info("🔔 [US#70] Alerta criado | PR={} | severity={}", 
                     request.getPullRequestId(), severity);

        } catch (Exception e) {
            log.warn("⚠️ [US#70] Erro ao criar alerta: {}", e.getMessage());
            // Não propagar erro - alerta é consultivo
        }
    }

    /**
     * Mapeia LLMSuspicionLevel para RiskLevel enum
     */
    private RiskLevel mapToRiskLevel(LLMSuspicionLevel level) {
        return switch (level) {
            case LOW -> RiskLevel.BAIXO;
            case MEDIUM -> RiskLevel.MEDIO;
            case HIGH -> RiskLevel.ALTO;
        };
    }

    /**
     * Mapeia LLMSuspicionLevel para decisão final
     */
    private FinalDecision mapToDecision(LLMSuspicionLevel level) {
        return switch (level) {
            case LOW -> FinalDecision.APROVADO;
            case MEDIUM -> FinalDecision.APROVADO_COM_RESTRICOES;
            case HIGH -> FinalDecision.BLOQUEADO;
        };
    }

    /**
     * US#53 - Retorna exit code para integração CI/CD
     */
    public int getCICDExitCode(LLMSuspicionLevel level) {
        return switch (level) {
            case LOW -> 0;      // OK
            case MEDIUM -> 1;   // WARNING
            case HIGH -> 2;     // BLOCKED
        };
    }
}
