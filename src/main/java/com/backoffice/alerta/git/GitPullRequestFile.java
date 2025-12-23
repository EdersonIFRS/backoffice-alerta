package com.backoffice.alerta.git;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * US#51 - Arquivo alterado em Pull Request
 * 
 * Representa um arquivo modificado no PR.
 * Usado para extrair lista de arquivos para análise de impacto.
 */
@Schema(description = "Arquivo alterado no Pull Request")
public class GitPullRequestFile {
    
    @Schema(description = "Caminho do arquivo", example = "src/main/java/com/backoffice/Payment.java")
    private String filePath;
    
    @Schema(description = "Tipo de alteração", example = "MODIFIED", allowableValues = {"ADDED", "MODIFIED", "DELETED"})
    private String changeType;
    
    public GitPullRequestFile() {}
    
    public GitPullRequestFile(String filePath, String changeType) {
        this.filePath = filePath;
        this.changeType = changeType;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public String getChangeType() {
        return changeType;
    }
    
    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }
}
