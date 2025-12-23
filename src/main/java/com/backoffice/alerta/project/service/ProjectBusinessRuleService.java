package com.backoffice.alerta.project.service;

import com.backoffice.alerta.project.domain.Project;
import com.backoffice.alerta.project.domain.ProjectBusinessRule;
import com.backoffice.alerta.project.dto.ProjectBusinessRuleResponse;
import com.backoffice.alerta.project.repository.ProjectBusinessRuleRepository;
import com.backoffice.alerta.project.repository.ProjectRepository;
import com.backoffice.alerta.rules.BusinessRule;
import com.backoffice.alerta.rules.BusinessRuleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * US#49 - Service para gerenciar associações entre Projects e BusinessRules
 * 
 * Responsabilidades:
 * - Associar regras a projetos
 * - Listar regras de um projeto
 * - Remover associações
 * - Validações de integridade
 */
@Service
public class ProjectBusinessRuleService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectBusinessRuleService.class);

    private final ProjectBusinessRuleRepository projectBusinessRuleRepository;
    private final ProjectRepository projectRepository;
    private final BusinessRuleRepository businessRuleRepository;

    public ProjectBusinessRuleService(
            ProjectBusinessRuleRepository projectBusinessRuleRepository,
            ProjectRepository projectRepository,
            BusinessRuleRepository businessRuleRepository) {
        this.projectBusinessRuleRepository = projectBusinessRuleRepository;
        this.projectRepository = projectRepository;
        this.businessRuleRepository = businessRuleRepository;
    }

    /**
     * US#49 - Associa uma BusinessRule a um Project
     * 
     * @param projectId ID do projeto
     * @param businessRuleId ID da regra de negócio
     * @param createdBy Usuário que está criando a associação
     * @return Response com dados da associação
     * @throws EntityNotFoundException Se projeto ou regra não existir
     * @throws IllegalStateException Se associação duplicada
     */
    @Transactional
    public ProjectBusinessRuleResponse associateRuleToProject(
            UUID projectId,
            String businessRuleId,
            String createdBy) {
        
        logger.info("🔗 [US#49] Associando regra {} ao projeto {}", businessRuleId, projectId);

        // 1. Validar que projeto existe
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    logger.error("❌ [US#49] Projeto {} não encontrado", projectId);
                    return new EntityNotFoundException("Projeto não encontrado: " + projectId);
                });

        // 2. Validar que regra existe
        BusinessRule businessRule = businessRuleRepository.findById(businessRuleId)
                .orElseThrow(() -> {
                    logger.error("❌ [US#49] Regra {} não encontrada", businessRuleId);
                    return new EntityNotFoundException("Regra de negócio não encontrada: " + businessRuleId);
                });

        // 3. Validar que associação não existe
        if (projectBusinessRuleRepository.existsByProjectIdAndBusinessRuleId(projectId, businessRuleId)) {
            logger.warn("⚠️ [US#49] Associação duplicada: Projeto {} já possui regra {}", projectId, businessRuleId);
            throw new IllegalStateException(
                String.format("Regra %s já está associada ao projeto %s", businessRule.getName(), project.getName())
            );
        }

        // 4. Criar e persistir associação
        ProjectBusinessRule association = new ProjectBusinessRule(projectId, businessRuleId, createdBy);
        ProjectBusinessRule saved = projectBusinessRuleRepository.save(association);

        logger.info("✅ [US#49] Associação criada: {} -> {}", project.getName(), businessRule.getName());

        // 5. Retornar response
        return buildResponse(saved, project, businessRule);
    }

    /**
     * US#49 - Lista todas as regras associadas a um projeto
     * 
     * @param projectId ID do projeto
     * @return Lista de associações (vazia se projeto sem regras)
     */
    @Transactional(readOnly = true)
    public List<ProjectBusinessRuleResponse> listRulesByProject(UUID projectId) {
        logger.info("🔍 [US#49] Listando regras do projeto {}", projectId);

        // Buscar todas as associações
        List<ProjectBusinessRule> associations = projectBusinessRuleRepository.findByProjectId(projectId);

        if (associations.isEmpty()) {
            logger.info("📭 [US#49] Projeto {} não possui regras associadas", projectId);
            return List.of();
        }

        // Buscar dados completos de projeto e regras
        Project project = projectRepository.findById(projectId).orElse(null);
        
        List<ProjectBusinessRuleResponse> responses = associations.stream()
                .map(assoc -> {
                    BusinessRule rule = businessRuleRepository.findById(assoc.getBusinessRuleId()).orElse(null);
                    return buildResponse(assoc, project, rule);
                })
                .collect(Collectors.toList());

        logger.info("✅ [US#49] Encontradas {} regras para o projeto {}", responses.size(), projectId);
        return responses;
    }

    /**
     * US#49 - Remove associação entre projeto e regra
     * 
     * @param projectId ID do projeto
     * @param businessRuleId ID da regra
     * @throws EntityNotFoundException Se associação não existir
     */
    @Transactional
    public void removeRuleFromProject(UUID projectId, String businessRuleId) {
        logger.info("🗑️ [US#49] Removendo regra {} do projeto {}", businessRuleId, projectId);

        // Buscar associação
        List<ProjectBusinessRule> associations = projectBusinessRuleRepository.findByProjectId(projectId);
        ProjectBusinessRule association = associations.stream()
                .filter(a -> a.getBusinessRuleId().equals(businessRuleId))
                .findFirst()
                .orElseThrow(() -> {
                    logger.error("❌ [US#49] Associação não encontrada: Projeto {} / Regra {}", projectId, businessRuleId);
                    return new EntityNotFoundException(
                        String.format("Associação não encontrada entre projeto %s e regra %s", projectId, businessRuleId)
                    );
                });

        // Remover associação
        projectBusinessRuleRepository.delete(association);
        
        logger.info("✅ [US#49] Associação removida com sucesso");
    }

    /**
     * Constrói response com dados completos
     */
    private ProjectBusinessRuleResponse buildResponse(
            ProjectBusinessRule association,
            Project project,
            BusinessRule businessRule) {
        
        ProjectBusinessRuleResponse response = new ProjectBusinessRuleResponse();
        response.setId(association.getId());
        response.setProjectId(association.getProjectId());
        response.setBusinessRuleId(association.getBusinessRuleId());
        response.setCreatedAt(association.getCreatedAt());
        response.setCreatedBy(association.getCreatedBy());

        if (project != null) {
            response.setProjectName(project.getName());
        }

        if (businessRule != null) {
            response.setBusinessRuleName(businessRule.getName());
            response.setBusinessRuleDescription(businessRule.getDescription());
        }

        return response;
    }
}
