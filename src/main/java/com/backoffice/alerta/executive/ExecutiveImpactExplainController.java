package com.backoffice.alerta.executive;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller para explicação executiva de impacto sistêmico
 * 
 * US#38 - Explicação Executiva Inteligente
 * 
 * IMPORTANTE: Endpoint consultivo - não altera decisões do sistema
 * 
 * Segurança RBAC:
 * ✅ ADMIN
 * ✅ RISK_MANAGER
 * ❌ ENGINEER
 * ❌ VIEWER
 */
@RestController
@RequestMapping("/risk/business-impact")
@Tag(name = "Executive Impact Explanation", description = "Explicação executiva de impacto sistêmico (Consultivo - US#38)")
public class ExecutiveImpactExplainController {
    
    private final ExecutiveImpactExplainService explainService;
    
    public ExecutiveImpactExplainController(ExecutiveImpactExplainService explainService) {
        this.explainService = explainService;
    }
    
    @PostMapping("/executive-explain")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER')")
    @Operation(
        summary = "Gera explicação executiva de impacto sistêmico",
        description = """
            **US#38 - Explicação Executiva Inteligente**
            
            Endpoint consultivo que interpreta o impacto de uma mudança em linguagem de negócio.
            
            **IMPORTANTE:**
            - ✅ Este endpoint é CONSULTIVO e não altera decisões do sistema
            - ✅ Não persiste dados, não cria auditorias, não envia notificações
            - ✅ Reutiliza dados existentes do grafo de impacto (US#37)
            - ✅ Gera interpretação determinística em linguagem executiva
            
            **Segurança:**
            - ADMIN: Acesso total
            - RISK_MANAGER: Acesso total
            - ENGINEER: Sem acesso
            - VIEWER: Sem acesso
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Explicação executiva gerada com sucesso"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acesso negado - apenas ADMIN e RISK_MANAGER"
        )
    })
    public ResponseEntity<ExecutiveImpactExplainResponse> generateExplanation(
            @Valid @RequestBody ExecutiveImpactExplainRequest request) {
        
        ExecutiveImpactExplainResponse response = explainService.generateExplanation(request);
        return ResponseEntity.ok(response);
    }
}
