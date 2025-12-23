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

/**
 * Controller para preferências de alertas por regra de negócio
 * 
 * US#57 - Preferências de Alertas por Projeto e Regra de Negócio
 * 
 * RBAC:
 * - ADMIN: criar/atualizar
 * - ADMIN, RISK_MANAGER: visualizar
 */
@RestController
@RequestMapping("/api/business-rules/{ruleId}/alert-preferences")
@Tag(name = "Preferências de Alertas por Regra", description = "Configuração de preferências de alertas em nível de regra de negócio (override de projeto)")
public class BusinessRuleAlertPreferenceController {

    private static final Logger log = LoggerFactory.getLogger(BusinessRuleAlertPreferenceController.class);

    private final AlertPreferenceService preferenceService;

    public BusinessRuleAlertPreferenceController(AlertPreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Cria ou atualiza preferências de alerta para uma regra de negócio",
        description = """
            Configura preferências de alerta para uma regra específica.
            **Esta preferência tem PRIORIDADE MÁXIMA sobre projeto e defaults.**
            
            **Hierarquia:** Regra > Projeto > Default
            
            **Uso típico:** Silenciar alertas de regras específicas sem afetar o projeto inteiro.
            
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
            @Parameter(description = "ID da regra de negócio") @PathVariable String ruleId,
            @Parameter(description = "Configuração de preferências", content = @Content(
                examples = @ExampleObject(value = """
                    {
                      "minimumSeverity": "CRITICAL",
                      "allowedAlertTypes": ["SYSTEM_DEGRADATION"],
                      "channels": ["TEAMS"],
                      "deliveryWindow": "ANY_TIME"
                    }
                    """)
            ))
            @Valid @RequestBody AlertPreferenceRequest request) {

        log.info("📬 POST /api/business-rules/{}/alert-preferences", ruleId);
        
        AlertPreferenceResponse response = preferenceService.createOrUpdateRulePreference(ruleId, request);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER')")
    @Operation(
        summary = "Busca preferências de alerta de uma regra de negócio",
        description = """
            Retorna as preferências configuradas para a regra.
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
            @Parameter(description = "ID da regra de negócio") @PathVariable String ruleId) {

        log.info("🔍 GET /api/business-rules/{}/alert-preferences", ruleId);
        
        return preferenceService.getRulePreference(ruleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
