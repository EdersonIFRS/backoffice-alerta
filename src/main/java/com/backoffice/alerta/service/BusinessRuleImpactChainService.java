package com.backoffice.alerta.service;

import com.backoffice.alerta.project.dto.ProjectContext;
import com.backoffice.alerta.project.domain.Project;
import com.backoffice.alerta.project.repository.ProjectRepository;
import com.backoffice.alerta.project.domain.ProjectBusinessRule;
import com.backoffice.alerta.project.repository.ProjectBusinessRuleRepository;
import com.backoffice.alerta.dto.BusinessImpactChainResponse;
import com.backoffice.alerta.dto.BusinessImpactRequest;
import com.backoffice.alerta.dto.BusinessImpactResponse;
import com.backoffice.alerta.dto.BusinessRuleOwnershipResponse;
import com.backoffice.alerta.dto.ImpactedBusinessRuleResponse;
import com.backoffice.alerta.dto.ImpactedRuleChainResponse;
import com.backoffice.alerta.dto.ImpactedRuleChainResponse.ImpactLevel;
import com.backoffice.alerta.rules.BusinessRule;
import com.backoffice.alerta.rules.BusinessRuleDependency;
import com.backoffice.alerta.rules.BusinessRuleDependencyRepository;
import com.backoffice.alerta.rules.BusinessRuleOwnership;
import com.backoffice.alerta.rules.BusinessRuleOwnershipRepository;
import com.backoffice.alerta.rules.BusinessRuleRepository;
import com.backoffice.alerta.rules.Criticality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço para análise de impacto cruzado e cadeia de dependências entre regras de negócio
 * 
 * Responsabilidades:
 * 1. Identificar regras diretamente impactadas (reutiliza US#16)
 * 2. Traversar grafo de dependências para encontrar impactos indiretos
 * 3. Detectar impactos em cascata (múltiplos níveis)
 * 4. Evitar ciclos infinitos
 * 5. Limitar profundidade de análise (máx 3 níveis)
 * 6. Gerar caminhos de dependência explicativos
 * 
 * ⚠️ READ-ONLY: NÃO recalcula risco, NÃO altera dados, NÃO gera side-effects
 * 
 * US#36 - Análise de Impacto Cruzado (Cadeia de Regras Afetadas)
 */
@Service
public class BusinessRuleImpactChainService {
    
    private static final Logger log = LoggerFactory.getLogger(BusinessRuleImpactChainService.class);
    private static final int MAX_DEPTH = 3;
    
    private final BusinessImpactAnalysisService directImpactService;
    private final BusinessRuleDependencyRepository dependencyRepository;
    private final BusinessRuleRepository businessRuleRepository;
    private final BusinessRuleOwnershipRepository ownershipRepository;
    private final ProjectRepository projectRepository;
    private final ProjectBusinessRuleRepository projectBusinessRuleRepository;
    
    public BusinessRuleImpactChainService(
            BusinessImpactAnalysisService directImpactService,
            BusinessRuleDependencyRepository dependencyRepository,
            BusinessRuleRepository businessRuleRepository,
            BusinessRuleOwnershipRepository ownershipRepository,
            ProjectRepository projectRepository,
            ProjectBusinessRuleRepository projectBusinessRuleRepository) {
        this.directImpactService = directImpactService;
        this.dependencyRepository = dependencyRepository;
        this.businessRuleRepository = businessRuleRepository;
        this.ownershipRepository = ownershipRepository;
        this.projectRepository = projectRepository;
        this.projectBusinessRuleRepository = projectBusinessRuleRepository;
    }
    
    /**
     * Analisa impacto cruzado completo incluindo cadeia de dependências
     * 
     * @param request Dados do Pull Request com arquivos alterados
     * @return Análise completa com impactos diretos, indiretos e em cascata
     */
    public BusinessImpactChainResponse analyzeImpactChain(BusinessImpactRequest request) {
        log.info("🔗 [CHAIN] Iniciando análise de impacto cruzado para PR: {}", request.getPullRequestId());
        
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
        
        // 1. Obter impactos diretos (reutiliza US#16)
        BusinessImpactResponse directAnalysis = directImpactService.analyze(request);
        List<ImpactedRuleChainResponse> directImpacts = convertToChainResponses(
            directAnalysis.getImpactedBusinessRules(),
            ImpactLevel.DIRECT
        );
        
        log.info("📍 [CHAIN] {} regras diretamente impactadas", directImpacts.size());
        
        // 2. Extrair IDs das regras diretamente impactadas
        Set<String> directRuleIds = directImpacts.stream()
            .map(ImpactedRuleChainResponse::getBusinessRuleId)
            .collect(Collectors.toSet());
        
        // 3. Descobrir impactos indiretos (1 nível de profundidade)
        List<ImpactedRuleChainResponse> indirectImpacts = discoverIndirectImpacts(directRuleIds);
        log.info("🔄 [CHAIN] {} regras indiretamente impactadas", indirectImpacts.size());
        
        // 4. Descobrir impactos em cascata (2-3 níveis de profundidade)
        Set<String> processedRuleIds = new HashSet<>();
        processedRuleIds.addAll(directRuleIds);
        processedRuleIds.addAll(indirectImpacts.stream()
            .map(ImpactedRuleChainResponse::getBusinessRuleId)
            .collect(Collectors.toSet()));
        
        List<ImpactedRuleChainResponse> cascadeImpacts = discoverCascadeImpacts(
            indirectImpacts,
            processedRuleIds
        );
        log.info("🌊 [CHAIN] {} regras impactadas em cascata", cascadeImpacts.size());
        
        // 5. Gerar sumário executivo
        BusinessImpactChainResponse.ChainSummary summary = generateSummary(
            directImpacts,
            indirectImpacts,
            cascadeImpacts
        );
        
        log.info("✅ [CHAIN] Análise concluída: {} regras afetadas no total", 
            summary.getTotalRulesAffected());
        
        BusinessImpactChainResponse response = new BusinessImpactChainResponse(
            request.getPullRequestId(),
            directImpacts,
            indirectImpacts,
            cascadeImpacts,
            summary
        );
        
        // US#50: Adicionar contexto de projeto
        response.setProjectContext(project != null 
            ? ProjectContext.scoped(project.getId(), project.getName())
            : ProjectContext.global());
        
        return response;
    }
    
    /**
     * Descobre impactos indiretos (1 nível além dos impactos diretos)
     */
    private List<ImpactedRuleChainResponse> discoverIndirectImpacts(Set<String> directRuleIds) {
        List<ImpactedRuleChainResponse> indirectImpacts = new ArrayList<>();
        
        for (String directRuleId : directRuleIds) {
            // Busca regras que dependem desta regra diretamente impactada
            List<BusinessRuleDependency> dependencies = 
                dependencyRepository.findBySourceRuleId(directRuleId);
            
            for (BusinessRuleDependency dep : dependencies) {
                String targetRuleId = dep.getTargetRuleId();
                
                // Evita duplicação
                if (directRuleIds.contains(targetRuleId)) {
                    continue;
                }
                
                Optional<BusinessRule> targetRuleOpt = 
                    businessRuleRepository.findById(targetRuleId);
                
                if (targetRuleOpt.isPresent()) {
                    BusinessRule targetRule = targetRuleOpt.get();
                    List<String> path = List.of(directRuleId, targetRuleId);
                    
                    ImpactedRuleChainResponse chainResponse = createChainResponse(
                        targetRule,
                        ImpactLevel.INDIRECT,
                        path
                    );
                    
                    indirectImpacts.add(chainResponse);
                    
                    log.debug("🔗 Impacto indireto: {} -> {} ({})",
                        directRuleId, targetRuleId, dep.getDependencyType().getLabel());
                }
            }
        }
        
        return indirectImpacts;
    }
    
    /**
     * Descobre impactos em cascata (2-3 níveis de profundidade)
     */
    private List<ImpactedRuleChainResponse> discoverCascadeImpacts(
            List<ImpactedRuleChainResponse> indirectImpacts,
            Set<String> processedRuleIds) {
        
        List<ImpactedRuleChainResponse> cascadeImpacts = new ArrayList<>();
        
        for (ImpactedRuleChainResponse indirect : indirectImpacts) {
            String indirectRuleId = indirect.getBusinessRuleId();
            List<String> currentPath = indirect.getDependencyPath();
            
            // Limita profundidade a MAX_DEPTH
            if (currentPath.size() >= MAX_DEPTH) {
                log.debug("⚠️ Profundidade máxima atingida para regra: {}", indirectRuleId);
                continue;
            }
            
            // Busca próximo nível de dependências
            List<BusinessRuleDependency> nextDependencies = 
                dependencyRepository.findBySourceRuleId(indirectRuleId);
            
            for (BusinessRuleDependency dep : nextDependencies) {
                String targetRuleId = dep.getTargetRuleId();
                
                // Evita ciclos e duplicação
                if (processedRuleIds.contains(targetRuleId)) {
                    log.debug("🔄 Ciclo detectado ou regra já processada: {}", targetRuleId);
                    continue;
                }
                
                Optional<BusinessRule> targetRuleOpt = 
                    businessRuleRepository.findById(targetRuleId);
                
                if (targetRuleOpt.isPresent()) {
                    BusinessRule targetRule = targetRuleOpt.get();
                    
                    // Constrói caminho completo
                    List<String> newPath = new ArrayList<>(currentPath);
                    newPath.add(targetRuleId);
                    
                    ImpactedRuleChainResponse chainResponse = createChainResponse(
                        targetRule,
                        ImpactLevel.CASCADE,
                        newPath
                    );
                    
                    cascadeImpacts.add(chainResponse);
                    processedRuleIds.add(targetRuleId);
                    
                    log.debug("🌊 Impacto cascata: {} (profundidade: {})",
                        targetRuleId, newPath.size());
                }
            }
        }
        
        return cascadeImpacts;
    }
    
    /**
     * Converte impactos diretos da US#16 para formato de cadeia
     */
    private List<ImpactedRuleChainResponse> convertToChainResponses(
            List<ImpactedBusinessRuleResponse> directRules,
            ImpactLevel level) {
        
        return directRules.stream()
            .map(rule -> {
                List<String> path = Collections.singletonList(rule.getBusinessRuleId());
                return new ImpactedRuleChainResponse(
                    rule.getBusinessRuleId(),
                    rule.getName(),
                    level,
                    path,
                    rule.getCriticality(),
                    rule.getOwnerships()
                );
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Cria response de cadeia para uma regra impactada
     */
    private ImpactedRuleChainResponse createChainResponse(
            BusinessRule rule,
            ImpactLevel level,
            List<String> path) {
        
        List<BusinessRuleOwnershipResponse> ownerships = getOwnershipResponses(rule.getId());
        
        return new ImpactedRuleChainResponse(
            rule.getId(),
            rule.getName(),
            level,
            path,
            rule.getCriticality(),
            ownerships
        );
    }
    
    /**
     * Busca ownerships de uma regra
     */
    private List<BusinessRuleOwnershipResponse> getOwnershipResponses(String ruleId) {
        try {
            UUID ruleUuid = UUID.fromString(ruleId);
            List<BusinessRuleOwnership> ownerships = 
                ownershipRepository.findByBusinessRuleId(ruleUuid);
            
            return ownerships.stream()
                .map(BusinessRuleOwnershipResponse::new)
                .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ ID de regra inválido: {}", ruleId);
            return Collections.emptyList();
        }
    }
    
    /**
     * Gera sumário executivo da análise
     */
    private BusinessImpactChainResponse.ChainSummary generateSummary(
            List<ImpactedRuleChainResponse> directImpacts,
            List<ImpactedRuleChainResponse> indirectImpacts,
            List<ImpactedRuleChainResponse> cascadeImpacts) {
        
        int total = directImpacts.size() + indirectImpacts.size() + cascadeImpacts.size();
        
        // Encontra maior criticidade
        List<Criticality> allCriticalities = new ArrayList<>();
        directImpacts.forEach(r -> allCriticalities.add(r.getCriticality()));
        indirectImpacts.forEach(r -> allCriticalities.add(r.getCriticality()));
        cascadeImpacts.forEach(r -> allCriticalities.add(r.getCriticality()));
        
        Criticality highest = allCriticalities.stream()
            .max(Comparator.comparingInt(Criticality::ordinal))
            .orElse(Criticality.BAIXA);
        
        // Requer atenção executiva se houver regra CRÍTICA em impactos indiretos ou cascata
        boolean requiresAttention = indirectImpacts.stream()
            .anyMatch(r -> r.getCriticality() == Criticality.CRITICA)
            || cascadeImpacts.stream()
            .anyMatch(r -> r.getCriticality() == Criticality.CRITICA);
        
        return new BusinessImpactChainResponse.ChainSummary(
            total,
            highest,
            requiresAttention
        );
    }
}
