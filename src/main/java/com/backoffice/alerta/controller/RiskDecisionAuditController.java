package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.RiskDecisionAuditResponse;
import com.backoffice.alerta.rules.FinalDecision;
import com.backoffice.alerta.rules.RiskDecisionAudit;
import com.backoffice.alerta.service.RiskDecisionAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller REST para consulta de auditoria de decisões de risco
 * 
 * IMPORTANTE: Endpoints são SOMENTE LEITURA
 * - Não permite criação manual (auditoria é automática)
 * - Não permite atualização (registros são imutáveis)
 * - Não permite exclusão (trilha permanente para compliance)
 */
@RestController
@RequestMapping("/risk/audit")
@Tag(name = "Audit", description = "Consulta de trilha de auditoria de decisões de risco (somente leitura)")
public class RiskDecisionAuditController {

    private final RiskDecisionAuditService auditService;

    public RiskDecisionAuditController(RiskDecisionAuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @Operation(
        summary = "Lista todos os registros de auditoria",
        description = "Retorna trilha completa de todas as decisões de risco registradas. " +
                     "Ordenado por data de criação (mais recentes primeiro). " +
                     "Útil para dashboards de governança e análise histórica.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Lista de registros de auditoria recuperada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = RiskDecisionAuditResponse.class))
                )
            )
        }
    )
    public ResponseEntity<List<RiskDecisionAuditResponse>> getAllAudits() {
        List<RiskDecisionAudit> audits = auditService.findAll();
        
        List<RiskDecisionAuditResponse> responses = audits.stream()
            .map(RiskDecisionAuditResponse::new)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{pullRequestId}")
    @Operation(
        summary = "Busca registros de auditoria por Pull Request",
        description = "Retorna histórico de decisões para um Pull Request específico. " +
                     "Pode haver múltiplos registros se houve recálculo ou consulta de IA. " +
                     "Útil para rastreabilidade e explicabilidade de decisões.",
        parameters = {
            @Parameter(
                name = "pullRequestId",
                description = "ID do Pull Request",
                example = "PR-458",
                required = true
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Registros encontrados",
                content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = RiskDecisionAuditResponse.class))
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Nenhum registro encontrado para o Pull Request"
            )
        }
    )
    public ResponseEntity<List<RiskDecisionAuditResponse>> getAuditsByPullRequest(
            @PathVariable String pullRequestId) {
        
        List<RiskDecisionAudit> audits = auditService.findByPullRequestId(pullRequestId);
        
        if (audits.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<RiskDecisionAuditResponse> responses = audits.stream()
            .map(RiskDecisionAuditResponse::new)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/decision/{decision}")
    @Operation(
        summary = "Busca registros de auditoria por decisão final",
        description = "Filtra registros pela decisão final tomada pelo sistema. " +
                     "Útil para análise de padrões: quantos PRs foram bloqueados, aprovados condicionalmente, etc. " +
                     "Suporta análise de efetividade de políticas de risco.",
        parameters = {
            @Parameter(
                name = "decision",
                description = "Decisão final do sistema",
                example = "BLOQUEADO",
                required = true,
                schema = @Schema(implementation = FinalDecision.class)
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Registros encontrados",
                content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = RiskDecisionAuditResponse.class))
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Decisão inválida. Valores aceitos: APROVADO, APROVADO_COM_RESTRICOES, BLOQUEADO"
            )
        }
    )
    public ResponseEntity<List<RiskDecisionAuditResponse>> getAuditsByDecision(
            @PathVariable FinalDecision decision) {
        
        List<RiskDecisionAudit> audits = auditService.findByFinalDecision(decision);
        
        List<RiskDecisionAuditResponse> responses = audits.stream()
            .map(RiskDecisionAuditResponse::new)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/health")
    @Operation(
        summary = "Verifica saúde do serviço de auditoria",
        description = "Endpoint simples para verificar se o serviço de auditoria está disponível"
    )
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Audit Service is running (read-only)");
    }
}
