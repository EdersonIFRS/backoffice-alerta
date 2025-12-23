package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resposta com sugestões de testes automatizados")
public class TestSuggestionResponse {

    @Schema(description = "Identificador único do Pull Request", example = "PR-001")
    private String pullRequestId;

    @Schema(description = "Versão das regras utilizada", example = "v2")
    private String ruleVersion;

    @Schema(description = "Lista de recomendações de testes")
    private List<TestRecommendation> suggestedTests;

    @Schema(description = "Impacto esperado total na cobertura (%)", example = "75")
    private Integer expectedCoverageImpact;

    public TestSuggestionResponse() {
    }

    public TestSuggestionResponse(String pullRequestId, String ruleVersion, 
                                 List<TestRecommendation> suggestedTests, Integer expectedCoverageImpact) {
        this.pullRequestId = pullRequestId;
        this.ruleVersion = ruleVersion;
        this.suggestedTests = suggestedTests;
        this.expectedCoverageImpact = expectedCoverageImpact;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public void setPullRequestId(String pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    public String getRuleVersion() {
        return ruleVersion;
    }

    public void setRuleVersion(String ruleVersion) {
        this.ruleVersion = ruleVersion;
    }

    public List<TestRecommendation> getSuggestedTests() {
        return suggestedTests;
    }

    public void setSuggestedTests(List<TestRecommendation> suggestedTests) {
        this.suggestedTests = suggestedTests;
    }

    public Integer getExpectedCoverageImpact() {
        return expectedCoverageImpact;
    }

    public void setExpectedCoverageImpact(Integer expectedCoverageImpact) {
        this.expectedCoverageImpact = expectedCoverageImpact;
    }
}
