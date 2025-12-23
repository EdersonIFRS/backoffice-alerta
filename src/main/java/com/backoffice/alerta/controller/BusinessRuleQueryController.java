package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.BusinessRuleExplanationResponse;
import com.backoffice.alerta.dto.BusinessRuleSearchResponse;
import com.backoffice.alerta.rules.Criticality;
import com.backoffice.alerta.rules.Domain;
import com.backoffice.alerta.service.BusinessRuleQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.util.List;

/**
 * Controller REST para consulta inteligente e explicação de regras de negócio
 * 
 * ⚠️ IMPORTANTE: 100% READ-ONLY
 * - NÃO recalcula risco
 * - NÃO altera decisões
 * - NÃO cria notificações
 * - NÃO cria SLAs
 * - NÃO chama IA externa
 * - Apenas consulta e explica dados existentes
 * 
 * US#35 - Consulta Inteligente e Explicação de Regras de Negócio
 */
@RestController
@RequestMapping("/business-rules")
@Tag(name = "Business Rules", description = "Consulta inteligente e explicação de regras de negócio (Read-Only)")
public class BusinessRuleQueryController {

    private static final Logger log = LoggerFactory.getLogger(BusinessRuleQueryController.class);
    
    private final BusinessRuleQueryService queryService;

    public BusinessRuleQueryController(BusinessRuleQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER', 'ENGINEER', 'VIEWER')")
    @Operation(
        summary = "🔍 Busca regras de negócio com filtros opcionais",
        description = "Permite buscar regras de negócio existentes usando diversos filtros.\n\n" +
                     "**Casos de uso:**\n" +
                     "- \"Quais regras existem no domínio de pagamento?\"\n" +
                     "- \"Quais regras críticas já causaram incidentes?\"\n" +
                     "- \"Quais regras são mantidas pelo time de Platform?\"\n" +
                     "- \"Buscar regras sobre cálculo de horas\"\n\n" +
                     "**Filtros disponíveis:**\n" +
                     "- query: Busca textual em nome/descrição\n" +
                     "- domain: Filtrar por domínio (PAYMENT, BILLING, ORDER, USER, GENERIC)\n" +
                     "- criticality: Filtrar por criticidade (BAIXA, MEDIA, ALTA, CRITICA)\n" +
                     "- hasIncidents: Apenas regras com/sem incidentes\n" +
                     "- ownedByTeam: Filtrar por nome do time responsável\n\n" +
                     "**IMPORTANTE:**\n" +
                     "- ✅ Consulta read-only\n" +
                     "- ✅ Não altera dados\n" +
                     "- ✅ Não recalcula risco\n" +
                     "- ✅ Acessível para todos os roles",
        parameters = {
            @Parameter(name = "query", description = "Busca textual livre em nome/descrição da regra", example = "cálculo de horas"),
            @Parameter(name = "domain", description = "Domínio de negócio", schema = @Schema(implementation = Domain.class)),
            @Parameter(name = "criticality", description = "Nível de criticidade", schema = @Schema(implementation = Criticality.class)),
            @Parameter(name = "hasIncidents", description = "Filtrar apenas regras com incidentes (true) ou sem incidentes (false)", example = "true"),
            @Parameter(name = "ownedByTeam", description = "Nome do time responsável (busca parcial)", example = "Platform")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "Lista de regras encontradas (pode ser vazia)",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = BusinessRuleSearchResponse.class),
            examples = @ExampleObject(
                name = "Exemplo de resposta",
                value = "[\n" +
                       "  {\n" +
                       "    \"id\": \"BR-001\",\n" +
                       "    \"name\": \"REGRA_CALCULO_HORAS_PJ\",\n" +
                       "    \"domain\": \"PAYMENT\",\n" +
                       "    \"criticality\": \"CRITICA\",\n" +
                       "    \"shortDescription\": \"Define como calcular horas trabalhadas para profissionais PJ\"\n" +
                       "  },\n" +
                       "  {\n" +
                       "    \"id\": \"BR-042\",\n" +
                       "    \"name\": \"REGRA_VALIDACAO_PAGAMENTO\",\n" +
                       "    \"domain\": \"PAYMENT\",\n" +
                       "    \"criticality\": \"ALTA\",\n" +
                       "    \"shortDescription\": \"Valida integridade de dados de pagamento antes de processar\"\n" +
                       "  }\n" +
                       "]"
            )
        )
    )
    public ResponseEntity<List<BusinessRuleSearchResponse>> searchRules(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) Domain domain,
        @RequestParam(required = false) Criticality criticality,
        @RequestParam(required = false) Boolean hasIncidents,
        @RequestParam(required = false) String ownedByTeam
    ) {
        log.info("🔍 [QUERY] Recebida busca de regras: query={}, domain={}, criticality={}, hasIncidents={}, team={}", 
            query, domain, criticality, hasIncidents, ownedByTeam);
        
        List<BusinessRuleSearchResponse> results = queryService.searchBusinessRules(
            query, domain, criticality, hasIncidents, ownedByTeam
        );
        
        log.info("✅ [QUERY] {} regras retornadas", results.size());
        
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}/explain")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER', 'ENGINEER', 'VIEWER')")
    @Operation(
        summary = "📖 Obtém explicação detalhada de uma regra de negócio",
        description = "Retorna explicação completa e contextual de uma regra de negócio, incluindo:\n\n" +
                     "**Informações fornecidas:**\n" +
                     "- Descrição de negócio clara e objetiva\n" +
                     "- Impacto operacional (o que acontece se quebrar)\n" +
                     "- Por que a regra importa (valor de negócio)\n" +
                     "- Riscos conhecidos baseados em criticidade e domínio\n" +
                     "- Histórico de incidentes (quantidade, severidade, último incidente)\n" +
                     "- Arquivos que implementam a regra\n" +
                     "- Times responsáveis (PRIMARY_OWNER, SECONDARY_OWNER, BACKUP)\n" +
                     "- Dicas estratégicas de risco (baseadas em análise determinística)\n\n" +
                     "**Casos de uso:**\n" +
                     "- \"Qual é a regra de cálculo de horas para PJ?\"\n" +
                     "- \"Essa regra já causou incidentes?\"\n" +
                     "- \"Quais arquivos implementam essa regra?\"\n" +
                     "- \"Qual time é responsável por essa regra?\"\n" +
                     "- \"Quais os riscos de alterar essa regra?\"\n\n" +
                     "**Construção da explicação (100% determinística):**\n" +
                     "- Baseada em dados existentes (regras, incidentes, ownership, arquivos)\n" +
                     "- Sem IA externa (explicação gerada por lógica interna)\n" +
                     "- Linguagem de negócio (não técnica)\n" +
                     "- Contextualizada por domínio e criticidade\n\n" +
                     "**IMPORTANTE:**\n" +
                     "- ✅ Consulta read-only\n" +
                     "- ✅ Não altera dados\n" +
                     "- ✅ Não recalcula risco\n" +
                     "- ✅ Acessível para todos os roles"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Explicação detalhada da regra",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = BusinessRuleExplanationResponse.class),
            examples = {
                @ExampleObject(
                    name = "Exemplo 1: Regra crítica com incidentes",
                    description = "Regra de pagamento crítica com histórico de incidentes e múltiplos times responsáveis",
                    value = "{\n" +
                           "  \"id\": \"BR-001\",\n" +
                           "  \"name\": \"REGRA_CALCULO_HORAS_PJ\",\n" +
                           "  \"domain\": \"PAYMENT\",\n" +
                           "  \"criticality\": \"CRITICA\",\n" +
                           "  \"businessDescription\": \"Define como calcular horas trabalhadas para profissionais PJ, incluindo regras de arredondamento, horas extras e descontos.\",\n" +
                           "  \"operationalImpact\": \"IMPACTO CRÍTICO: Falhas nesta regra podem causar perda financeira significativa, violação de compliance ou interrupção de serviço crítico. Afeta processamento de pagamentos e transações financeiras.\",\n" +
                           "  \"whyItMatters\": \"💰 Impacta diretamente receita e compliance financeiro. ⚠️ Mudanças nesta regra exigem revisão cuidadosa e teste rigoroso. Esta regra faz parte do núcleo de negócio e deve ser mantida com alta qualidade.\",\n" +
                           "  \"knownRisks\": [\n" +
                           "    \"Risco de impacto financeiro direto em caso de falha\",\n" +
                           "    \"Requer aprovação de múltiplos stakeholders para mudanças\",\n" +
                           "    \"Risco de fraude se validações forem removidas\",\n" +
                           "    \"Compliance com regulamentações financeiras (PCI-DSS)\"\n" +
                           "  ],\n" +
                           "  \"historicalIncidents\": {\n" +
                           "    \"totalIncidents\": 8,\n" +
                           "    \"criticalIncidents\": 2,\n" +
                           "    \"highIncidents\": 4,\n" +
                           "    \"lastIncidentDate\": \"2024-11-15\",\n" +
                           "    \"summary\": \"8 incidentes registrados (2 críticos, 4 altos). Último incidente: 2024-11-15\"\n" +
                           "  },\n" +
                           "  \"implementedByFiles\": [\n" +
                           "    \"src/main/java/com/empresa/payment/HourCalculationService.java\",\n" +
                           "    \"src/main/java/com/empresa/payment/PJContractProcessor.java\"\n" +
                           "  ],\n" +
                           "  \"ownedByTeams\": [\n" +
                           "    {\"teamName\": \"Platform Team\", \"role\": \"PRIMARY_OWNER\"},\n" +
                           "    {\"teamName\": \"Finance Team\", \"role\": \"SECONDARY_OWNER\"},\n" +
                           "    {\"teamName\": \"DevOps Team\", \"role\": \"BACKUP\"}\n" +
                           "  ],\n" +
                           "  \"riskHints\": [\n" +
                           "    \"⚠️ CRÍTICO: Qualquer mudança deve passar por revisão executiva\",\n" +
                           "    \"🔴 ATENÇÃO: Regra com histórico frequente de incidentes - revisar implementação\"\n" +
                           "  ]\n" +
                           "}"
                ),
                @ExampleObject(
                    name = "Exemplo 2: Regra simples sem incidentes",
                    description = "Regra de baixa criticidade, estável, sem incidentes históricos",
                    value = "{\n" +
                           "  \"id\": \"BR-099\",\n" +
                           "  \"name\": \"REGRA_FORMATACAO_ENDERECO\",\n" +
                           "  \"domain\": \"USER\",\n" +
                           "  \"criticality\": \"BAIXA\",\n" +
                           "  \"businessDescription\": \"Padroniza formatação de endereços para exibição em relatórios.\",\n" +
                           "  \"operationalImpact\": \"IMPACTO BAIXO: Falhas têm impacto operacional mínimo e podem ser corrigidas sem urgência. Afeta gestão de usuários e autenticação.\",\n" +
                           "  \"whyItMatters\": \"Esta regra faz parte do núcleo de negócio e deve ser mantida com alta qualidade.\",\n" +
                           "  \"knownRisks\": [\n" +
                           "    \"Sem riscos críticos conhecidos - monitoramento contínuo recomendado\"\n" +
                           "  ],\n" +
                           "  \"historicalIncidents\": {\n" +
                           "    \"totalIncidents\": 0,\n" +
                           "    \"criticalIncidents\": 0,\n" +
                           "    \"highIncidents\": 0,\n" +
                           "    \"lastIncidentDate\": null,\n" +
                           "    \"summary\": \"✅ Nenhum incidente registrado - regra estável.\"\n" +
                           "  },\n" +
                           "  \"implementedByFiles\": [\n" +
                           "    \"src/main/java/com/empresa/user/AddressFormatter.java\"\n" +
                           "  ],\n" +
                           "  \"ownedByTeams\": [\n" +
                           "    {\"teamName\": \"User Experience Team\", \"role\": \"PRIMARY_OWNER\"}\n" +
                           "  ],\n" +
                           "  \"riskHints\": [\n" +
                           "    \"✅ Regra estável sem histórico de incidentes\"\n" +
                           "  ]\n" +
                           "}"
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "Regra de negócio não encontrada"
    )
    public ResponseEntity<BusinessRuleExplanationResponse> explainRule(
        @PathVariable String id
    ) {
        log.info("📖 [EXPLAIN] Recebida solicitação de explicação para regra: {}", id);
        
        return queryService.explainBusinessRule(id)
            .map(explanation -> {
                log.info("✅ [EXPLAIN] Explicação gerada com sucesso para regra: {}", id);
                return ResponseEntity.ok(explanation);
            })
            .orElseGet(() -> {
                log.warn("⚠️ [EXPLAIN] Regra não encontrada: {}", id);
                return ResponseEntity.notFound().build();
            });
    }
}
