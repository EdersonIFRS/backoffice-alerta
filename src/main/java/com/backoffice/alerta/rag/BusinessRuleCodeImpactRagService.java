package com.backoffice.alerta.rag;

import com.backoffice.alerta.ast.ASTCodeAnalysisService;
import com.backoffice.alerta.ast.ASTImpactDetail;
import com.backoffice.alerta.project.dto.ProjectContext;
import com.backoffice.alerta.project.domain.Project;
import com.backoffice.alerta.project.repository.ProjectRepository;
import com.backoffice.alerta.project.domain.ProjectBusinessRule;
import com.backoffice.alerta.project.repository.ProjectBusinessRuleRepository;
import com.backoffice.alerta.repository.BusinessRuleIncidentRepository;
import com.backoffice.alerta.rules.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service para RAG de impacto no código
 * 
 * US#45 - RAG com Mapeamento de Código Impactado
 * 
 * Responde perguntas sobre onde mexer no código e o que pode quebrar.
 * Reutiliza US#43/44 e adiciona análise de arquivos e dependências.
 * 
 * Read-only, sem auditoria, sem persistência.
 */
@Service
public class BusinessRuleCodeImpactRagService {
    
    private static final Logger log = LoggerFactory.getLogger(BusinessRuleCodeImpactRagService.class);
    
    private final BusinessRuleRagService ragService;
    private final FileBusinessRuleMappingRepository fileMappingRepository;
    private final BusinessRuleDependencyRepository dependencyRepository;
    private final BusinessRuleIncidentRepository incidentRepository;
    private final BusinessRuleOwnershipRepository ownershipRepository;
    private final RagLLMClient llmClient;
    private final ProjectRepository projectRepository;
    private final ProjectBusinessRuleRepository projectBusinessRuleRepository;
    private final Gson gson;
    
    @Autowired(required = false)
    private ASTCodeAnalysisService astCodeAnalysisService;
    
    public BusinessRuleCodeImpactRagService(
            BusinessRuleRagService ragService,
            FileBusinessRuleMappingRepository fileMappingRepository,
            BusinessRuleDependencyRepository dependencyRepository,
            BusinessRuleIncidentRepository incidentRepository,
            BusinessRuleOwnershipRepository ownershipRepository,
            RagLLMClient llmClient,
            ProjectRepository projectRepository,
            ProjectBusinessRuleRepository projectBusinessRuleRepository) {
        this.ragService = ragService;
        this.fileMappingRepository = fileMappingRepository;
        this.dependencyRepository = dependencyRepository;
        this.incidentRepository = incidentRepository;
        this.ownershipRepository = ownershipRepository;
        this.llmClient = llmClient;
        this.projectRepository = projectRepository;
        this.projectBusinessRuleRepository = projectBusinessRuleRepository;
        this.gson = new Gson();
    }
    
    public RagCodeImpactResponse analyzeCodeImpact(RagCodeImpactRequest request) {
        log.info("🔍 Code Impact RAG Query: '{}' (focus: {}, maxFiles: {})", 
                 request.getQuestion(), request.getFocus(), request.getMaxFiles());
        
        // US#50: Escopo de projeto (opcional)
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
        
        // 1. Reutilizar RAG existente para encontrar regras relevantes
        RagQueryRequest ragRequest = new RagQueryRequest(request.getQuestion());
        ragRequest.setFocus(request.getFocus());
        ragRequest.setMaxSources(request.getMaxFiles() * 2);
        ragRequest.setProjectId(request.getProjectId());
        
        RagQueryResponse ragResponse = ragService.query(ragRequest);
        
        if (ragResponse.getSources().isEmpty()) {
            return buildNoDataResponse(request, project);
        }
        
        // 2. Enriquecer com dados técnicos
        List<String> ruleIds = ragResponse.getSources().stream()
            .map(RagSourceReference::getId)
            .collect(Collectors.toList());
        
        TechnicalEnrichment enrichment = enrichWithTechnicalData(ruleIds, request.getMaxFiles());
        
        // 3. Gerar resposta com IA ou fallback
        RagCodeImpactResponse response = generateCodeImpactResponse(
            request, 
            enrichment, 
            ragResponse
        );
        
        // US#50: Adicionar contexto de projeto
        response.setProjectContext(project != null 
            ? ProjectContext.scoped(project.getId(), project.getName())
            : ProjectContext.global());
        
        // US#69: Adicionar análise AST para arquivos Java
        if (astCodeAnalysisService != null && !enrichment.rankedFiles.isEmpty()) {
            try {
                List<ASTImpactDetail> astDetails = performASTAnalysis(enrichment.rankedFiles);
                response.setAstDetails(astDetails);
                log.info("🧩 [US#69] Análise AST concluída | detalhes={}", astDetails.size());
            } catch (Exception e) {
                log.warn("⚠️ [US#69] Erro na análise AST, continuando sem detalhes AST: {}", e.getMessage());
                response.setAstDetails(new ArrayList<>());
            }
        } else {
            response.setAstDetails(new ArrayList<>());
        }
        
        log.info("✅ Code Impact RAG Response: confidence={}, rules={}, files={}, usedFallback={}, scoped={}, astDetails={}", 
                 response.getConfidence(), 
                 response.getImpactedRules().size(),
                 response.getImpactedFiles().size(),
                 response.isUsedFallback(),
                 response.getProjectContext().isScoped(),
                 response.getAstDetails().size());
        
        return response;
    }
    
