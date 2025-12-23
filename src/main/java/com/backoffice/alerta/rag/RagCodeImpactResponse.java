package com.backoffice.alerta.rag;

import com.backoffice.alerta.ast.ASTImpactDetail;
import com.backoffice.alerta.project.dto.ProjectContext;
import com.backoffice.alerta.rules.Criticality;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO para consulta RAG de impacto no código
 * 
 * US#45 - RAG com Mapeamento de Código Impactado
 * US#50 - Campo projectContext para indicar escopo
 * US#63 - Campo ruleScores para transparência do RAG
 * 
 * Retorna arquivos impactados, dependências e ownership
 */
@Schema(description = "Resposta com análise de impacto no código")
public class RagCodeImpactResponse {
    
    @Schema(description = "Resposta gerada pela IA ou fallback determinístico")
    private String answer;
    
    @Schema(description = "Nível de confiança da resposta")
    private ConfidenceLevel confidence;
    
    @Schema(description = "Regras de negócio impactadas")
    private List<ImpactedRuleInfo> impactedRules = new ArrayList<>();
    
    @Schema(description = "Arquivos de código impactados")
    private List<ImpactedFileInfo> impactedFiles = new ArrayList<>();
    
    @Schema(description = "Análise de impacto de dependências")
    private DependencyImpact dependencyImpact;
    
    @Schema(description = "Times e responsáveis")
    private List<OwnershipInfo> ownerships = new ArrayList<>();
    
    @Schema(description = "Disclaimer de responsabilidade")
    private String disclaimer;
    
    @Schema(description = "Se usou resposta de fallback (IA indisponível)")
    private boolean usedFallback;
    
    @Schema(description = "Contexto de escopo de projeto (se aplicável)")
    private ProjectContext projectContext;
    
    /**
     * US#63 - Scores e detalhes de ranking das regras retornadas
     * Lista vazia quando não aplicável (nunca null)
     */
    @Schema(description = "Scores de similaridade e detalhes de ranking do RAG")
    private List<RagRuleScoreDetail> ruleScores = new ArrayList<>();
    
    /**
     * US#69 - Detalhes de impacto a nível de AST (métodos, classes)
     * Lista vazia quando análise AST não aplicável (nunca null)
     */
    @Schema(description = "Detalhes de impacto a nível de AST (métodos, classes)")
    private List<ASTImpactDetail> astDetails = new ArrayList<>();
    
    // Inner classes
    @Schema(description = "Informações de regra impactada")
    public static class ImpactedRuleInfo {
        private String ruleId;
        private String ruleName;
        private Criticality criticality;
        private boolean hasIncidents;
        
        public ImpactedRuleInfo() {}
        
        public ImpactedRuleInfo(String ruleId, String ruleName, Criticality criticality, boolean hasIncidents) {
            this.ruleId = ruleId;
            this.ruleName = ruleName;
            this.criticality = criticality;
            this.hasIncidents = hasIncidents;
        }
        
        // Getters and Setters
        public String getRuleId() { return ruleId; }
        public void setRuleId(String ruleId) { this.ruleId = ruleId; }
        
        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        
        public Criticality getCriticality() { return criticality; }
        public void setCriticality(Criticality criticality) { this.criticality = criticality; }
        
        public boolean isHasIncidents() { return hasIncidents; }
        public void setHasIncidents(boolean hasIncidents) { this.hasIncidents = hasIncidents; }
    }
    
    @Schema(description = "Informações de arquivo impactado")
    public static class ImpactedFileInfo {
        private String filePath;
        private String reason;
        private String riskLevel;
        
        public ImpactedFileInfo() {}
        
        public ImpactedFileInfo(String filePath, String reason, String riskLevel) {
            this.filePath = filePath;
            this.reason = reason;
            this.riskLevel = riskLevel;
        }
        
        // Getters and Setters
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    }
    
    @Schema(description = "Análise de impacto de dependências")
    public static class DependencyImpact {
        private int direct;
        private int indirect;
        private int cascade;
        
        public DependencyImpact() {}
        
        public DependencyImpact(int direct, int indirect, int cascade) {
            this.direct = direct;
            this.indirect = indirect;
            this.cascade = cascade;
        }
        
        // Getters and Setters
        public int getDirect() { return direct; }
        public void setDirect(int direct) { this.direct = direct; }
        
        public int getIndirect() { return indirect; }
        public void setIndirect(int indirect) { this.indirect = indirect; }
        
        public int getCascade() { return cascade; }
        public void setCascade(int cascade) { this.cascade = cascade; }
    }
    
    @Schema(description = "Informações de ownership")
    public static class OwnershipInfo {
        private String teamName;
        private String role;
        private String contactEmail;
        
        public OwnershipInfo() {}
        
        public OwnershipInfo(String teamName, String role, String contactEmail) {
            this.teamName = teamName;
            this.role = role;
            this.contactEmail = contactEmail;
        }
        
        // Getters and Setters
        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public String getContactEmail() { return contactEmail; }
        public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    }
    
    // Main class Getters and Setters
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    
    public ConfidenceLevel getConfidence() { return confidence; }
    public void setConfidence(ConfidenceLevel confidence) { this.confidence = confidence; }
    
    public List<ImpactedRuleInfo> getImpactedRules() { return impactedRules; }
    public void setImpactedRules(List<ImpactedRuleInfo> impactedRules) { this.impactedRules = impactedRules; }
    
    public List<ImpactedFileInfo> getImpactedFiles() { return impactedFiles; }
    public void setImpactedFiles(List<ImpactedFileInfo> impactedFiles) { this.impactedFiles = impactedFiles; }
    
    public DependencyImpact getDependencyImpact() { return dependencyImpact; }
    public void setDependencyImpact(DependencyImpact dependencyImpact) { this.dependencyImpact = dependencyImpact; }
    
    public List<OwnershipInfo> getOwnerships() { return ownerships; }
    public void setOwnerships(List<OwnershipInfo> ownerships) { this.ownerships = ownerships; }
    
    public String getDisclaimer() { return disclaimer; }
    public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }
    
    public boolean isUsedFallback() { return usedFallback; }
    public void setUsedFallback(boolean usedFallback) { this.usedFallback = usedFallback; }
    
    public ProjectContext getProjectContext() { return projectContext; }
    public void setProjectContext(ProjectContext projectContext) { this.projectContext = projectContext; }
    
    public List<RagRuleScoreDetail> getRuleScores() { return ruleScores; }
    public void setRuleScores(List<RagRuleScoreDetail> ruleScores) { this.ruleScores = ruleScores; }
    
    public List<ASTImpactDetail> getAstDetails() { return astDetails; }
    public void setAstDetails(List<ASTImpactDetail> astDetails) { this.astDetails = astDetails; }
}
