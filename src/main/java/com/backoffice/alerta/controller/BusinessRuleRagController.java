package com.backoffice.alerta.controller;

import com.backoffice.alerta.rag.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller RAG para consultas sobre regras de negócio
 * Permite perguntas em linguagem natural com contexto real do sistema
 */
@RestController
@RequestMapping("/risk/rag")
@Tag(name = "RAG - Consultas Inteligentes", description = "Perguntas em linguagem natural sobre regras de negócio com IA")
public class BusinessRuleRagController {
    
    private final BusinessRuleRagService ragService;
    
    public BusinessRuleRagController(BusinessRuleRagService ragService) {
        this.ragService = ragService;
    }
    
    @PostMapping("/query")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER')")
    @Operation(
        summary = "Consulta RAG sobre regras de negócio",
        description = """
            Permite fazer perguntas em linguagem natural sobre regras de negócio, impactos, 
            ownership e histórico, usando IA com contexto REAL do sistema.
            
            **⚠️ IMPORTANTE:**
            - A IA NÃO pode inventar informações
            - Todas as respostas têm fontes reais do sistema
            - Se a IA falhar, o sistema responde de forma determinística
            - Este endpoint é **consultivo**, não decisório
            - Sempre verificar o disclaimer e as fontes
            
            **🔐 Acesso:** Apenas ADMIN e RISK_MANAGER
            
            **📊 Foco da Explicação:**
            - `BUSINESS`: Foco em regras de negócio e impacto funcional
            - `TECHNICAL`: Foco em implementação técnica e dependências
            - `EXECUTIVE`: Foco em riscos, ownership e decisões estratégicas
            
            **🎯 Exemplos de perguntas:**
            - "Como funciona a validação de CPF para pagamento PJ?"
            - "Quais regras podem impactar o PIX se mudarem?"
            - "Quem é o responsável pela validação de CNPJ?"
            - "Quantos incidentes tivemos com regras de pagamento?"
            """,
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Resposta gerada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RagQueryResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Acesso negado - apenas ADMIN e RISK_MANAGER"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Pergunta inválida ou parâmetros incorretos"
            )
        }
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Pergunta em linguagem natural sobre regras de negócio",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = RagQueryRequest.class),
            examples = {
                @ExampleObject(
                    name = "Pergunta de Negócio",
                    summary = "Validação de CPF para PJ",
                    value = """
                        {
                          "question": "Como funciona a validação de CPF para pagamento de pessoa jurídica?",
                          "focus": "BUSINESS",
                          "maxSources": 5
                        }
                        """
                ),
                @ExampleObject(
                    name = "Pergunta de Impacto",
                    summary = "Regras que podem quebrar",
                    value = """
                        {
                          "question": "Quais regras podem ser impactadas se eu alterar a validação de PIX?",
                          "focus": "TECHNICAL",
                          "maxSources": 8
                        }
                        """
                ),
                @ExampleObject(
                    name = "Pergunta Organizacional",
                    summary = "Ownership de regras",
                    value = """
                        {
                          "question": "Quem é o responsável pela regra de validação de CNPJ?",
                          "focus": "EXECUTIVE",
                          "maxSources": 3
                        }
                        """
                )
            }
        )
    )
    public ResponseEntity<RagQueryResponse> query(@Valid @RequestBody RagQueryRequest request) {
        RagQueryResponse response = ragService.query(request);
        return ResponseEntity.ok(response);
    }
}
