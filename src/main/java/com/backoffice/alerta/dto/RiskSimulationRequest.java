package com.backoffice.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Requisição para simular cenários hipotéticos em um Pull Request")
public class RiskSimulationRequest {

    @Schema(description = "Identificador único do Pull Request", example = "PR-12345")
    @NotBlank(message = "pullRequestId é obrigatório")
    private String pullRequestId;

    @Schema(
        description = "Versão das regras para análise",
        example = "v2",
        allowableValues = {"v1", "v2"}
    )
    private String ruleVersion;

    @Schema(description = "Lista de arquivos na situação atual")
    @NotEmpty(message = "baseFiles não pode ser vazio")
    @Valid
    private List<PullRequestRequest.FileChange> baseFiles;

    @Schema(description = "Parâmetros da simulação")
    @NotNull(message = "simulation é obrigatório")
    @Valid
    private Simulation simulation;

    public RiskSimulationRequest() {
    }

    public RiskSimulationRequest(String pullRequestId, String ruleVersion, 
                                 List<PullRequestRequest.FileChange> baseFiles, Simulation simulation) {
        this.pullRequestId = pullRequestId;
        this.ruleVersion = ruleVersion;
        this.baseFiles = baseFiles;
        this.simulation = simulation;
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

    public List<PullRequestRequest.FileChange> getBaseFiles() {
        return baseFiles;
    }

    public void setBaseFiles(List<PullRequestRequest.FileChange> baseFiles) {
        this.baseFiles = baseFiles;
    }

    public Simulation getSimulation() {
        return simulation;
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
    }

    @Schema(description = "Parâmetros de simulação")
    public static class Simulation {

        @Schema(
            description = "Se true, simula que todos os arquivos possuem testes",
            example = "true"
        )
        private Boolean applyTests;

        @Schema(
            description = "Se informado, sobrescreve o número de linhas alteradas em todos os arquivos",
            example = "40"
        )
        private Integer overrideLinesChanged;

        public Simulation() {
        }

        public Simulation(Boolean applyTests, Integer overrideLinesChanged) {
            this.applyTests = applyTests;
            this.overrideLinesChanged = overrideLinesChanged;
        }

        public Boolean getApplyTests() {
            return applyTests;
        }

        public void setApplyTests(Boolean applyTests) {
            this.applyTests = applyTests;
        }

        public Integer getOverrideLinesChanged() {
            return overrideLinesChanged;
        }

        public void setOverrideLinesChanged(Integer overrideLinesChanged) {
            this.overrideLinesChanged = overrideLinesChanged;
        }
    }
}
