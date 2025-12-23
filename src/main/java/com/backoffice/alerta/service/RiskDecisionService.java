package com.backoffice.alerta.service;

import com.backoffice.alerta.dto.*;
import com.backoffice.alerta.rules.*;
import com.backoffice.alerta.repository.BusinessRuleIncidentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço de decisão inteligente de aprovação de mudanças
 * Orquestra análises existentes para tomar decisão final
 */
@Service
public class RiskDecisionService {

    private final BusinessImpactAnalysisService impactAnalysisService;
    private final BusinessRuleIncidentRepository incidentRepository;
    private final RiskDecisionAuditService auditService;
    private final BusinessRuleOwnershipRepository ownershipRepository;
    private final RiskNotificationService notificationService;

    public RiskDecisionService(BusinessImpactAnalysisService impactAnalysisService,
                              BusinessRuleIncidentRepository incidentRepository,
                              RiskDecisionAuditService auditService,
                              BusinessRuleOwnershipRepository ownershipRepository,
                              RiskNotificationService notificationService) {
        this.impactAnalysisService = impactAnalysisService;
        this.incidentRepository = incidentRepository;
        this.auditService = auditService;
        this.ownershipRepository = ownershipRepository;
        this.notificationService = notificationService;
    }

    /**
     * Decide se uma mudança deve ser aprovada
     * @param request Requisição com dados do PR
     * @return Decisão com justificativa e ações requeridas
     */
    public RiskDecisionResponse decide(RiskDecisionRequest request) {
        validateRequest(request);

        // Reutiliza análise de impacto existente (US #16)
        BusinessImpactRequest impactRequest = new BusinessImpactRequest(
            request.getPullRequestId(),
            request.getChangedFiles()
        );
        BusinessImpactResponse impactAnalysis = impactAnalysisService.analyze(impactRequest);

        // Extrai risco geral já calculado
        RiskLevel overallRisk = impactAnalysis.getOverallBusinessRisk();

        // Verifica incidentes críticos nas regras impactadas
        boolean hasCriticalIncidents = checkForCriticalIncidents(impactAnalysis);

        // Aplica lógica de decisão
        DecisionContext context = new DecisionContext(
            request.getEnvironment(),
            request.getChangeType(),
            overallRisk,
            hasCriticalIncidents,
            request.getPolicy()
        );

        FinalDecision decision = determineDecision(context);
        String reason = generateDecisionReason(context, impactAnalysis);
        List<String> requiredActions = generateRequiredActions(context, decision);
        List<String> conditions = generateConditions(context, decision);
        String explanation = generateExplanation(context, impactAnalysis);

        // Busca ownerships relevantes se decisão requer aprovação/notificação
        List<BusinessRuleOwnershipResponse> relevantOwnerships = new ArrayList<>();
        if (decision == FinalDecision.BLOQUEADO || 
            decision == FinalDecision.APROVADO_COM_RESTRICOES) {
            relevantOwnerships = collectRelevantOwnerships(impactAnalysis);
        }

        RiskDecisionResponse response = new RiskDecisionResponse(
            request.getPullRequestId(),
            decision,
            overallRisk,
            reason,
            requiredActions,
            conditions,
            explanation,
            relevantOwnerships
        );

        // Registra auditoria automaticamente (US #20)
        RiskDecisionAudit audit = auditService.createAudit(request, response, impactAnalysis);

        // Gera notificações organizacionais automaticamente (US #27)
        notificationService.generateNotifications(
            request, 
            response, 
            audit.getId(), 
            impactAnalysis.getImpactedBusinessRules()
        );

        return response;
    }

