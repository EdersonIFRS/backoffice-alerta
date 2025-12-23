// US#48 - Service para gestão de Projetos
package com.backoffice.alerta.project.service;

import com.backoffice.alerta.project.api.dto.ProjectRequest;
import com.backoffice.alerta.project.api.dto.ProjectResponse;
import com.backoffice.alerta.project.domain.Project;
import com.backoffice.alerta.project.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * US#48 - Service para gestão de Projetos Organizacionais.
 * 
 * Responsabilidades:
 * - Criar, atualizar, desativar projetos
 * - Validar unicidade de nome
 * - Buscar projetos ativos e por ID
 * - Garantir que não há delete físico
 * 
 * Governança:
 * - Nenhuma integração com Git
 * - Apenas gestão de metadados
 * - Histórico preservado
 */
@Service
@Transactional
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /**
     * Cria um novo projeto organizacional.
     * 
     * Validações:
     * - Nome único (case-insensitive)
     * - Todos os campos obrigatórios preenchidos
     * 
     * @param request Dados do projeto
     * @return Projeto criado
     * @throws IllegalStateException se nome já existe
     */
    public ProjectResponse createProject(ProjectRequest request) {
        logger.info("📦 [US#48] Criando projeto: {}", request.getName());

        // Validar unicidade de nome
        validateUniqueName(request.getName(), null);

        Project project = new Project(
                request.getName(),
                request.getType(),
                request.getRepositoryType(),
                request.getRepositoryUrl(),
                request.getDefaultBranch()
        );
        project.setDescription(request.getDescription());

        Project saved = projectRepository.save(project);

        logger.info("✅ [US#48] Projeto criado com sucesso: {} (ID: {})", saved.getName(), saved.getId());

        return ProjectResponse.fromEntity(saved);
    }

    /**
     * Atualiza um projeto existente.
     * 
     * Validações:
     * - Projeto deve existir
     * - Nome único (se alterado)
     * 
     * @param id ID do projeto
     * @param request Novos dados
     * @return Projeto atualizado
     * @throws EntityNotFoundException se projeto não existe
     * @throws IllegalStateException se novo nome já existe
     */
    public ProjectResponse updateProject(UUID id, ProjectRequest request) {
        logger.info("🔄 [US#48] Atualizando projeto: {}", id);

        Project project = findProjectById(id);

        // Validar unicidade de nome (se alterado)
        if (!project.getName().equalsIgnoreCase(request.getName())) {
            validateUniqueName(request.getName(), id);
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setType(request.getType());
        project.setRepositoryType(request.getRepositoryType());
        project.setRepositoryUrl(request.getRepositoryUrl());
        project.setDefaultBranch(request.getDefaultBranch());

        Project updated = projectRepository.save(project);

        logger.info("✅ [US#48] Projeto atualizado: {}", updated.getName());

        return ProjectResponse.fromEntity(updated);
    }

    /**
     * Desativa um projeto sem deletá-lo fisicamente.
     * 
     * Preserva histórico para auditoria e governança.
     * 
     * @param id ID do projeto
     * @throws EntityNotFoundException se projeto não existe
     */
    public void deactivateProject(UUID id) {
        logger.info("⏸️  [US#48] Desativando projeto: {}", id);

        Project project = findProjectById(id);
        project.deactivate();

        projectRepository.save(project);

        logger.info("✅ [US#48] Projeto desativado: {}", project.getName());
    }

    /**
     * Busca todos os projetos ativos.
     * 
     * @return Lista de projetos com active=true
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> findActiveProjects() {
        logger.debug("🔍 [US#48] Buscando projetos ativos");

        return projectRepository.findByActiveTrue().stream()
                .map(ProjectResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Busca todos os projetos (ativos e inativos).
     * 
     * @return Lista completa de projetos
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> findAllProjects() {
        logger.debug("🔍 [US#48] Buscando todos os projetos");

        return projectRepository.findAll().stream()
                .map(ProjectResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Busca projeto por ID.
     * 
     * @param id ID do projeto
     * @return Projeto encontrado
     * @throws EntityNotFoundException se não existe
     */
    @Transactional(readOnly = true)
    public ProjectResponse findById(UUID id) {
        logger.debug("🔍 [US#48] Buscando projeto por ID: {}", id);

        Project project = findProjectById(id);
        return ProjectResponse.fromEntity(project);
    }

    // Métodos auxiliares privados

    /**
     * Busca entidade Project por ID.
     * 
     * @throws EntityNotFoundException se não existe
     */
    private Project findProjectById(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("❌ [US#48] Projeto não encontrado: {}", id);
                    return new EntityNotFoundException("Projeto não encontrado: " + id);
                });
    }

    /**
     * Valida se nome de projeto é único.
     * 
     * @param name Nome a validar
     * @param excludeId ID a excluir da validação (para updates)
     * @throws IllegalStateException se nome já existe
     */
    private void validateUniqueName(String name, UUID excludeId) {
        projectRepository.findByNameIgnoreCase(name).ifPresent(existing -> {
            // Se é update e o projeto é o mesmo, OK
            if (excludeId != null && existing.getId().equals(excludeId)) {
                return;
            }

            logger.error("❌ [US#48] Nome de projeto já existe: {}", name);
            throw new IllegalStateException(
                    "Já existe um projeto com o nome '" + name + "'. Os nomes devem ser únicos."
            );
        });
    }
}
