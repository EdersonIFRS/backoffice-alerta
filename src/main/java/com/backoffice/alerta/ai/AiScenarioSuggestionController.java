package com.backoffice.alerta.ai;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para sugestão automática de cenários de decisão via IA
 * 
 * ⚠️ IMPORTANTE: 100% READ-ONLY
 * - IA NÃO decide automaticamente
 * - IA NÃO cria auditoria
 * - IA NÃO persiste dados
 * - IA NÃO envia notificações
 * - IA NÃO cria SLA
 * - IA apenas simula e recomenda
 * 
 * US#34 - IA sugere automaticamente cenários ótimos de decisão
 */
@RestController
@RequestMapping("/risk/ai")
@Tag(name = "AI", description = "Sugestão automática de cenários de decisão via IA (Read-Only)")
public class AiScenarioSuggestionController {

    private static final Logger log = LoggerFactory.getLogger(AiScenarioSuggestionController.class);
    
    private final AiScenarioSuggestionService aiService;

    public AiScenarioSuggestionController(AiScenarioSuggestionService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/suggest-scenarios")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER')")
    @Operation(
        summary = "🤖 IA sugere automaticamente cenários ótimos de decisão",
        description = "Motor de IA que analisa o contexto de um Pull Request e sugere automaticamente " +
                     "os melhores cenários alternativos para reduzir risco, evitar SLAs e melhorar a decisão.\n\n" +
                     "**Como funciona:**\n" +
                     "1. IA executa análise baseline do cenário atual\n" +
                     "2. IA gera automaticamente variações controladas:\n" +
                     "   - Mudança de ambiente (PRODUCTION → STAGING/DEV)\n" +
                     "   - Mudança de tipo (HOTFIX → FEATURE → REFACTOR)\n" +
                     "   - Exclusão de arquivos críticos\n" +
                     "   - Divisão lógica de PR\n" +
                     "   - Combinações estratégicas\n" +
                     "3. IA simula cada variação usando o motor de simulação\n" +
                     "4. IA avalia cenários com score objetivo (0-100):\n" +
                     "   - Redução de risco (peso alto)\n" +
                     "   - Remoção de SLA (peso alto)\n" +
                     "   - Menos times notificados\n" +
                     "   - Melhoria na decisão\n" +
                     "5. IA ordena e retorna os top N melhores cenários\n\n" +
                     "**Explicabilidade:**\n" +
                     "- Cada cenário possui explicação em linguagem de negócio\n" +
                     "- Score transparente e auditável\n" +
                     "- Justificativa clara do motivo da recomendação\n\n" +
                     "**IMPORTANTE:**\n" +
                     "- ✅ IA apenas SUGERE (não decide)\n" +
                     "- ✅ 100% simulação (read-only)\n" +
                     "- ❌ IA NÃO cria auditoria\n" +
                     "- ❌ IA NÃO persiste dados\n" +
                     "- ❌ IA NÃO envia notificações\n" +
                     "- ❌ IA NÃO cria SLA\n" +
                     "- ❌ IA NÃO aplica mudanças automaticamente\n\n" +
                     "**Segurança:**\n" +
                     "- Endpoint protegido por RBAC\n" +
                     "- Apenas ADMIN e RISK_MANAGER podem acessar",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AiScenarioSuggestionRequest.class),
                examples = {
                    @ExampleObject(
                        name = "Exemplo 1: HOTFIX em PRODUCTION (cenário crítico)",
                        description = "IA analisa HOTFIX crítico em produção e sugere alternativas mais seguras",
                        value = "{\n" +
                               "  \"pullRequestId\": \"PR-2024-CRITICAL-789\",\n" +
                               "  \"environment\": \"PRODUCTION\",\n" +
                               "  \"changeType\": \"HOTFIX\",\n" +
                               "  \"maxScenarios\": 3\n" +
                               "}"
                    ),
                    @ExampleObject(
                        name = "Exemplo 2: FEATURE em PRODUCTION",
                        description = "IA avalia feature em produção e sugere otimizações",
                        value = "{\n" +
                               "  \"pullRequestId\": \"PR-2024-FEATURE-456\",\n" +
                               "  \"environment\": \"PRODUCTION\",\n" +
                               "  \"changeType\": \"FEATURE\",\n" +
                               "  \"maxScenarios\": 5\n" +
                               "}"
                    ),
                    @ExampleObject(
                        name = "Exemplo 3: Buscar apenas o melhor cenário",
                        description = "IA retorna apenas a melhor alternativa possível",
                        value = "{\n" +
                               "  \"pullRequestId\": \"PR-2024-123\",\n" +
                               "  \"environment\": \"STAGING\",\n" +
                               "  \"changeType\": \"FEATURE\",\n" +
                               "  \"maxScenarios\": 1\n" +
                               "}"
                    )
                }
            )
        )
    )
    @ApiResponse(
        responseCode = "200",
        description = "IA retorna baseline + cenários sugeridos ordenados por score",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = AiScenarioSuggestionResponse.class),
            examples = @ExampleObject(
                name = "Response de exemplo",
                value = "{\n" +
                       "  \"baseline\": {\n" +
                       "    \"riskLevel\": \"CRITICO\",\n" +
                       "    \"decision\": \"BLOQUEADO\"\n" +
                       "  },\n" +
                       "  \"suggestedScenarios\": [\n" +
                       "    {\n" +
                       "      \"scenarioId\": \"SC-1\",\n" +
                       "      \"description\": \"Mover deploy para STAGING reduz risco crítico\",\n" +
                       "      \"riskLevel\": \"MEDIO\",\n" +
                       "      \"decision\": \"APROVADO_COM_RESTRICOES\",\n" +
                       "      \"slaRemoved\": true,\n" +
                       "      \"teamsNotified\": [\"Platform Team\"],\n" +
                       "      \"score\": 92,\n" +
                       "      \"explanation\": \"Reduz risco de CRÍTICO → MÉDIO. Remove SLA crítico. Reduz notificações de 3 para 1 times. Remove 2 restrições operacionais.\"\n" +
                       "    },\n" +
                       "    {\n" +
                       "      \"scenarioId\": \"SC-2\",\n" +
                       "      \"description\": \"Reclassificar como FEATURE reduz urgência e permite mais revisões\",\n" +
                       "      \"riskLevel\": \"ALTO\",\n" +
                       "      \"decision\": \"APROVADO_COM_RESTRICOES\",\n" +
                       "      \"slaRemoved\": true,\n" +
                       "      \"teamsNotified\": [\"Platform Team\", \"Security Team\"],\n" +
                       "      \"score\": 75,\n" +
                       "      \"explanation\": \"Reduz risco de CRÍTICO → ALTO. Remove SLA crítico. Remove 1 restrições operacionais.\"\n" +
                       "    },\n" +
                       "    {\n" +
                       "      \"scenarioId\": \"SC-3\",\n" +
                       "      \"description\": \"Remover arquivos críticos da análise reduz impacto em regras financeiras\",\n" +
                       "      \"riskLevel\": \"ALTO\",\n" +
                       "      \"decision\": \"APROVADO_COM_RESTRICOES\",\n" +
                       "      \"slaRemoved\": false,\n" +
                       "      \"teamsNotified\": [\"Platform Team\", \"Security Team\"],\n" +
                       "      \"score\": 55,\n" +
                       "      \"explanation\": \"Reduz risco de CRÍTICO → ALTO. Remove impacto de 3 regras críticas.\"\n" +
                       "    }\n" +
                       "  ]\n" +
                       "}"
            )
        )
    )
    public ResponseEntity<AiScenarioSuggestionResponse> suggestScenarios(
        @RequestBody AiScenarioSuggestionRequest request
    ) {
        log.info("🤖 [AI] Recebida solicitação de sugestão automática para PR: {}", request.getPullRequestId());
        
        AiScenarioSuggestionResponse response = aiService.suggestScenarios(request);
        
        log.info("✅ [AI] {} cenários sugeridos com sucesso para PR: {}", 
            response.getSuggestedScenarios().size(), 
            request.getPullRequestId());
        
        return ResponseEntity.ok(response);
    }
}
