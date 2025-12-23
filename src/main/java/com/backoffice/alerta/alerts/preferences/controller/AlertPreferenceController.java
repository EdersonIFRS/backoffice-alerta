package com.backoffice.alerta.alerts.preferences.controller;

import com.backoffice.alerta.alerts.preferences.dto.EffectiveAlertPreferenceResponse;
import com.backoffice.alerta.alerts.preferences.service.AlertPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controller para consulta de preferência efetiva (após resolução de hierarquia)
 * 
 * US#57 - Preferências de Alertas por Projeto e Regra de Negócio
 * 
 * HIERARQUIA: Regra > Projeto > Default
 */
@RestController
@RequestMapping("/api/alerts/preferences")
@Tag(name = "Preferências Efetivas de Alertas", description = "Consulta de preferências após aplicar hierarquia (Regra > Projeto > Default)")
public class AlertPreferenceController {

    private static final Logger log = LoggerFactory.getLogger(AlertPreferenceController.class);

    private final AlertPreferenceService preferenceService;

    public AlertPreferenceController(AlertPreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @GetMapping("/effective")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER')")
    @Operation(
        summary = "Consulta preferência efetiva após aplicar hierarquia",
        description = """
            Retorna a preferência efetiva para um contexto (projeto + regra).
            
            **Hierarquia de resolução:**
            1. Preferência da Regra (prioridade máxima)
            2. Preferência do Projeto
            3. Defaults do Sistema
            
            **Parâmetros opcionais:**
            - projectId: contexto do projeto
            - businessRuleId: contexto da regra
            
            **Response inclui:**
            - source: RULE, PROJECT ou DEFAULT
            - valores resolvidos finais
            - contexto enriquecido
            
            **RBAC:** ADMIN, RISK_MANAGER
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Preferência efetiva resolvida",
            content = @Content(
                schema = @Schema(implementation = EffectiveAlertPreferenceResponse.class),
                examples = {
                    @ExampleObject(name = "Resolvida de RULE", value = """
                        {
                          "source": "RULE",
                          "projectId": "550e8400-e29b-41d4-a716-446655440001",
                          "projectName": "Backoffice Pagamentos",
                          "businessRuleId": "REGRA_CALCULO_HORAS_PJ",
                          "minimumSeverity": "CRITICAL",
                          "allowedAlertTypes": ["SYSTEM_DEGRADATION"],
                          "channels": ["TEAMS"],
                          "deliveryWindow": "ANY_TIME"
                        }
                        """),
                    @ExampleObject(name = "Resolvida de PROJECT", value = """
                        {
                          "source": "PROJECT",
                          "projectId": "550e8400-e29b-41d4-a716-446655440001",
                          "projectName": "Backoffice Pagamentos",
                          "minimumSeverity": "WARNING",
                          "allowedAlertTypes": [],
                          "channels": ["SLACK", "TEAMS"],
                          "deliveryWindow": "BUSINESS_HOURS"
                        }
                        """),
                    @ExampleObject(name = "Defaults do sistema", value = """
                        {
                          "source": "DEFAULT",
                          "minimumSeverity": "INFO",
                          "allowedAlertTypes": [],
                          "channels": ["SLACK", "TEAMS"],
                          "deliveryWindow": "ANY_TIME"
                        }
                        """)
                }
            )
        ),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public ResponseEntity<EffectiveAlertPreferenceResponse> getEffectivePreference(
            @Parameter(description = "UUID do projeto (opcional)") 
            @RequestParam(required = false) UUID projectId,
            
            @Parameter(description = "ID da regra de negócio (opcional)")
            @RequestParam(required = false) String businessRuleId) {

        log.info("🔍 GET /api/alerts/preferences/effective - projectId: {}, businessRuleId: {}", 
                 projectId, businessRuleId);
        
        EffectiveAlertPreferenceResponse response = preferenceService.resolveEffectivePreference(
            projectId, 
            businessRuleId
        );
        
        return ResponseEntity.ok(response);
    }
}
