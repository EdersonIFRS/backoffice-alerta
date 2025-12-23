package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.BusinessRuleRequest;
import com.backoffice.alerta.dto.BusinessRuleResponse;
import com.backoffice.alerta.service.BusinessRuleService;
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
 * Controller REST para gerenciar Regras de Negócio
 */
@RestController
@RequestMapping("/business-rules")
@Tag(name = "Business Rules", description = "API para gerenciar catálogo de regras de negócio")
public class BusinessRuleController {

    private final BusinessRuleService service;

    public BusinessRuleController(BusinessRuleService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
        summary = "Criar nova regra de negócio",
        description = "Cria uma nova regra de negócio no catálogo. O ID deve ser único."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Regra criada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BusinessRuleResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos ou ID duplicado",
            content = @Content(mediaType = "application/json")
        )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Dados da regra de negócio a ser criada",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = BusinessRuleRequest.class),
            examples = @ExampleObject(
                name = "Exemplo de regra de pagamento",
                value = """
                    {
                      "id": "BR-001",
                      "name": "Validação de Limite de Crédito",
                      "domain": "PAYMENT",
                      "description": "Todo pagamento acima de R$ 10.000 deve passar por aprovação manual",
                      "criticality": "ALTA",
                      "owner": "Financeiro"
                    }
                    """
            )
        )
    )
    public ResponseEntity<BusinessRuleResponse> create(@RequestBody BusinessRuleRequest request) {
        try {
            BusinessRuleResponse response = service.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @Operation(
        summary = "Listar todas as regras de negócio",
        description = "Retorna todas as regras de negócio cadastradas no catálogo"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de regras retornada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BusinessRuleResponse.class)
            )
        )
    })
    public ResponseEntity<List<BusinessRuleResponse>> findAll() {
        List<BusinessRuleResponse> rules = service.findAll();
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar regra de negócio por ID",
        description = "Retorna os detalhes de uma regra de negócio específica"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Regra encontrada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BusinessRuleResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Regra não encontrada",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<BusinessRuleResponse> findById(@PathVariable String id) {
        try {
            BusinessRuleResponse response = service.findById(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