    /**
     * US#69 - Realiza análise AST em arquivos Java mapeados
     */
    private List<ASTImpactDetail> performASTAnalysis(List<FileBusinessRuleMapping> rankedFiles) {
        // Filtrar apenas arquivos .java
        List<FileBusinessRuleMapping> javaFiles = rankedFiles.stream()
            .filter(f -> f.getFilePath().endsWith(".java"))
            .collect(Collectors.toList());
        
        if (javaFiles.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Em produção, ler conteúdo real dos arquivos do sistema de arquivos ou Git
        // Por ora, usaremos placeholder (integração futura com Git/FileSystem)
        Map<String, String> javaFileContents = new HashMap<>();
        for (FileBusinessRuleMapping mapping : javaFiles) {
            // TODO: Integrar com Git ou FileSystem para ler conteúdo real
            // javaFileContents.put(mapping.getFilePath(), readFileContent(mapping.getFilePath()));
            log.debug("📄 [US#69] Arquivo Java identificado para análise AST: {}", mapping.getFilePath());
        }
        
        // Se não temos conteúdo real, retornar lista vazia
        if (javaFileContents.isEmpty()) {
            log.debug("ℹ️ [US#69] Nenhum conteúdo de arquivo disponível para análise AST");
            return new ArrayList<>();
        }
        
        return astCodeAnalysisService.analyzeFiles(javaFileContents);
    }
    
    private TechnicalEnrichment enrichWithTechnicalData(List<String> ruleIds, int maxFiles) {
        log.info("📊 Enriquecendo com dados técnicos para {} regras", ruleIds.size());
        
        TechnicalEnrichment enrichment = new TechnicalEnrichment();
        
        for (String ruleId : ruleIds) {
            try {
                // Buscar arquivos mapeados (aceita String)
                List<FileBusinessRuleMapping> mappings = fileMappingRepository.findByBusinessRuleId(ruleId);
                enrichment.fileMappings.addAll(mappings);
                
                log.info("📄 Regra {} tem {} arquivo(s) mapeado(s)", ruleId, mappings.size());
                
                // Buscar dependências (aceita String)
                List<BusinessRuleDependency> dependencies = dependencyRepository.findBySourceRuleId(ruleId);
                enrichment.dependencies.addAll(dependencies);
                
                log.info("🔗 Regra {} tem {} dependência(s)", ruleId, dependencies.size());
                
                // Para incident e ownership, precisamos converter para UUID
                try {
                    UUID ruleUuid = UUID.fromString(ruleId);
                    
                    // Verificar incidentes
                    long incidentCount = incidentRepository.findByBusinessRuleIdOrderByOccurredAtDesc(ruleUuid).size();
                    if (incidentCount > 0) {
                        enrichment.rulesWithIncidents.add(ruleId);
                        log.info("⚠️ Regra {} tem {} incidente(s)", ruleId, incidentCount);
                    }
                    
                    // Buscar ownership
                    List<BusinessRuleOwnership> ownerships = ownershipRepository.findByBusinessRuleId(ruleUuid);
                    enrichment.ownerships.addAll(ownerships);
                    
                } catch (IllegalArgumentException e) {
                    log.warn("⚠️ ID de regra não é UUID válido: {}", ruleId);
                }
                
            } catch (Exception e) {
                log.error("❌ Erro ao enriquecer regra {}: {}", ruleId, e.getMessage());
            }
        }
        
        // Ranking de arquivos
        enrichment.rankedFiles = rankFiles(enrichment.fileMappings, enrichment.dependencies, maxFiles);
        
        log.info("✅ Enriquecimento concluído: {} arquivos, {} dependências, {} ownerships",
                 enrichment.rankedFiles.size(),
                 enrichment.dependencies.size(),
                 enrichment.ownerships.size());
        
        return enrichment;
    }
    
    private List<FileBusinessRuleMapping> rankFiles(
            List<FileBusinessRuleMapping> mappings,
            List<BusinessRuleDependency> dependencies,
            int maxFiles) {
        
        if (mappings.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Ranking simples: implementação direta tem prioridade
        Map<String, Integer> fileScores = new HashMap<>();
        
        for (FileBusinessRuleMapping mapping : mappings) {
            String filePath = mapping.getFilePath();
            int score = 0;
            
            // Implementação direta
            if (mapping.getImpactType() == ImpactType.DIRECT) {
                score += 100;
            } else {
                score += 50;
            }
            
            // Soma ao score existente
            fileScores.merge(filePath, score, Integer::sum);
        }
        
        // Ordenar por score e limitar
        return mappings.stream()
            .sorted((m1, m2) -> {
                int score1 = fileScores.getOrDefault(m1.getFilePath(), 0);
                int score2 = fileScores.getOrDefault(m2.getFilePath(), 0);
                return Integer.compare(score2, score1);
            })
            .limit(maxFiles)
            .collect(Collectors.toList());
    }
    
    private RagCodeImpactResponse generateCodeImpactResponse(
            RagCodeImpactRequest request,
            TechnicalEnrichment enrichment,
            RagQueryResponse ragResponse) {
        
        RagCodeImpactResponse response = new RagCodeImpactResponse();
        
        try {
            // Tentar usar IA
            String context = buildTechnicalContext(enrichment, ragResponse);
            RagLLMClient.RagAnswer aiAnswer = llmClient.generateAnswer(
                request.getQuestion(),
                context,
                request.getFocus()
            );
            
            if (aiAnswer.isSuccess() && aiAnswer.getAnswer() != null && !aiAnswer.getAnswer().isBlank()) {
                response.setAnswer(aiAnswer.getAnswer());
                response.setConfidence(aiAnswer.getConfidence());
                response.setUsedFallback(false);
            } else {
                generateFallbackResponse(response, request, enrichment, ragResponse);
            }
            
        } catch (Exception e) {
            log.warn("⚠️ IA falhou, usando fallback: {}", e.getMessage());
            generateFallbackResponse(response, request, enrichment, ragResponse);
        }
        
        // Preencher dados estruturados
        populateStructuredData(response, enrichment, ragResponse);
        
        // US#63: Propagar scores do RAG
        response.setRuleScores(ragResponse.getRuleScores());
        
        response.setDisclaimer("⚠️ Esta análise é baseada em dados reais do sistema. " +
                              "Não constitui decisão executiva. Valide com os times técnicos antes de alterações.");
        
        return response;
    }
    
    private String buildTechnicalContext(TechnicalEnrichment enrichment, RagQueryResponse ragResponse) {
        JsonObject context = new JsonObject();
        
        // Regras
        JsonArray rulesArray = new JsonArray();
        for (RagSourceReference source : ragResponse.getSources()) {
            JsonObject ruleObj = new JsonObject();
            ruleObj.addProperty("id", source.getId());
            ruleObj.addProperty("name", source.getTitle());
            ruleObj.addProperty("domain", source.getDomain().name());
            ruleObj.addProperty("criticality", source.getCriticality().name());
            rulesArray.add(ruleObj);
        }
        context.add("rules", rulesArray);
        
        // Arquivos
        JsonArray filesArray = new JsonArray();
        for (FileBusinessRuleMapping mapping : enrichment.rankedFiles) {
            JsonObject fileObj = new JsonObject();
            fileObj.addProperty("path", mapping.getFilePath());
            fileObj.addProperty("type", mapping.getImpactType().name());
            filesArray.add(fileObj);
        }
        context.add("files", filesArray);
        
        // Dependências
        JsonArray depsArray = new JsonArray();
        for (BusinessRuleDependency dep : enrichment.dependencies) {
            JsonObject depObj = new JsonObject();
            depObj.addProperty("type", dep.getDependencyType().name());
            depsArray.add(depObj);
        }
        context.add("dependencies", depsArray);
        
        return gson.toJson(context);
    }
    
    private void generateFallbackResponse(
            RagCodeImpactResponse response,
            RagCodeImpactRequest request,
            TechnicalEnrichment enrichment,
            RagQueryResponse ragResponse) {
        
        StringBuilder answer = new StringBuilder();
        
        int ruleCount = ragResponse.getSources().size();
        int fileCount = enrichment.rankedFiles.size();
        int depCount = enrichment.dependencies.size();
        
        answer.append(String.format("📊 Encontrei %d regra(s) de negócio com %d arquivo(s) impactado(s).\n\n", 
                                    ruleCount, fileCount));
        
        if (fileCount > 0) {
            answer.append("📄 **Arquivos principais para alteração:**\n\n");
            for (int i = 0; i < Math.min(5, fileCount); i++) {
                FileBusinessRuleMapping mapping = enrichment.rankedFiles.get(i);
                answer.append(String.format("• `%s` (%s)\n",
                    mapping.getFilePath(),
                    mapping.getImpactType().name()
                ));
            }
            answer.append("\n");
        }
        
        if (depCount > 0) {
            answer.append(String.format("🔗 **Atenção:** %d dependência(s) detectada(s). " +
                                       "Mudanças podem ter efeito cascata.\n\n", depCount));
        }
        
        if (!enrichment.ownerships.isEmpty()) {
            answer.append(String.format("👥 **Avisar:** %d time(s) responsável(is).\n\n", 
                                       enrichment.ownerships.size()));
        }
        
        answer.append("⚠️ Resposta gerada de forma determinística (IA indisponível).");
        
        response.setAnswer(answer.toString());
        response.setConfidence(fileCount > 0 ? ConfidenceLevel.MEDIUM : ConfidenceLevel.LOW);
        response.setUsedFallback(true);
    }
    
    private void populateStructuredData(
            RagCodeImpactResponse response,
            TechnicalEnrichment enrichment,
            RagQueryResponse ragResponse) {
        
        // Regras impactadas
        for (RagSourceReference source : ragResponse.getSources()) {
            boolean hasIncidents = enrichment.rulesWithIncidents.contains(source.getId());
            response.getImpactedRules().add(new RagCodeImpactResponse.ImpactedRuleInfo(
                source.getId(),
                source.getTitle(),
                source.getCriticality(),
                hasIncidents
            ));
        }
        
        // Arquivos impactados
        for (FileBusinessRuleMapping mapping : enrichment.rankedFiles) {
            String reason = String.format("Implementação %s da regra", mapping.getImpactType().name());
            String riskLevel = mapping.getImpactType() == ImpactType.DIRECT ? "HIGH" : "MEDIUM";
            
            response.getImpactedFiles().add(new RagCodeImpactResponse.ImpactedFileInfo(
                mapping.getFilePath(),
                reason,
                riskLevel
            ));
        }
        
        // Dependências
        int direct = enrichment.dependencies.size();
        int indirect = 0;
        int cascade = 0;
        
        response.setDependencyImpact(new RagCodeImpactResponse.DependencyImpact(direct, indirect, cascade));
        
        // Ownerships
        for (BusinessRuleOwnership ownership : enrichment.ownerships) {
            response.getOwnerships().add(new RagCodeImpactResponse.OwnershipInfo(
                ownership.getTeamName(),
                ownership.getRole().name(),
                ownership.getContactEmail()
            ));
        }
    }
    
    private RagCodeImpactResponse buildNoDataResponse(RagCodeImpactRequest request, Project project) {
        RagCodeImpactResponse response = new RagCodeImpactResponse();
        response.setAnswer("❌ Não encontrei regras ou arquivos relevantes para essa pergunta. " +
                          "Tente reformular usando termos como: pagamento, validação, cálculo, PJ, CPF.");
        response.setConfidence(ConfidenceLevel.LOW);
        response.setUsedFallback(true);
        response.setDependencyImpact(new RagCodeImpactResponse.DependencyImpact(0, 0, 0));
        response.setDisclaimer("⚠️ Nenhum dado encontrado.");
        
        // US#50: Adicionar contexto de projeto
        response.setProjectContext(project != null 
            ? ProjectContext.scoped(project.getId(), project.getName())
            : ProjectContext.global());
        
        return response;
    }
    
    /**
     * Classe interna para armazenar dados enriquecidos
     */
    private static class TechnicalEnrichment {
        List<FileBusinessRuleMapping> fileMappings = new ArrayList<>();
        List<FileBusinessRuleMapping> rankedFiles = new ArrayList<>();
        List<BusinessRuleDependency> dependencies = new ArrayList<>();
        List<BusinessRuleOwnership> ownerships = new ArrayList<>();
        Set<String> rulesWithIncidents = new HashSet<>();
    }
}
