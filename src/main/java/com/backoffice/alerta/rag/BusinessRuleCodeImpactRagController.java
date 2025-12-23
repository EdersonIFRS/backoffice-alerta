package com.backoffice.alerta.rag;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para RAG de impacto no código
 * 
 * US#45 - RAG com Mapeamento de Código Impactado
 * 
 * Endpoint consultivo que responde perguntas sobre:
 * - Onde mexer no código
 * - Quais arquivos serão impactados
 * - Quais riscos técnicos existem
 * - Quem deve ser avisado
 * 
 * 🔐 Acesso: ADMIN e RISK_MANAGER
 * ⚠️ Read-only: não altera dados, não cria auditoria
 */
@RestController
@RequestMapping("/risk/rag")
@Tag(name = "RAG - Code Impact", description = "RAG para análise de impacto no código")
@SecurityRequirement(name = "bearer-jwt")
public class BusinessRuleCodeImpactRagController {
    
    private final BusinessRuleCodeImpactRagService codeImpactService;
    
    public BusinessRuleCodeImpactRagController(BusinessRuleCodeImpactRagService codeImpactService) {
        this.codeImpactService = codeImpactService;
    }
    
    @PostMapping("/code-impact")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER')")
    @Operation(
        summary = "Consulta RAG sobre impacto no código",
        description = """
            Permite fazer perguntas em linguagem natural sobre:
            
            ✅ **Onde alterar o código** quando uma regra precisa mudar
            ✅ **Quais arquivos** serão impactados por uma mudança
            ✅ **Quais dependências** podem causar efeito cascata
            ✅ **Quem são os responsáveis** que devem ser avisados
            ✅ **Quais riscos técnicos** estão envolvidos
            
            ⚠️ **IMPORTANTE:**
            - A IA NÃO pode inventar informações
            - Todos os arquivos retornados existem no projeto real
            - Se a IA falhar, o sistema responde de forma determinística
            - Este endpoint é **consultivo**, não decisório
            - **Read-only**: não altera dados, não cria auditoria
            
            🔐 **Acesso:** Apenas ADMIN e RISK_MANAGER
            
            📊 **Foco da Explicação:**
            - `BUSINESS`: Foco em regras de negócio e impacto funcional
            - `TECHNICAL`: Foco em arquivos, dependências e implementação
            - `EXECUTIVE`: Foco em riscos, ownership e decisões
            
            🎯 **Exemplos de perguntas:**
            - "Onde alterar o cálculo de horas para PJ?"
            - "Quais arquivos mexer para validação de CPF?"
            - "Se mudar a regra de PIX, o que pode quebrar?"
            - "Quem preciso avisar antes de alterar pagamento PJ?"
            """,
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Análise de impacto gerada com sucesso"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Requisição inválida (pergunta vazia, etc.)"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Acesso negado (requer ADMIN ou RISK_MANAGER)"
            )
        }
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Pergunta em linguagem natural sobre impacto no código",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = RagCodeImpactRequest.class),
            examples = {
                @ExampleObject(
                    name = "Alteração Crítica - PJ",
                    value = """
                        {
                          "question": "Onde alterar o cálculo de horas para Pessoa Jurídica?",
                          "focus": "TECHNICAL",
                          "maxFiles": 10
                        }
                        """,
                    description = "Pergunta técnica sobre alteração em regra crítica"
                ),
                @ExampleObject(
                    name = "Alteração Simples - Validação",
                    value = """
                        {
                          "question": "Quais arquivos mexer para mudar validação de CPF?",
                          "focus": "TECHNICAL",
                          "maxFiles": 5
                        }
                        """,
                    description = "Pergunta sobre alteração técnica simples"
                ),
                @ExampleObject(
                    name = "Pergunta Executiva - Ownership",
                    value = """
                        {
                          "question": "Quem preciso avisar antes de alterar regras de pagamento?",
                          "focus": "EXECUTIVE",
                          "maxFiles": 10
                        }
                        """,
                    description = "Pergunta executiva focada em responsáveis e riscos"
                )
            }
        )
    )
    public ResponseEntity<RagCodeImpactResponse> analyzeCodeImpact(
            @Valid @RequestBody RagCodeImpactRequest request) {
        
        RagCodeImpactResponse response = codeImpactService.analyzeCodeImpact(request);
        return ResponseEntity.ok(response);
    }
}
