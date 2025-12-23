package com.backoffice.alerta.code;

import java.time.Instant;
import java.util.UUID;

/**
 * FASE 1: Representa um arquivo de código-fonte indexado no sistema
 * Permite busca semântica em código além de regras de negócio
 */
public class CodeFile {
    
    private String filePath;          // Caminho relativo no repositório
    private UUID projectId;
    private String content;            // Conteúdo completo do arquivo
    private String language;           // java, typescript, python, etc
    private CodeFileType fileType;     // SOURCE, TEST, CONFIG
    private Instant createdAt;
    private Instant updatedAt;

    public CodeFile() {
    }

    public CodeFile(String filePath, UUID projectId, String content, 
                   String language, CodeFileType fileType) {
        this.filePath = filePath;
        this.projectId = projectId;
        this.content = content;
        this.language = language;
        this.fileType = fileType;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public CodeFileType getFileType() {
        return fileType;
    }

    public void setFileType(CodeFileType fileType) {
        this.fileType = fileType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public enum CodeFileType {
        SOURCE,   // Código de produção
        TEST,     // Arquivos de teste
        CONFIG    // Arquivos de configuração
    }
}
