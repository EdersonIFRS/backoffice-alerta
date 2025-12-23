package com.backoffice.alerta.simulation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para simulação What-If de decisões de risco
 * 
 * ⚠️ IMPORTANTE: 100% READ-ONLY
 * - NÃO cria auditoria
 * - NÃO persiste dados
 * - NÃO envia notificações
 * - NÃO cria SLA
 * - Apenas simulação em memória para análise executiva
 * 
 * US#33 - Simulação Executiva de Decisão de Risco
 */
@RestController
@RequestMapping("/risk/simulate")
@Tag(name = "Simulation", description = "Simulação What-If de decisões de risco (Read-Only)")
public class RiskWhatIfSimulationController {

    private final RiskWhatIfSimulationService simulationService;

    public RiskWhatIfSimulationController(RiskWhatIfSimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping("/decision")
    @Operation(
        summary = "Simula mudança na decisão de risco com cenário What-If",
        description = "Motor de simulação executiva que permite avaliar como a decisão de risco mudaria sob diferentes cenários:\n\n" +
                     "**Casos de uso:**\n" +
                     "- E se o deploy fosse em ambiente diferente?\n" +
                     "- E se o tipo de mudança fosse diferente (FEATURE vs HOTFIX)?\n" +
                     "- E se determinados arquivos fossem excluídos da mudança?\n\n" +
                     "**IMPORTANTE:**\n" +
                     "- ✅ 100% simulação (read-only)\n" +
                     "- ❌ NÃO cria auditoria\n" +
                     "- ❌ NÃO persiste dados\n" +
                     "- ❌ NÃO envia notificações reais\n" +
                     "- ❌ NÃO cria SLA\n\n" +
                     "**Retorna:**\n" +
                     "- Baseline (cenário atual)\n" +
                     "- Simulação (com what-if aplicado)\n" +
                     "- Delta (diferenças)\n" +
                     "- Recomendação executiva",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RiskWhatIfSimulationRequest.class),
                examples = {
                    @ExampleObject(
                        name = "Exemplo 1: BLOQUEADO → APROVADO COM RESTRIÇÕES ao mudar ambiente",
                        description = "Simula mudança de PRODUCTION para STAGING",
                        value = "{\n" +
                               "  \"pullRequestId\": \"PR-2024-123\",\n" +
                               "  \"environment\": \"PRODUCTION\",\n" +
                               "  \"changeType\": \"HOTFIX\",\n" +
                               "  \"baselineDecision\": \"BLOQUEADO\",\n" +
                               "  \"whatIf\": {\n" +
                               "    \"overrideEnvironment\": \"STAGING\",\n" +
                               "    \"overrideChangeType\": null,\n" +
                               "    \"excludeFiles\": []\n" +
                               "  }\n" +
                               "}"
                    ),
                    @ExampleObject(
                        name = "Exemplo 2: Exclusão de arquivo crítico reduz risco",
                        description = "Simula remoção de 3 arquivos críticos da mudança",
                        value = "{\n" +
                               "  \"pullRequestId\": \"PR-2024-456\",\n" +
                               "  \"environment\": \"PRODUCTION\",\n" +
                               "  \"changeType\": \"FEATURE\",\n" +
                               "  \"baselineDecision\": \"APROVADO_COM_RESTRICOES\",\n" +
                               "  \"whatIf\": {\n" +
                               "    \"overrideEnvironment\": null,\n" +
                               "    \"overrideChangeType\": null,\n" +
                               "    \"excludeFiles\": [\n" +
                               "      \"src/main/java/payment/PaymentService.java\",\n" +
                               "      \"src/main/java/security/AuthService.java\",\n" +
                               "      \"src/main/resources/application.yml\"\n" +
                               "    ]\n" +
                               "  }\n" +
                               "}"
                    ),
                    @ExampleObject(
                        name = "Exemplo 3: HOTFIX → FEATURE reduz impacto organizacional",
                        description = "Simula mudança de tipo de deploy",
                        value = "{\n" +
                               "  \"pullRequestId\": \"PR-2024-789\",\n" +
                               "  \"environment\": \"PRODUCTION\",\n" +
                               "  \"changeType\": \"HOTFIX\",\n" +
                               "  \"baselineDecision\": \"BLOQUEADO\",\n" +
                               "  \"whatIf\": {\n" +
                               "    \"overrideEnvironment\": null,\n" +
                               "    \"overrideChangeType\": \"FEATURE\",\n" +
                               "    \"excludeFiles\": []\n" +
                               "  }\n" +
                               "}"
                    )
                }
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Simulação executada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RiskWhatIfSimulationResponse.class),
                    examples = @ExampleObject(
                        value = "{\n" +
                               "  \"pullRequestId\": \"PR-2024-123\",\n" +
                               "  \"baseline\": {\n" +
                               "    \"finalDecision\": \"BLOQUEADO\",\n" +
                               "    \"riskLevel\": \"CRITICO\",\n" +
                               "    \"impactedRules\": 5,\n" +
                               "    \"slaTriggered\": true,\n" +
                               "    \"notifiedTeams\": [\"Platform Team\", \"Security Team\"],\n" +
                               "    \"restrictions\": [\"Requer aprovação VP\", \"Deploy apenas em horário comercial\"]\n" +
                               "  },\n" +
                               "  \"simulation\": {\n" +
                               "    \"finalDecision\": \"APROVADO_COM_RESTRICOES\",\n" +
                               "    \"riskLevel\": \"MEDIO\",\n" +
                               "    \"impactedRules\": 5,\n" +
                               "    \"slaTriggered\": false,\n" +
                               "    \"notifiedTeams\": [],\n" +
                               "    \"restrictions\": []\n" +
                               "  },\n" +
                               "  \"delta\": {\n" +
                               "    \"riskReduction\": \"CRITICO → MEDIO\",\n" +
                               "    \"rulesNoLongerImpacted\": [],\n" +
                               "    \"slaImpact\": \"SLA não mais necessário\"\n" +
                               "  },\n" +
                               "  \"executiveRecommendation\": {\n" +
                               "    \"headline\": \"Redução significativa de risco\",\n" +
                               "    \"confidence\": \"ALTA\",\n" +
                               "    \"summary\": \"A mudança proposta reduz o nível de risco de CRITICO para MEDIO. Recomenda-se considerar esta alternativa.\"\n" +
                               "  }\n" +
                               "}"
                    )
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Request inválido (dados obrigatórios ausentes)"
            )
        }
    )
    public ResponseEntity<RiskWhatIfSimulationResponse> simulateDecision(
            @RequestBody RiskWhatIfSimulationRequest request) {
        
        RiskWhatIfSimulationResponse response = simulationService.simulate(request);
        return ResponseEntity.ok(response);
    }
}
