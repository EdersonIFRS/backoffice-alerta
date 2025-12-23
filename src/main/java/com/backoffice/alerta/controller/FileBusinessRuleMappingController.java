package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.FileBusinessRuleMappingRequest;
import com.backoffice.alerta.dto.FileBusinessRuleMappingResponse;
import com.backoffice.alerta.service.FileBusinessRuleMappingService;
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
 * Controller REST para gerenciar mapeamentos entre arquivos e regras de negócio
 */
@RestController
@RequestMapping("/business-rule-mappings")
@Tag(name = "Business Rule Mappings", description = "API para mapear arquivos às regras de negócio que implementam")
public class FileBusinessRuleMappingController {

    private final FileBusinessRuleMappingService service;

    public FileBusinessRuleMappingController(FileBusinessRuleMappingService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
        summary = "Criar novo mapeamento",
        description = "Mapeia um arquivo a uma regra de negócio. A regra deve existir e o mapeamento não pode ser duplicado."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Mapeamento criado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FileBusinessRuleMappingResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos, regra não existe ou mapeamento duplicado",
            content = @Content(mediaType = "application/json")
        )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Dados do mapeamento a ser criado",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = FileBusinessRuleMappingRequest.class),
            examples = @ExampleObject(
                name = "Exemplo de mapeamento direto",
                value = """
                    {
                      "filePath": "src/main/java/com/app/payment/PaymentService.java",
                      "businessRuleId": "BR-001",
                      "impactType": "DIRECT"
                    }
                    """
            )
        )
    )
    public ResponseEntity<FileBusinessRuleMappingResponse> create(
            @RequestBody FileBusinessRuleMappingRequest request) {
        try {
            FileBusinessRuleMappingResponse response = service.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @Operation(
        summary = "Listar todos os mapeamentos",
        description = "Retorna todos os mapeamentos entre arquivos e regras de negócio"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de mapeamentos retornada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FileBusinessRuleMappingResponse.class)
            )
        )
    })
    public ResponseEntity<List<FileBusinessRuleMappingResponse>> findAll() {
        List<FileBusinessRuleMappingResponse> mappings = service.findAll();
        return ResponseEntity.ok(mappings);
    }

    @GetMapping("/file")
    @Operation(
        summary = "Buscar mapeamentos por arquivo",
        description = "Retorna todas as regras de negócio que um arquivo específico implementa"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Mapeamentos encontrados com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FileBusinessRuleMappingResponse.class)
            )
        )
    })
    public ResponseEntity<List<FileBusinessRuleMappingResponse>> findByFilePath(
            @RequestParam String filePath) {
        List<FileBusinessRuleMappingResponse> mappings = service.findByFilePath(filePath);
        return ResponseEntity.ok(mappings);
    }

    @GetMapping("/rule/{businessRuleId}")
    @Operation(
        summary = "Buscar mapeamentos por regra de negócio",
        description = "Retorna todos os arquivos que implementam uma regra de negócio específica"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Mapeamentos encontrados com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FileBusinessRuleMappingResponse.class)
            )
        )
    })
    public ResponseEntity<List<FileBusinessRuleMappingResponse>> findByBusinessRuleId(
            @PathVariable String businessRuleId) {
        List<FileBusinessRuleMappingResponse> mappings = service.findByBusinessRuleId(businessRuleId);
        return ResponseEntity.ok(mappings);
    }
}
