package com.backoffice.alerta.alerts.preferences.controller;

import com.backoffice.alerta.alerts.preferences.dto.AlertPreferenceRequest;
import com.backoffice.alerta.alerts.preferences.dto.AlertPreferenceResponse;
import com.backoffice.alerta.alerts.preferences.service.AlertPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller para preferências de alertas por projeto
 * 
 * US#57 - Preferências de Alertas por Projeto e Regra de Negócio
 * 
 * RBAC:
 * - ADMIN: criar/atualizar
 * - ADMIN, RISK_MANAGER: visualizar
 */
@RestController
@RequestMapping("/api/projects/{projectId}/alert-preferences")
@Tag(name = "Preferências de Alertas por Projeto", description = "Configuração de preferências de alertas em nível de projeto")
public class ProjectAlertPreferenceController {

    private static final Logger log = LoggerFactory.getLogger(ProjectAlertPreferenceController.class);

    private final AlertPreferenceService preferenceService;

    public ProjectAlertPreferenceController(AlertPreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Cria ou atualiza preferências de alerta para um projeto",
        description = """
            Configura preferências de alerta para um projeto específico.
            
            **Hierarquia:** Regra > Projeto > Default
            
            **Campos:**
            - minimumSeverity: severidade mínima (INFO, WARNING, CRITICAL)
            - allowedAlertTypes: tipos permitidos (vazio = todos)
            - channels: canais habilitados (SLACK, TEAMS)
            - deliveryWindow: janela de entrega (BUSINESS_HOURS, ANY_TIME)
            
            **RBAC:** Apenas ADMIN
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Preferência criada/atualizada",
            content = @Content(schema = @Schema(implementation = AlertPreferenceResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão (requer ADMIN)")
    })
    public ResponseEntity<AlertPreferenceResponse> createOrUpdate(
            @Parameter(description = "UUID do projeto") @PathVariable UUID projectId,
            @Parameter(description = "Configuração de preferências", content = @Content(
                examples = @ExampleObject(value = """
                    {
                      "minimumSeverity": "WARNING",
                      "allowedAlertTypes": ["HIGH_BLOCK_RATE_PROJECT", "SYSTEM_DEGRADATION"],
                      "channels": ["SLACK"],
                      "deliveryWindow": "BUSINESS_HOURS"
                    }
                    """)
            ))
            @Valid @RequestBody AlertPreferenceRequest request) {

        log.info("📬 POST /api/projects/{}/alert-preferences", projectId);
        
        AlertPreferenceResponse response = preferenceService.createOrUpdateProjectPreference(projectId, request);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER')")
    @Operation(
        summary = "Busca preferências de alerta de um projeto",
        description = """
            Retorna as preferências configuradas para o projeto.
            Se não houver preferências configuradas, retorna 404.
            
            **RBAC:** ADMIN, RISK_MANAGER
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Preferências encontradas",
            content = @Content(schema = @Schema(implementation = AlertPreferenceResponse.class))),
        @ApiResponse(responseCode = "404", description = "Preferências não configuradas"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public ResponseEntity<AlertPreferenceResponse> get(
            @Parameter(description = "UUID do projeto") @PathVariable UUID projectId) {

        log.info("🔍 GET /api/projects/{}/alert-preferences", projectId);
        
        return preferenceService.getProjectPreference(projectId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Atualiza preferências de alerta de um projeto",
        description = """
            Atualiza preferências existentes ou cria novas.
            Equivalente ao POST.
            
            **RBAC:** Apenas ADMIN
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Preferência atualizada",
            content = @Content(schema = @Schema(implementation = AlertPreferenceResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public ResponseEntity<AlertPreferenceResponse> update(
            @Parameter(description = "UUID do projeto") @PathVariable UUID projectId,
            @Valid @RequestBody AlertPreferenceRequest request) {

        log.info("🔄 PUT /api/projects/{}/alert-preferences", projectId);
        
        AlertPreferenceResponse response = preferenceService.createOrUpdateProjectPreference(projectId, request);
        
        return ResponseEntity.ok(response);
    }
}