    /**
     * Determina a decisão final baseado no contexto
     */
    private FinalDecision determineDecision(DecisionContext context) {
        // Regra 1: Risco CRÍTICO em PRODUÇÃO = BLOQUEADO
        if (context.riskLevel == RiskLevel.CRITICO && context.environment.isCritical()) {
            return FinalDecision.BLOQUEADO;
        }

        // Regra 2: Risco ALTO + incidentes CRÍTICOS = BLOQUEADO
        if (context.riskLevel == RiskLevel.ALTO && context.hasCriticalIncidents) {
            return FinalDecision.BLOQUEADO;
        }

        // Regra 3: Risco ALTO + policy permite condicional = APROVADO_COM_RESTRICOES
        if (context.riskLevel == RiskLevel.ALTO && 
            context.policy != null && 
            context.policy.isAllowConditionalApproval()) {
            return FinalDecision.APROVADO_COM_RESTRICOES;
        }

        // Regra 4: Risco ALTO sem política condicional = BLOQUEADO
        if (context.riskLevel == RiskLevel.ALTO) {
            return FinalDecision.BLOQUEADO;
        }

        // Regra 5: Risco MÉDIO em PRODUÇÃO = APROVADO_COM_RESTRICOES
        if (context.riskLevel == RiskLevel.MEDIO && context.environment.isCritical()) {
            return FinalDecision.APROVADO_COM_RESTRICOES;
        }

        // Regra 6: HOTFIX com histórico crítico = APROVADO_COM_RESTRICOES
        if (context.changeType.isUrgent() && context.hasCriticalIncidents) {
            return FinalDecision.APROVADO_COM_RESTRICOES;
        }

        // Regra 7: Risco BAIXO = APROVADO
        if (context.riskLevel == RiskLevel.BAIXO) {
            return FinalDecision.APROVADO;
        }

        // Padrão: Risco médio ou baixo sem restrições específicas
        return FinalDecision.APROVADO;
    }

    /**
     * Verifica se há incidentes críticos nas regras impactadas
     */
    private boolean checkForCriticalIncidents(BusinessImpactResponse impactAnalysis) {
        if (impactAnalysis.getImpactedBusinessRules() == null) {
            return false;
        }

        for (ImpactedBusinessRuleResponse rule : impactAnalysis.getImpactedBusinessRules()) {
            UUID businessRuleUuid = UUID.fromString(rule.getBusinessRuleId());
            List<BusinessRuleIncident> incidents = 
                incidentRepository.findByBusinessRuleIdOrderByOccurredAtDesc(businessRuleUuid);
            
            boolean hasCritical = incidents.stream()
                .anyMatch(i -> i.getSeverity() == IncidentSeverity.CRITICAL);
            
            if (hasCritical) {
                return true;
            }
        }

        return false;
    }

    /**
     * Coleta ownerships relevantes das regras impactadas
     * Prioriza PRIMARY_OWNER, depois SECONDARY_OWNER, depois BACKUP
     */
    private List<BusinessRuleOwnershipResponse> collectRelevantOwnerships(
            BusinessImpactResponse impactAnalysis) {
        
        List<BusinessRuleOwnershipResponse> allOwnerships = new ArrayList<>();
        
        if (impactAnalysis.getImpactedBusinessRules() == null) {
            return allOwnerships;
        }

        for (ImpactedBusinessRuleResponse rule : impactAnalysis.getImpactedBusinessRules()) {
            try {
                UUID ruleId = UUID.fromString(rule.getBusinessRuleId());
                List<BusinessRuleOwnership> ownerships = 
                    ownershipRepository.findByBusinessRuleId(ruleId);
                
                // Converte para response e adiciona à lista
                ownerships.stream()
                    .map(BusinessRuleOwnershipResponse::new)
                    .forEach(allOwnerships::add);
            } catch (IllegalArgumentException e) {
                // ID inválido, ignora e continua
                continue;
            }
        }

        // Ordena por prioridade: PRIMARY_OWNER > SECONDARY_OWNER > BACKUP
        allOwnerships.sort((o1, o2) -> {
            int priority1 = getRolePriority(o1.getRole());
            int priority2 = getRolePriority(o2.getRole());
            return Integer.compare(priority1, priority2);
        });

        return allOwnerships;
    }

    /**
     * Retorna prioridade numérica do role (menor = mais prioritário)
     */
    private int getRolePriority(OwnershipRole role) {
        return switch (role) {
            case PRIMARY_OWNER -> 1;
            case SECONDARY_OWNER -> 2;
            case BACKUP -> 3;
        };
    }

    /**
     * Gera razão da decisão
     */
    private String generateDecisionReason(DecisionContext context, 
                                         BusinessImpactResponse impactAnalysis) {
        if (context.riskLevel == RiskLevel.CRITICO && context.environment.isCritical()) {
            return "Risco crítico detectado para ambiente de produção";
        }

        if (context.riskLevel == RiskLevel.ALTO && context.hasCriticalIncidents) {
            return "Mudança afeta regras críticas com histórico de incidentes graves";
        }

        if (context.riskLevel == RiskLevel.ALTO) {
            return "Nível de risco alto requer aprovação condicional ou bloqueio";
        }

        if (context.riskLevel == RiskLevel.MEDIO && context.environment.isCritical()) {
            return "Risco médio em produção requer condições especiais";
        }

        if (context.changeType.isUrgent() && context.hasCriticalIncidents) {
            return "Hotfix em área com histórico de incidentes críticos";
        }

        if (impactAnalysis.getImpactedBusinessRules().isEmpty()) {
            return "Nenhuma regra de negócio crítica impactada";
        }

        return "Mudança analisada e dentro dos limites aceitáveis";
    }

