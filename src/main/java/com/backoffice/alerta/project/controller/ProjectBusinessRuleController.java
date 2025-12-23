package com.backoffice.alerta.project.controller;

import com.backoffice.alerta.project.dto.AssociateRuleRequest;
import com.backoffice.alerta.project.dto.ProjectBusinessRuleResponse;
import com.backoffice.alerta.project.service.ProjectBusinessRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * US#49 - Controller REST para associações entre Projects e BusinessRules
 * 
 * Endpoints:
 * - POST   /api/projects/{projectId}/business-rules        (ADMIN, RISK_MANAGER)
 * - GET    /api/projects/{projectId}/business-rules        (ADMIN, RISK_MANAGER, ENGINEER)
 * - DELETE /api/projects/{projectId}/business-rules/{ruleId} (ADMIN)
 */
@RestController
@RequestMapping("/api/projects/{projectId}/business-rules")
@Tag(
    name = "US#49 - Project Business Rules",
    description = "Gerenciamento de associações entre Projetos e Regras de Negócio"
)
@SecurityRequirement(name = "bearer-jwt")
public class ProjectBusinessRuleController {

    private final ProjectBusinessRuleService projectBusinessRuleService;

    public ProjectBusinessRuleController(ProjectBusinessRuleService projectBusinessRuleService) {
        this.projectBusinessRuleService = projectBusinessRuleService;
    }

    /**
     * POST /api/projects/{projectId}/business-rules
     * Associa uma BusinessRule a um Project
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER')")
    @Operation(
        summary = "Associar regra de negócio a projeto",
        description = """
            **US#49** - Cria uma associação entre um projeto organizacional e uma regra de negócio.
            
            **Regras:**
            - Projeto deve existir
            - Regra de negócio deve existir
            - Não permite duplicação
            - Auditoria via JWT (createdBy)
            
            **Impacto:**
            - RAG passa a considerar apenas regras do projeto
            - Chat contextualizado ao projeto
            - Impact Analysis filtrada por projeto
            
            **RBAC:** ADMIN, RISK_MANAGER
            """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados da regra a ser associada",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AssociateRuleRequest.class),
                examples = @ExampleObject(
                    name = "Associar Regra Crítica",
                    summary = "Associar regra de cálculo ao projeto backend",
                    value = """
                        {
                          "businessRuleId": "550e8400-e29b-41d4-a716-446655440001"
                        }
                        """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Associação criada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProjectBusinessRuleResponse.class),
                examples = @ExampleObject(
                    name = "Sucesso",
                    value = """
                        {
                          "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
                          "projectId": "550e8400-e29b-41d4-a716-446655440010",
                          "projectName": "Backoffice Pagamentos",
                          "businessRuleId": "550e8400-e29b-41d4-a716-446655440001",
                          "businessRuleName": "REGRA_CALCULO_HORAS_PJ",
                          "businessRuleDescription": "Cálculo de Horas PJ",
                          "createdAt": "2024-12-19T22:30:00Z",
                          "createdBy": "admin"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Associação duplicada",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "error": "Regra REGRA_CALCULO_HORAS_PJ já está associada ao projeto Backoffice Pagamentos"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acesso negado - requer ADMIN ou RISK_MANAGER"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Projeto ou regra não encontrados",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "error": "Projeto não encontrado: 550e8400-e29b-41d4-a716-446655440099"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ProjectBusinessRuleResponse> associateRule(
            @Parameter(description = "ID do projeto", required = true, example = "550e8400-e29b-41d4-a716-446655440010")
            @PathVariable UUID projectId,
            @Valid @RequestBody AssociateRuleRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        
        ProjectBusinessRuleResponse response = projectBusinessRuleService.associateRuleToProject(
            projectId,
            request.getBusinessRuleId(),
            username
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/projects/{projectId}/business-rules
     * Lista todas as regras associadas ao projeto
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER', 'ENGINEER')")
    @Operation(
        summary = "Listar regras do projeto",
        description = """
            **US#49** - Lista todas as regras de negócio associadas a um projeto específico.
            
            **Retorno:**
            - Lista vazia se projeto sem regras
            - Ordenação por data de associação
            - Inclui dados completos (projeto + regra)
            
            **Uso:**
            - RAG: buscar regras relevantes do projeto
            - Chat: contexto de conversação
            - Dashboard: métricas por projeto
            
            **RBAC:** ADMIN, RISK_MANAGER, ENGINEER
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de regras retornada (pode ser vazia)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProjectBusinessRuleResponse.class),
                examples = @ExampleObject(
                    name = "Projeto com 2 regras",
                    value = """
                        [
                          {
                            "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
                            "projectId": "550e8400-e29b-41d4-a716-446655440010",
                            "projectName": "Backoffice Pagamentos",
                            "businessRuleId": "550e8400-e29b-41d4-a716-446655440001",
                            "businessRuleName": "REGRA_CALCULO_HORAS_PJ",
                            "businessRuleDescription": "Cálculo de Horas PJ",
                            "createdAt": "2024-12-19T22:30:00Z",
                            "createdBy": "admin"
                          },
                          {
                            "id": "8d0f7780-8536-51ef-b18d-f18ed2e01bf8",
                            "projectId": "550e8400-e29b-41d4-a716-446655440010",
                            "projectName": "Backoffice Pagamentos",
                            "businessRuleId": "550e8400-e29b-41d4-a716-446655440003",
                            "businessRuleName": "REGRA_CALCULO_TRIBUTOS",
                            "businessRuleDescription": "Cálculo de Tributos",
                            "createdAt": "2024-12-19T22:31:00Z",
                            "createdBy": "admin"
                          }
                        ]
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acesso negado"
        )
    })
    public ResponseEntity<List<ProjectBusinessRuleResponse>> listRules(
            @Parameter(description = "ID do projeto", required = true, example = "550e8400-e29b-41d4-a716-446655440010")
            @PathVariable UUID projectId) {

        List<ProjectBusinessRuleResponse> rules = projectBusinessRuleService.listRulesByProject(projectId);
        return ResponseEntity.ok(rules);
    }

    /**
     * DELETE /api/projects/{projectId}/business-rules/{businessRuleId}
     * Remove associação entre projeto e regra
     */
    @DeleteMapping("/{businessRuleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Remover associação entre projeto e regra",
        description = """
            **US#49** - Remove a associação entre um projeto e uma regra de negócio.
            
            **Atenção:**
            - Remove apenas a associação (soft delete)
            - Não remove a regra de negócio
            - Não remove o projeto
            - Não recalcula riscos
            - Não cria auditoria
            
            **Impacto:**
            - Regra deixa de aparecer em buscas RAG do projeto
            - Chat não considera mais a regra neste projeto
            - Impact Analysis ignora a regra
            
            **RBAC:** ADMIN (somente)
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Associação removida com sucesso"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acesso negado - requer ADMIN"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Associação não encontrada",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "error": "Associação não encontrada entre projeto 550e8400-e29b-41d4-a716-446655440010 e regra 550e8400-e29b-41d4-a716-446655440001"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<Void> removeRule(
            @Parameter(description = "ID do projeto", required = true, example = "550e8400-e29b-41d4-a716-446655440010")
            @PathVariable UUID projectId,
            @Parameter(description = "ID da regra de negócio", required = true, example = "550e8400-e29b-41d4-a716-446655440001")
            @PathVariable String businessRuleId) {

        projectBusinessRuleService.removeRuleFromProject(projectId, businessRuleId);
        return ResponseEntity.noContent().build();
    }
}
