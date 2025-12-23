package com.backoffice.alerta.dto;

import com.backoffice.alerta.project.dto.ProjectContext;

import java.util.List;

/**
 * Response completo com grafo de impacto para visualização
 * 
 * Contém:
 * - Nós (regras de negócio impactadas)
 * - Arestas (dependências entre regras)
 * - Sumário executivo da análise
 * 
 * Formato otimizado para renderização em bibliotecas de grafo como:
 * - react-flow
 * - vis-network
 * - recharts
 * 
 * US#37 - Visualização de Impacto Sistêmico (Mapa de Dependências)
 */
public class BusinessImpactGraphResponse {
    
    private String pullRequestId;
    private List<ImpactGraphNodeResponse> nodes;
    private List<ImpactGraphEdgeResponse> edges;
    private GraphSummary summary;
    private ProjectContext projectContext;
    
    public BusinessImpactGraphResponse() {
    }
    
    public BusinessImpactGraphResponse(String pullRequestId,
                                      List<ImpactGraphNodeResponse> nodes,
                                      List<ImpactGraphEdgeResponse> edges,
                                      GraphSummary summary) {
        this.pullRequestId = pullRequestId;
        this.nodes = nodes;
        this.edges = edges;
        this.summary = summary;
    }
    
    public String getPullRequestId() {
        return pullRequestId;
    }
    
    public void setPullRequestId(String pullRequestId) {
        this.pullRequestId = pullRequestId;
    }
    
    public List<ImpactGraphNodeResponse> getNodes() {
        return nodes;
    }
    
    public void setNodes(List<ImpactGraphNodeResponse> nodes) {
        this.nodes = nodes;
    }
    
    public List<ImpactGraphEdgeResponse> getEdges() {
        return edges;
    }
    
    public void setEdges(List<ImpactGraphEdgeResponse> edges) {
        this.edges = edges;
    }
    
    public GraphSummary getSummary() {
        return summary;
    }
    
    public void setSummary(GraphSummary summary) {
        this.summary = summary;
    }
    
    public ProjectContext getProjectContext() {
        return projectContext;
    }
    
    public void setProjectContext(ProjectContext projectContext) {
        this.projectContext = projectContext;
    }
    
    /**
     * Sumário executivo do grafo de impacto
     */
    public static class GraphSummary {
        private int totalRules;
        private int direct;
        private int indirect;
        private int cascade;
        private int criticalRules;
        private boolean requiresExecutiveAttention;
        
        public GraphSummary() {
        }
        
        public GraphSummary(int totalRules,
                          int direct,
                          int indirect,
                          int cascade,
                          int criticalRules,
                          boolean requiresExecutiveAttention) {
            this.totalRules = totalRules;
            this.direct = direct;
            this.indirect = indirect;
            this.cascade = cascade;
            this.criticalRules = criticalRules;
            this.requiresExecutiveAttention = requiresExecutiveAttention;
        }
        
        public int getTotalRules() {
            return totalRules;
        }
        
        public void setTotalRules(int totalRules) {
            this.totalRules = totalRules;
        }
        
        public int getDirect() {
            return direct;
        }
        
        public void setDirect(int direct) {
            this.direct = direct;
        }
        
        public int getIndirect() {
            return indirect;
        }
        
        public void setIndirect(int indirect) {
            this.indirect = indirect;
        }
        
        public int getCascade() {
            return cascade;
        }
        
        public void setCascade(int cascade) {
            this.cascade = cascade;
        }
        
        public int getCriticalRules() {
            return criticalRules;
        }
        
        public void setCriticalRules(int criticalRules) {
            this.criticalRules = criticalRules;
        }
        
        public boolean isRequiresExecutiveAttention() {
            return requiresExecutiveAttention;
        }
        
        public void setRequiresExecutiveAttention(boolean requiresExecutiveAttention) {
            this.requiresExecutiveAttention = requiresExecutiveAttention;
        }
    }
}
