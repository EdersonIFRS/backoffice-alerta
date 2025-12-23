package com.backoffice.alerta.rag;

import com.backoffice.alerta.project.dto.ProjectContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Response da consulta RAG sobre regras de negócio
 * 
 * US#50 - Campo projectContext para indicar escopo
 * US#63 - Campo ruleScores para transparência do RAG
 */
public class RagQueryResponse {
    
    private String answer;
    private ConfidenceLevel confidence;
    private List<RagSourceReference> sources = new ArrayList<>();
    private List<String> relatedImpacts = new ArrayList<>();
    private List<OwnershipSummary> ownerships = new ArrayList<>();
    private String disclaimer;
    private boolean usedFallback;
    private ProjectContext projectContext;
    
    /**
     * US#63 - Scores e detalhes de ranking das regras retornadas
     * Lista vazia quando não aplicável (nunca null)
     */
    private List<RagRuleScoreDetail> ruleScores = new ArrayList<>();
    
    public RagQueryResponse() {
        this.disclaimer = "⚠️ Esta resposta é baseada exclusivamente em dados reais do sistema. " +
                         "Não constitui decisão executiva. Para ações críticas, consulte os " +
                         "responsáveis técnicos e de negócio.";
    }
    
    public String getAnswer() {
        return answer;
    }
    
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    
    public ConfidenceLevel getConfidence() {
        return confidence;
    }
    
    public void setConfidence(ConfidenceLevel confidence) {
        this.confidence = confidence;
    }
    
    public List<RagSourceReference> getSources() {
        return sources;
    }
    
    public void setSources(List<RagSourceReference> sources) {
        this.sources = sources;
    }
    
    public List<String> getRelatedImpacts() {
        return relatedImpacts;
    }
    
    public void setRelatedImpacts(List<String> relatedImpacts) {
        this.relatedImpacts = relatedImpacts;
    }
    
    public List<OwnershipSummary> getOwnerships() {
        return ownerships;
    }
    
    public void setOwnerships(List<OwnershipSummary> ownerships) {
        this.ownerships = ownerships;
    }
    
    public String getDisclaimer() {
        return disclaimer;
    }
    
    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
    
    public boolean isUsedFallback() {
        return usedFallback;
    }
    
    public void setUsedFallback(boolean usedFallback) {
        this.usedFallback = usedFallback;
    }
    
    public ProjectContext getProjectContext() {
        return projectContext;
    }
    
    public void setProjectContext(ProjectContext projectContext) {
        this.projectContext = projectContext;
    }
    
    public List<RagRuleScoreDetail> getRuleScores() {
        return ruleScores;
    }
    
    public void setRuleScores(List<RagRuleScoreDetail> ruleScores) {
        this.ruleScores = ruleScores;
    }
}
