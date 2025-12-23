// US#48 - Controller REST para gestão de Projetos
package com.backoffice.alerta.project.api;

import com.backoffice.alerta.project.api.dto.ProjectRequest;
import com.backoffice.alerta.project.api.dto.ProjectResponse;
import com.backoffice.alerta.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * US#48 - Controller REST para Cadastro de Projetos Reais.
 * 
 * Permite criar, atualizar, desativar e consultar projetos organizacionais
 * que servirão como contexto raiz para análises futuras.
 * 
 * IMPORTANTE: Esta funcionalidade NÃO integra com Git.
 * Serve apenas para cadastro de metadados de projetos.
 * 
 * RBAC:
 * - POST/PUT/PATCH: ADMIN apenas
 * - GET (lista): ADMIN, RISK_MANAGER
 * - GET (ID): ADMIN, RISK_MANAGER, ENGINEER
 * - GET /active: Todos autenticados
 */
@RestController
@RequestMapping("/projects")
@Tag(name = "US#48 - Projetos", description = "Cadastro e gerenciamento de projetos organizacionais (contexto de produto)")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Criar novo projeto",
            description = """
                    Cria um novo projeto organizacional no sistema.
                    
                    Projeto representa o contexto organizacional de análise. 
                    Nenhuma integração com código é realizada nesta etapa.
                    
                    Validações:
                    - Nome único obrigatório
                    - Todos os campos de repositório são metadados apenas
                    
                    **Apenas ADMIN pode criar projetos.**
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Projeto criado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou nome duplicado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas ADMIN")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do projeto a criar",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = ProjectRequest.class),
                    examples = @ExampleObject(
                            name = "Exemplo de projeto backend",
                            value = """
                                    {
                                      "name": "Backoffice Pagamentos",
                                      "description": "Sistema de processamento de pagamentos para clientes PJ e PF",
                                      "type": "BACKEND",
                                      "repositoryType": "GITHUB",
                                      "repositoryUrl": "https://github.com/company/payment-backoffice",
                                      "defaultBranch": "main"
                                    }
                                    """
                    )
            )
    )
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest request) {
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER')")
    @Operation(
            summary = "Listar todos os projetos",
            description = """
                    Retorna todos os projetos cadastrados (ativos e inativos).
                    
                    **Acesso:** ADMIN, RISK_MANAGER
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de projetos retornada com sucesso"
            ),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        List<ProjectResponse> projects = projectService.findAllProjects();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER', 'ENGINEER')")
    @Operation(
            summary = "Buscar projeto por ID",
            description = """
                    Retorna detalhes de um projeto específico.
                    
                    **Acesso:** ADMIN, RISK_MANAGER, ENGINEER
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Projeto encontrado",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<ProjectResponse> getProjectById(
            @Parameter(description = "ID do projeto", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id
    ) {
        ProjectResponse response = projectService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @Operation(
            summary = "Listar projetos ativos",
            description = """
                    Retorna apenas projetos com status ativo.
                    
                    Útil para seletores e dropdowns.
                    
                    **Acesso:** Todos autenticados
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de projetos ativos"
            )
    })
    public ResponseEntity<List<ProjectResponse>> getActiveProjects() {
        List<ProjectResponse> projects = projectService.findActiveProjects();
        return ResponseEntity.ok(projects);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Atualizar projeto existente",
            description = """
                    Atualiza dados de um projeto existente.
                    
                    Validações:
                    - Projeto deve existir
                    - Nome único (se alterado)
                    
                    **Apenas ADMIN pode atualizar projetos.**
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Projeto atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou nome duplicado"),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas ADMIN")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Novos dados do projeto",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = ProjectRequest.class),
                    examples = @ExampleObject(
                            name = "Atualização de projeto",
                            value = """
                                    {
                                      "name": "Backoffice Pagamentos V2",
                                      "description": "Sistema modernizado de processamento de pagamentos",
                                      "type": "BACKEND",
                                      "repositoryType": "GITHUB",
                                      "repositoryUrl": "https://github.com/company/payment-backoffice-v2",
                                      "defaultBranch": "main"
                                    }
                                    """
                    )
            )
    )
    public ResponseEntity<ProjectResponse> updateProject(
            @Parameter(description = "ID do projeto", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @Valid @RequestBody ProjectRequest request
    ) {
        ProjectResponse response = projectService.updateProject(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Desativar projeto",
            description = """
                    Desativa um projeto sem deletá-lo fisicamente.
                    
                    Preserva histórico para auditoria e governança.
                    Projeto desativado não aparece na lista de ativos.
                    
                    **Apenas ADMIN pode desativar projetos.**
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Projeto desativado com sucesso"
            ),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas ADMIN")
    })
    public ResponseEntity<Void> deactivateProject(
            @Parameter(description = "ID do projeto", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id
    ) {
        projectService.deactivateProject(id);
        return ResponseEntity.noContent().build();
    }
}
