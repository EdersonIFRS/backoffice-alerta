package com.backoffice.alerta.onboarding;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * US#72 - Status detalhado do onboarding
 */
@Schema(description = "Status detalhado do processo de onboarding")
public class ProjectOnboardingStatusResponse {

    @Schema(description = "Etapa atual", example = "INDEXING_EMBEDDINGS")
    private String currentStep;

    @Schema(description = "Etapas completadas")
    private List<String> completedSteps = new ArrayList<>();

    @Schema(description = "Etapas pendentes")
    private List<String> pendingSteps = new ArrayList<>();

    @Schema(description = "Última atualização")
    private LocalDateTime lastUpdated;

    public ProjectOnboardingStatusResponse() {}

    public String getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }

    public List<String> getCompletedSteps() {
        return completedSteps;
    }

    public void setCompletedSteps(List<String> completedSteps) {
        this.completedSteps = completedSteps;
    }

    public List<String> getPendingSteps() {
        return pendingSteps;
    }

    public void setPendingSteps(List<String> pendingSteps) {
        this.pendingSteps = pendingSteps;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