    /**
     * Gera ações obrigatórias
     */
    private List<String> generateRequiredActions(DecisionContext context, FinalDecision decision) {
        List<String> actions = new ArrayList<>();

        if (decision == FinalDecision.BLOQUEADO) {
            actions.add("Reanálise obrigatória da mudança");
            actions.add("Redução do escopo ou refatoração necessária");
            return actions;
        }

        if (decision == FinalDecision.APROVADO_COM_RESTRICOES) {
            if (context.riskLevel == RiskLevel.ALTO || context.hasCriticalIncidents) {
                actions.add("Revisão manual obrigatória por especialista sênior");
                actions.add("Plano de rollback documentado e testado");
            }

            if (context.environment.isCritical()) {
                actions.add("Deploy fora do horário comercial");
                actions.add("Equipe de plantão durante deploy");
            }

            if (context.changeType.isUrgent()) {
                actions.add("Validação em ambiente de staging obrigatória");
            }
        }

        return actions;
    }

    /**
     * Gera condições específicas
     */
    private List<String> generateConditions(DecisionContext context, FinalDecision decision) {
        List<String> conditions = new ArrayList<>();

        if (decision == FinalDecision.APROVADO_COM_RESTRICOES) {
            if (context.riskLevel == RiskLevel.ALTO) {
                conditions.add("Feature flag obrigatória para rollback imediato");
                conditions.add("Monitoramento reforçado nas primeiras 24 horas");
            }

            if (context.hasCriticalIncidents) {
                conditions.add("Alertas configurados para métricas críticas");
                conditions.add("Comunicação prévia com área de negócio");
            }

            if (context.environment.isCritical() && context.riskLevel != RiskLevel.BAIXO) {
                conditions.add("Deploy gradual (canary deployment recomendado)");
            }
        }

        return conditions;
    }

    /**
     * Gera explicação detalhada
     */
    private String generateExplanation(DecisionContext context, 
                                      BusinessImpactResponse impactAnalysis) {
        StringBuilder explanation = new StringBuilder();

        explanation.append("Análise de Decisão: ");

        if (impactAnalysis.getImpactedBusinessRules().isEmpty()) {
            explanation.append("A mudança não impacta diretamente regras de negócio catalogadas. ");
        } else {
            explanation.append(String.format(
                "A mudança impacta %d regra(s) de negócio. ",
                impactAnalysis.getImpactedBusinessRules().size()
            ));
        }

        explanation.append(String.format(
            "Ambiente: %s. Tipo de mudança: %s. Risco calculado: %s. ",
            context.environment.getDescription(),
            context.changeType.getDescription(),
            context.riskLevel.getDescription()
        ));

        if (context.hasCriticalIncidents) {
            explanation.append("ATENÇÃO: Histórico de incidentes críticos detectado nas regras impactadas. ");
        }

        return explanation.toString();
    }

    /**
     * Valida requisição
     */
    private void validateRequest(RiskDecisionRequest request) {
        if (request.getPullRequestId() == null || request.getPullRequestId().trim().isEmpty()) {
            throw new IllegalArgumentException("pullRequestId é obrigatório");
        }

        if (request.getEnvironment() == null) {
            throw new IllegalArgumentException("environment é obrigatório");
        }

        if (request.getChangeType() == null) {
            throw new IllegalArgumentException("changeType é obrigatório");
        }

        if (request.getChangedFiles() == null || request.getChangedFiles().isEmpty()) {
            throw new IllegalArgumentException("changedFiles não pode ser vazio");
        }
    }

    /**
     * Classe auxiliar para contexto de decisão
     */
    private static class DecisionContext {
        final Environment environment;
        final ChangeType changeType;
        final RiskLevel riskLevel;
        final boolean hasCriticalIncidents;
        final ApprovalPolicy policy;

        DecisionContext(Environment environment, ChangeType changeType, RiskLevel riskLevel,
                       boolean hasCriticalIncidents, ApprovalPolicy policy) {
            this.environment = environment;
            this.changeType = changeType;
            this.riskLevel = riskLevel;
            this.hasCriticalIncidents = hasCriticalIncidents;
            this.policy = policy;
        }
    }
}

