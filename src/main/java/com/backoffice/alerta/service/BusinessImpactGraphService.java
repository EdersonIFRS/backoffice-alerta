package com.backoffice.alerta.service;

import com.backoffice.alerta.project.dto.ProjectContext;
import com.backoffice.alerta.project.domain.Project;
import com.backoffice.alerta.project.repository.ProjectRepository;
import com.backoffice.alerta.dto.BusinessImpactChainResponse;
import com.backoffice.alerta.dto.BusinessImpactGraphResponse;
import com.backoffice.alerta.dto.BusinessImpactRequest;
import com.backoffice.alerta.dto.ImpactGraphEdgeResponse;
import com.backoffice.alerta.dto.ImpactGraphNodeResponse;
import com.backoffice.alerta.dto.ImpactedRuleChainResponse;
import com.backoffice.alerta.repository.BusinessRuleIncidentRepository;
import com.backoffice.alerta.rules.BusinessRule;
import com.backoffice.alerta.rules.BusinessRuleDependency;
import com.backoffice.alerta.rules.BusinessRuleDependencyRepository;
import com.backoffice.alerta.rules.BusinessRuleIncident;
import com.backoffice.alerta.rules.BusinessRuleOwnership;
import com.backoffice.alerta.rules.BusinessRuleOwnershipRepository;
import com.backoffice.alerta.rules.BusinessRuleRepository;
import com.backoffice.alerta.rules.Criticality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço para geração de grafo visual de impacto sistêmico
 * 
 * Responsabilidades:
 * 1. Reutilizar BusinessRuleImpactChainService (US#36) para análise de impacto
 * 2. Transformar resultados em formato de grafo (nós + arestas)
 * 3. Enriquecer nós com ownership e indicadores de incidentes
 * 4. Montar arestas baseadas em dependências reais
 * 5. Gerar sumário executivo para dashboard
 * 
 * ⚠️ READ-ONLY: Apenas transforma dados, não recalcula impacto ou cria side-effects
 * 
 * US#37 - Visualização de Impacto Sistêmico (Mapa de Dependências)
 */
@Service
public class BusinessImpactGraphService {
    
    private static final Logger log = LoggerFactory.getLogger(BusinessImpactGraphService.class);
    
    private final BusinessRuleImpactChainService impactChainService;
    private final BusinessRuleDependencyRepository dependencyRepository;
    private final BusinessRuleRepository businessRuleRepository;
    private final BusinessRuleOwnershipRepository ownershipRepository;
    private final BusinessRuleIncidentRepository incidentRepository;
    private final ProjectRepository projectRepository;
    
    public BusinessImpactGraphService(
            BusinessRuleImpactChainService impactChainService,
            BusinessRuleDependencyRepository dependencyRepository,
            BusinessRuleRepository businessRuleRepository,
            BusinessRuleOwnershipRepository ownershipRepository,
            BusinessRuleIncidentRepository incidentRepository,
            ProjectRepository projectRepository) {
        this.impactChainService = impactChainService;
        this.dependencyRepository = dependencyRepository;
        this.businessRuleRepository = businessRuleRepository;
        this.ownershipRepository = ownershipRepository;
        this.incidentRepository = incidentRepository;
        this.projectRepository = projectRepository;
    }
    
    /**
     * Gera grafo visual de impacto sistêmico
     * 
     * @param request Dados do Pull Request
     * @return Grafo completo com nós, arestas e sumário
     */
    public BusinessImpactGraphResponse generateImpactGraph(BusinessImpactRequest request) {
        log.info("🗺️ [GRAPH] Gerando grafo de impacto para PR: {}", request.getPullRequestId());
        
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
        
        // 1. Obter análise de impacto da US#36 (reutiliza tudo)
        BusinessImpactChainResponse chainAnalysis = impactChainService.analyzeImpactChain(request);
        
        // 2. Montar nós do grafo
        List<ImpactGraphNodeResponse> nodes = buildGraphNodes(chainAnalysis);
        log.info("📍 [GRAPH] {} nós criados", nodes.size());
        
        // 3. Montar arestas do grafo
        List<ImpactGraphEdgeResponse> edges = buildGraphEdges(nodes);
        log.info("➡️ [GRAPH] {} arestas criadas", edges.size());
        
        // 4. Gerar sumário executivo
        BusinessImpactGraphResponse.GraphSummary summary = buildGraphSummary(
            chainAnalysis,
            nodes
        );
        
        log.info("✅ [GRAPH] Grafo gerado: {} regras, {} conexões", 
            nodes.size(), edges.size());
        
        BusinessImpactGraphResponse response = new BusinessImpactGraphResponse(
            request.getPullRequestId(),
            nodes,
            edges,
            summary
        );
        
        // US#50: Adicionar contexto de projeto
        response.setProjectContext(project != null 
            ? ProjectContext.scoped(project.getId(), project.getName())
            : ProjectContext.global());
        
        return response;
    }
    
    /**
     * Constrói nós do grafo a partir da análise de cadeia
     */
    private List<ImpactGraphNodeResponse> buildGraphNodes(BusinessImpactChainResponse chainAnalysis) {
        List<ImpactGraphNodeResponse> nodes = new ArrayList<>();
        
        // Adiciona nós DIRECT
        for (ImpactedRuleChainResponse rule : chainAnalysis.getDirectImpacts()) {
            nodes.add(createGraphNode(rule, ImpactGraphNodeResponse.ImpactLevel.DIRECT));
        }
        
        // Adiciona nós INDIRECT
        for (ImpactedRuleChainResponse rule : chainAnalysis.getIndirectImpacts()) {
            nodes.add(createGraphNode(rule, ImpactGraphNodeResponse.ImpactLevel.INDIRECT));
        }
        
        // Adiciona nós CASCADE
        for (ImpactedRuleChainResponse rule : chainAnalysis.getCascadeImpacts()) {
            nodes.add(createGraphNode(rule, ImpactGraphNodeResponse.ImpactLevel.CASCADE));
        }
        
        return nodes;
    }
    
    /**
     * Cria um nó do grafo com todas as informações visuais
     */
    private ImpactGraphNodeResponse createGraphNode(
            ImpactedRuleChainResponse chainRule,
            ImpactGraphNodeResponse.ImpactLevel impactLevel) {
        
        String ruleId = chainRule.getBusinessRuleId();
        
        // Busca regra completa para obter domínio
        Optional<BusinessRule> ruleOpt = businessRuleRepository.findById(ruleId);
        if (ruleOpt.isEmpty()) {
            log.warn("⚠️ [GRAPH] Regra não encontrada: {}", ruleId);
            return null;
        }
        
        BusinessRule rule = ruleOpt.get();
        
        // Converte ownerships para formato simplificado
        List<ImpactGraphNodeResponse.OwnershipInfo> ownerships = 
            chainRule.getOwnerships().stream()
                .map(o -> new ImpactGraphNodeResponse.OwnershipInfo(
                    o.getTeamName(),
                    o.getRole().toString()
                ))
                .collect(Collectors.toList());
        
        // Verifica se tem incidentes históricos
        boolean hasIncidents = checkHasIncidents(ruleId);
        
        return new ImpactGraphNodeResponse(
            ruleId,
            chainRule.getRuleName(),
            rule.getDomain(),
            chainRule.getCriticality(),
            impactLevel,
            ownerships,
            hasIncidents
        );
    }
    
    /**
     * Verifica se regra tem incidentes históricos
     */
    private boolean checkHasIncidents(String ruleId) {
        try {
            UUID ruleUuid = UUID.fromString(ruleId);
            List<BusinessRuleIncident> incidents = 
                incidentRepository.findByBusinessRuleIdOrderByOccurredAtDesc(ruleUuid);
            return !incidents.isEmpty();
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ [GRAPH] ID inválido para verificar incidentes: {}", ruleId);
            return false;
        }
    }
    
    /**
     * Constrói arestas do grafo baseadas em dependências reais
     */
    private List<ImpactGraphEdgeResponse> buildGraphEdges(
            List<ImpactGraphNodeResponse> nodes) {
        
        List<ImpactGraphEdgeResponse> edges = new ArrayList<>();
        Set<String> nodeIds = nodes.stream()
            .map(ImpactGraphNodeResponse::getRuleId)
            .collect(Collectors.toSet());
        
        // Para cada nó do grafo
        for (ImpactGraphNodeResponse node : nodes) {
            String sourceId = node.getRuleId();
            
            // Busca dependências onde este nó é origem
            List<BusinessRuleDependency> dependencies = 
                dependencyRepository.findBySourceRuleId(sourceId);
            
            for (BusinessRuleDependency dep : dependencies) {
                String targetId = dep.getTargetRuleId();
                
                // Só adiciona aresta se o target também está no grafo
                if (nodeIds.contains(targetId)) {
                    edges.add(new ImpactGraphEdgeResponse(
                        sourceId,
                        targetId,
                        dep.getDependencyType()
                    ));
                    
                    log.debug("➡️ Aresta criada: {} -> {} ({})",
                        sourceId, targetId, dep.getDependencyType().getLabel());
                }
            }
        }
        
        return edges;
    }
    
    /**
     * Gera sumário executivo do grafo
     */
    private BusinessImpactGraphResponse.GraphSummary buildGraphSummary(
            BusinessImpactChainResponse chainAnalysis,
            List<ImpactGraphNodeResponse> nodes) {
        
        int direct = chainAnalysis.getDirectImpacts().size();
        int indirect = chainAnalysis.getIndirectImpacts().size();
        int cascade = chainAnalysis.getCascadeImpacts().size();
        int total = direct + indirect + cascade;
        
        // Conta regras críticas
        long criticalCount = nodes.stream()
            .filter(n -> n.getCriticality() == Criticality.CRITICA)
            .count();
        
        // Requer atenção executiva se houver CRITICA em INDIRECT ou CASCADE
        boolean requiresAttention = nodes.stream()
            .anyMatch(n -> n.getCriticality() == Criticality.CRITICA &&
                          (n.getImpactLevel() == ImpactGraphNodeResponse.ImpactLevel.INDIRECT ||
                           n.getImpactLevel() == ImpactGraphNodeResponse.ImpactLevel.CASCADE));
        
        return new BusinessImpactGraphResponse.GraphSummary(
            total,
            direct,
            indirect,
            cascade,
            (int) criticalCount,
            requiresAttention
        );
    }
}
