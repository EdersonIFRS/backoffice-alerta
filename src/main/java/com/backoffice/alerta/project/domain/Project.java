// US#48 - Entidade JPA de Projeto Organizacional
package com.backoffice.alerta.project.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * US#48 - Cadastro de Projetos Reais (Contexto de Produto)
 * 
 * Representa um projeto/produto organizacional real que será analisado
 * pelo sistema de gestão de risco.
 * 
 * IMPORTANTE: Esta entidade NÃO integra com Git nesta etapa.
 * Serve apenas como contexto raiz para futuras análises de regras,
 * código, IA e riscos.
 * 
 * Governança:
 * - Não permite delete físico (apenas desativação)
 * - Nome único obrigatório
 * - Histórico preservado via created_at/updated_at
 */
@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProjectType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "repository_type", nullable = false, length = 50)
    private RepositoryType repositoryType;

    @Column(name = "repository_url", nullable = false, length = 500)
    private String repositoryUrl;

    @Column(name = "default_branch", nullable = false, length = 100)
    private String defaultBranch;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // Construtores
    
    public Project() {
        // JPA requer construtor padrão
    }

    public Project(String name, ProjectType type, RepositoryType repositoryType, 
                   String repositoryUrl, String defaultBranch) {
        this.name = name;
        this.type = type;
        this.repositoryType = repositoryType;
        this.repositoryUrl = repositoryUrl;
        this.defaultBranch = defaultBranch;
        this.createdAt = Instant.now();
        this.active = true;
    }

    // Lifecycle callbacks

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Business methods

    /**
     * Desativa o projeto sem deletar fisicamente.
     * Preserva histórico para fins de auditoria e governança.
     */
    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    /**
     * Reativa um projeto previamente desativado.
     */
    public void reactivate() {
        this.active = true;
        this.updatedAt = Instant.now();
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProjectType getType() {
        return type;
    }

    public void setType(ProjectType type) {
        this.type = type;
    }

    public RepositoryType getRepositoryType() {
        return repositoryType;
    }

    public void setRepositoryType(RepositoryType repositoryType) {
        this.repositoryType = repositoryType;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    // equals/hashCode baseado em ID (padrão JPA)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(id, project.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", active=" + active +
                '}';
    }
}
