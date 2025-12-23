package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.BusinessRuleIncidentRequest;
import com.backoffice.alerta.dto.BusinessRuleIncidentResponse;
import com.backoffice.alerta.service.BusinessRuleIncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gerenciar incidentes históricos de regras de negócio
 */
@RestController
@RequestMapping("/business-rule-incidents")
@Tag(name = "Business Rule Incidents", description = "API para gerenciar histórico de incidentes de produção")
public class BusinessRuleIncidentController {

    private final BusinessRuleIncidentService service;

    public BusinessRuleIncidentController(BusinessRuleIncidentService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
        summary = "Criar novo incidente",
        description = "Registra um incidente histórico de produção associado a uma regra de negócio. " +
                     "Este histórico será usado para aumentar o risco em análises futuras."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Incidente criado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BusinessRuleIncidentResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos ou regra não encontrada",
            content = @Content(mediaType = "application/json")
        )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Dados do incidente a ser registrado",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = BusinessRuleIncidentRequest.class),
            examples = @ExampleObject(
                name = "Exemplo de incidente crítico",
                value = """
                    {
                      "businessRuleId": "BR-001",
                      "title": "Falha no processamento de pagamento recorrente",
                      "description": "Sistema falhou ao processar pagamentos recorrentes causando perda de receita estimada em R$ 50.000",
                      "severity": "CRITICAL",
                      "occurredAt": "2025-12-15T10:30:00Z"
                    }
                    """
            )
        )
    )
    public ResponseEntity<BusinessRuleIncidentResponse> create(
            @RequestBody BusinessRuleIncidentRequest request) {
        try {
            BusinessRuleIncidentResponse response = service.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @Operation(
        summary = "Listar todos os incidentes",
        description = "Retorna todos os incidentes históricos registrados no sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de incidentes retornada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BusinessRuleIncidentResponse.class)
            )
        )
    })
    public ResponseEntity<List<BusinessRuleIncidentResponse>> findAll() {
        List<BusinessRuleIncidentResponse> incidents = service.findAll();
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/rule/{businessRuleId}")
    @Operation(
        summary = "Buscar incidentes por regra de negócio",
        description = "Retorna todos os incidentes históricos de uma regra de negócio específica"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Incidentes encontrados com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BusinessRuleIncidentResponse.class)
            )
        )
    })
    public ResponseEntity<List<BusinessRuleIncidentResponse>> findByBusinessRuleId(
            @PathVariable String businessRuleId) {
        List<BusinessRuleIncidentResponse> incidents = service.findByBusinessRuleId(businessRuleId);
        return ResponseEntity.ok(incidents);
    }
}
