package com.backoffice.alerta.rag;

/**
 * Detalhe de score e ranking de uma regra retornada pelo RAG
 * 
 * US#63 - Score de Similaridade Visível no RAG
 * 
 * Expõe explicitamente:
 * - Score de similaridade semântica (0.0 - 1.0)
 * - Score de match por palavras-chave (quantidade de matches)
 * - Tipo de match (SEMANTIC, KEYWORD, HYBRID, FALLBACK)
 * - Posição final no ranking
 * - Se foi incluída por fallback
 * 
 * Princípios:
 * - READ-ONLY (não altera decisões)
 * - Determinístico (sem IA)
 * - Auditável e explicável
 * - Backward compatible (campo opcional em RagQueryResponse)
 */
public class RagRuleScoreDetail {
    
    private String businessRuleId;
    private String businessRuleName;
    private RagMatchType matchType;
    
    /**
     * Score de similaridade semântica (cosine similarity)
     * 0.0 - 1.0
     * 0.0 se não aplicável (ex: apenas KEYWORD)
     */
    private double semanticScore;
    
    /**
     * Quantidade de termos da query que deram match na regra
     * 0 se não aplicável (ex: apenas SEMANTIC)
     */
    private int keywordScore;
    
    /**
     * Posição no ranking final após merge + re-ranking
     * 1 = mais relevante
     */
    private int finalRankPosition;
    
    /**
     * Se a regra foi incluída por fallback (threshold não atingido)
     */
    private boolean includedByFallback;
    
    public RagRuleScoreDetail() {
    }
    
    public RagRuleScoreDetail(String businessRuleId, String businessRuleName, 
                              RagMatchType matchType, double semanticScore, 
                              int keywordScore, int finalRankPosition, 
                              boolean includedByFallback) {
        this.businessRuleId = businessRuleId;
        this.businessRuleName = businessRuleName;
        this.matchType = matchType;
        this.semanticScore = semanticScore;
        this.keywordScore = keywordScore;
        this.finalRankPosition = finalRankPosition;
        this.includedByFallback = includedByFallback;
    }
    
    public String getBusinessRuleId() {
        return businessRuleId;
    }
    
    public void setBusinessRuleId(String businessRuleId) {
        this.businessRuleId = businessRuleId;
    }
    
    public String getBusinessRuleName() {
        return businessRuleName;
    }
    
    public void setBusinessRuleName(String businessRuleName) {
        this.businessRuleName = businessRuleName;
    }
    
    public RagMatchType getMatchType() {
        return matchType;
    }
    
    public void setMatchType(RagMatchType matchType) {
        this.matchType = matchType;
    }
    
    public double getSemanticScore() {
        return semanticScore;
    }
    
    public void setSemanticScore(double semanticScore) {
        this.semanticScore = semanticScore;
    }
    
    public int getKeywordScore() {
        return keywordScore;
    }
    
    public void setKeywordScore(int keywordScore) {
        this.keywordScore = keywordScore;
    }
    
    public int getFinalRankPosition() {
        return finalRankPosition;
    }
    
    public void setFinalRankPosition(int finalRankPosition) {
        this.finalRankPosition = finalRankPosition;
    }
    
    public boolean isIncludedByFallback() {
        return includedByFallback;
    }
    
    public void setIncludedByFallback(boolean includedByFallback) {
        this.includedByFallback = includedByFallback;
    }
}
