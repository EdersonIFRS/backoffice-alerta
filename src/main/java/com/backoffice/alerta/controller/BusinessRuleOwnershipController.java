package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.BusinessRuleOwnershipRequest;
import com.backoffice.alerta.dto.BusinessRuleOwnershipResponse;
import com.backoffice.alerta.rules.OwnershipRole;
import com.backoffice.alerta.rules.TeamType;
import com.backoffice.alerta.service.BusinessRuleOwnershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller para gerenciar ownership organizacional de regras de negócio
 * 
 * US#26 - Ownership Organizacional por Regra de Negócio
 * 
 * Permite atribuir responsabilidade (ownership) de regras a times/equipes,
 * habilitando governança, rastreabilidade e futuras aprovações.
 */
@RestController
@RequestMapping("/business-rule-ownerships")
@Tag(name = "Business Rule Ownership", description = "Gerenciamento de ownership organizacional de regras de negócio")
public class BusinessRuleOwnershipController {

    private final BusinessRuleOwnershipService ownershipService;

    public BusinessRuleOwnershipController(BusinessRuleOwnershipService ownershipService) {
        this.ownershipService = ownershipService;
    }

    @PostMapping
    @Operation(
        summary = "Criar ownership organizacional",
        description = """
            Atribui ownership (responsabilidade) de uma regra de negócio a um time/equipe organizacional.
            
            **Regras de Validação:**
            - Regra de negócio deve existir
            - Apenas 1 PRIMARY_OWNER por regra (outros roles permitem múltiplos)
            - Campos obrigatórios: businessRuleId, teamName, teamType, role, contactEmail
            
            **Roles Disponíveis:**
            - PRIMARY_OWNER: Responsável direto pela regra (único)
            - SECONDARY_OWNER: Notificação e suporte (múltiplos permitidos)
            - BACKUP: Responsável em caso de ausência do primary (múltiplos permitidos)
            """,
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Ownership criado com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BusinessRuleOwnershipResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Dados inválidos (regra não existe, campos obrigatórios faltando)"
            ),
            @ApiResponse(
                responseCode = "409",
                description = "Conflito - já existe PRIMARY_OWNER para esta regra"
            )
        }
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Dados do ownership a ser criado",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = BusinessRuleOwnershipRequest.class),
            examples = {
                @ExampleObject(
                    name = "Ownership Financeiro PRIMARY",
                    description = "Time de finanças como responsável primário por regra de pagamento",
                    value = """
                        {
                          "businessRuleId": "550e8400-e29b-41d4-a716-446655440000",
                          "teamName": "Time de Pagamentos",
                          "teamType": "FINANCE",
                          "role": "PRIMARY_OWNER",
                          "contactEmail": "payments-team@company.com",
                          "approvalRequired": true
                        }
                        """
                ),
                @ExampleObject(
                    name = "Ownership Técnico SECONDARY",
                    description = "Time de engenharia como suporte secundário",
                    value = """
                        {
                          "businessRuleId": "550e8400-e29b-41d4-a716-446655440000",
                          "teamName": "Engenharia de Pagamentos",
                          "teamType": "ENGINEERING",
                          "role": "SECONDARY_OWNER",
                          "contactEmail": "payments-eng@company.com",
                          "approvalRequired": false
                        }
                        """
                ),
                @ExampleObject(
                    name = "Regra Crítica com Múltiplos Owners",
                    description = "Regra de segurança com aprovação obrigatória do time de security",
                    value = """
                        {
                          "businessRuleId": "123e4567-e89b-12d3-a456-426614174000",
                          "teamName": "Segurança da Informação",
                          "teamType": "SECURITY",
                          "role": "PRIMARY_OWNER",
                          "contactEmail": "security@company.com",
                          "approvalRequired": true
                        }
                        """
                )
            }
        )
    )
    public ResponseEntity<BusinessRuleOwnershipResponse> createOwnership(
            @RequestBody BusinessRuleOwnershipRequest request) {
        
        try {
            BusinessRuleOwnershipResponse response = ownershipService.createOwnership(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            // Regra não existe ou campos obrigatórios faltando
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            // PRIMARY_OWNER duplicado
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping
    @Operation(
        summary = "Listar todos os ownerships",
        description = "Retorna todos os ownerships organizacionais cadastrados no sistema",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Lista de ownerships retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BusinessRuleOwnershipResponse.class)
                )
            )
        }
    )
    public ResponseEntity<List<BusinessRuleOwnershipResponse>> listAllOwnerships() {
        List<BusinessRuleOwnershipResponse> ownerships = ownershipService.listAllOwnerships();
        return ResponseEntity.ok(ownerships);
    }

    @GetMapping("/rule/{businessRuleId}")
    @Operation(
        summary = "Listar ownerships de uma regra",
        description = "Retorna todos os ownerships (PRIMARY, SECONDARY, BACKUP) de uma regra específica",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Ownerships da regra retornados com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BusinessRuleOwnershipResponse.class)
                )
            )
        }
    )
    public ResponseEntity<List<BusinessRuleOwnershipResponse>> listOwnershipsByRule(
            @Parameter(description = "ID da regra de negócio", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID businessRuleId) {
        
        List<BusinessRuleOwnershipResponse> ownerships = ownershipService.listOwnershipsByRule(businessRuleId);
        return ResponseEntity.ok(ownerships);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Remover ownership",
        description = "Remove um ownership organizacional pelo ID",
        responses = {
            @ApiResponse(
                responseCode = "204",
                description = "Ownership removido com sucesso"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Ownership não encontrado"
            )
        }
    )
    public ResponseEntity<Void> deleteOwnership(
            @Parameter(description = "ID do ownership", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        
        boolean deleted = ownershipService.deleteOwnership(id);
        
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
