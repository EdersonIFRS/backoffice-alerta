package com.backoffice.alerta.service;

import com.backoffice.alerta.dto.*;
import com.backoffice.alerta.notification.*;
import com.backoffice.alerta.rules.*;
import com.backoffice.alerta.repository.RiskNotificationRepository;
import com.backoffice.alerta.repository.BusinessRuleIncidentRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço de geração de notificações organizacionais
 * 
 * Responsável por:
 * - Gerar notificações automaticamente após decisões de risco
 * - Identificar times responsáveis via ownership (US#26)
 * - Aplicar regras de priorização (PRIMARY → SECONDARY → BACKUP)
 * - Determinar severidade e gatilhos
 * 
 * ⚠️ NÃO envia mensagens reais (apenas cria eventos imutáveis)
 */
@Service
public class RiskNotificationService {

    private final RiskNotificationRepository notificationRepository;
    private final BusinessRuleOwnershipRepository ownershipRepository;
    private final BusinessRuleIncidentRepository incidentRepository;
    private final RiskSlaService slaService;

    public RiskNotificationService(RiskNotificationRepository notificationRepository,
                                  BusinessRuleOwnershipRepository ownershipRepository,
                                  BusinessRuleIncidentRepository incidentRepository,
                                  RiskSlaService slaService) {
        this.notificationRepository = notificationRepository;
        this.ownershipRepository = ownershipRepository;
        this.incidentRepository = incidentRepository;
        this.slaService = slaService;
    }

    /**
     * Gera notificações automaticamente após decisão de risco
     * 
     * @param request Requisição original da decisão
     * @param response Resposta com a decisão final
     * @param auditId ID da auditoria criada (US#20)
     * @param impactedRules Regras impactadas da análise (US#16)
     */
    public void generateNotifications(RiskDecisionRequest request,
                                     RiskDecisionResponse response,
                                     UUID auditId,
                                     List<ImpactedBusinessRuleResponse> impactedRules) {
        
        // Verifica se deve gerar notificações
        if (!shouldNotify(response)) {
            return;
        }

        // Determina gatilho e severidade
        NotificationTrigger trigger = determineTrigger(response, request.getEnvironment());
        NotificationSeverity severity = determineSeverity(response, request.getEnvironment());

        // Para cada regra impactada, notifica os responsáveis
        for (ImpactedBusinessRuleResponse rule : impactedRules) {
            try {
                UUID ruleId = UUID.fromString(rule.getBusinessRuleId());
                
                // Busca ownerships da regra
                List<BusinessRuleOwnership> ownerships = 
                    ownershipRepository.findByBusinessRuleId(ruleId);
                
                if (ownerships.isEmpty()) {
                    continue; // Sem ownership, não gera notificação
                }

                // Filtra ownerships que devem ser notificados
                List<BusinessRuleOwnership> toNotify = filterOwnershipsToNotify(
                    ownerships, 
                    request.getEnvironment()
                );

                // Busca histórico de incidentes
                UUID businessRuleUuid = UUID.fromString(rule.getBusinessRuleId());
                List<BusinessRuleIncident> incidents = 
                    incidentRepository.findByBusinessRuleIdOrderByOccurredAtDesc(businessRuleUuid);

                // Gera notificação para cada ownership
                for (BusinessRuleOwnership ownership : toNotify) {
                    String message = buildMessage(
                        request, 
                        response, 
                        rule, 
                        incidents
                    );

                    NotificationChannel channel = determineChannel(ownership.getTeamType());

                    RiskNotification notification = new RiskNotification(
                        auditId,
                        request.getPullRequestId(),
                        ruleId,
                        ownership.getTeamName(),
                        ownership.getTeamType(),
                        ownership.getRole(),
                        trigger,
                        severity,
                        channel,
                        message
                    );

                    RiskNotification savedNotification = notificationRepository.save(notification);

                    // Cria SLA automaticamente para notificações críticas (US#28)
                    if (severity == NotificationSeverity.CRITICAL) {
                        slaService.createSlaForNotification(savedNotification);
                    }
                }
            } catch (IllegalArgumentException e) {
                // ID inválido, ignora e continua
                continue;
            }
        }
    }

    /**
     * Verifica se deve gerar notificações baseado na decisão
     */
    private boolean shouldNotify(RiskDecisionResponse response) {
        FinalDecision decision = response.getFinalDecision();
        
        // BLOQUEADO → sempre notifica
        if (decision == FinalDecision.BLOQUEADO) {
            return true;
        }
        
        // APROVADO_COM_RESTRICOES → sempre notifica
        if (decision == FinalDecision.APROVADO_COM_RESTRICOES) {
            return true;
        }
        
        // APROVADO → não notifica
        return false;
    }

    /**
     * Determina o gatilho da notificação
     */
    private NotificationTrigger determineTrigger(RiskDecisionResponse response, 
                                                Environment environment) {
        FinalDecision decision = response.getFinalDecision();
        RiskLevel riskLevel = response.getRiskLevel();

        if (decision == FinalDecision.BLOQUEADO) {
            return NotificationTrigger.RISK_BLOCKED;
        }

        if (decision == FinalDecision.APROVADO_COM_RESTRICOES) {
            // Se é alto risco em produção, usa gatilho específico
            if ((riskLevel == RiskLevel.ALTO || riskLevel == RiskLevel.CRITICO) 
                && environment.isCritical()) {
                return NotificationTrigger.HIGH_RISK_PRODUCTION;
            }
            return NotificationTrigger.RISK_RESTRICTED;
        }

        return NotificationTrigger.RISK_RESTRICTED; // Default
    }

    /**
     * Determina a severidade da notificação
     */
    private NotificationSeverity determineSeverity(RiskDecisionResponse response, 
                                                  Environment environment) {
        FinalDecision decision = response.getFinalDecision();
        RiskLevel riskLevel = response.getRiskLevel();

        // BLOQUEADO → sempre CRITICAL
        if (decision == FinalDecision.BLOQUEADO) {
            return NotificationSeverity.CRITICAL;
        }

        // Risco CRÍTICO em PRODUÇÃO → CRITICAL
        if (riskLevel == RiskLevel.CRITICO && environment.isCritical()) {
            return NotificationSeverity.CRITICAL;
        }

        // Risco ALTO → WARNING
        if (riskLevel == RiskLevel.ALTO) {
            return NotificationSeverity.WARNING;
        }

        // Demais casos → INFO
        return NotificationSeverity.INFO;
    }

    /**
     * Filtra ownerships que devem receber notificação
     * 
     * Regras:
     * - PRIMARY_OWNER → sempre notificado
     * - SECONDARY_OWNER → sempre notificado se existir
     * - BACKUP → apenas se PRIMARY não existir OU ambiente = PRODUCTION
     */
    private List<BusinessRuleOwnership> filterOwnershipsToNotify(
            List<BusinessRuleOwnership> ownerships, 
            Environment environment) {
        
        List<BusinessRuleOwnership> result = new ArrayList<>();

        // Separa por role
        Optional<BusinessRuleOwnership> primary = ownerships.stream()
            .filter(o -> o.getRole() == OwnershipRole.PRIMARY_OWNER)
            .findFirst();
        
        List<BusinessRuleOwnership> secondaries = ownerships.stream()
            .filter(o -> o.getRole() == OwnershipRole.SECONDARY_OWNER)
            .collect(Collectors.toList());
        
        List<BusinessRuleOwnership> backups = ownerships.stream()
            .filter(o -> o.getRole() == OwnershipRole.BACKUP)
            .collect(Collectors.toList());

        // PRIMARY sempre notificado
        primary.ifPresent(result::add);

        // SECONDARY sempre notificado
        result.addAll(secondaries);

        // BACKUP apenas se PRIMARY não existir OU produção
        if (primary.isEmpty() || environment.isCritical()) {
            result.addAll(backups);
        }

        return result;
    }

    /**
     * Constrói mensagem da notificação
     */
    private String buildMessage(RiskDecisionRequest request,
                               RiskDecisionResponse response,
                               ImpactedBusinessRuleResponse rule,
                               List<BusinessRuleIncident> incidents) {
        
        StringBuilder message = new StringBuilder();

        message.append("🔔 NOTIFICAÇÃO DE RISCO - ");
        message.append(response.getFinalDecision().getDescription());
        message.append("\n\n");

        message.append("📋 Pull Request: ").append(request.getPullRequestId()).append("\n");
        message.append("🏢 Ambiente: ").append(request.getEnvironment().getDescription()).append("\n");
        message.append("⚠️ Nível de Risco: ").append(response.getRiskLevel().getDescription()).append("\n\n");

        message.append("📌 Regra de Negócio Impactada:\n");
        message.append("   - ID: ").append(rule.getBusinessRuleId()).append("\n");
        message.append("   - Nome: ").append(rule.getName()).append("\n");
        message.append("   - Domínio: ").append(rule.getDomain().getDescription()).append("\n");
        message.append("   - Criticidade: ").append(rule.getCriticality().getDescription()).append("\n\n");

        message.append("💡 Motivo: ").append(response.getDecisionReason()).append("\n\n");

        // Incidentes históricos
        if (incidents != null && !incidents.isEmpty()) {
            long criticalCount = incidents.stream()
                .filter(i -> i.getSeverity() == IncidentSeverity.CRITICAL)
                .count();
            
            if (criticalCount > 0) {
                message.append("⚠️ ALERTA: Esta regra possui ")
                    .append(criticalCount)
                    .append(" incidente(s) crítico(s) registrado(s).\n\n");
            }
        }

        // Ações obrigatórias
        if (response.getRequiredActions() != null && !response.getRequiredActions().isEmpty()) {
            message.append("✅ Ações Obrigatórias:\n");
            for (String action : response.getRequiredActions()) {
                message.append("   • ").append(action).append("\n");
            }
        }

        return message.toString();
    }

    /**
     * Determina canal preferencial baseado no tipo de time
     */
    private NotificationChannel determineChannel(TeamType teamType) {
        return switch (teamType) {
            case ENGINEERING, OPERATIONS -> NotificationChannel.SLACK;
            case FINANCE, PRODUCT -> NotificationChannel.EMAIL;
            case SECURITY, RISK -> NotificationChannel.WEBHOOK;
        };
    }

    /**
     * Lista todas as notificações
     */
    public List<RiskNotificationResponse> listAllNotifications() {
        return notificationRepository.findAll().stream()
                .map(RiskNotificationResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Lista notificações por auditoria
     */
    public List<RiskNotificationResponse> listByAuditId(UUID auditId) {
        return notificationRepository.findByAuditIdOrderByCreatedAtDesc(auditId).stream()
                .map(RiskNotificationResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Lista notificações por time
     */
    public List<RiskNotificationResponse> listByTeam(String teamName) {
        return notificationRepository.findByTeamNameOrderByCreatedAtDesc(teamName).stream()
                .map(RiskNotificationResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Gera resumo de notificações
     */
    public NotificationSummaryResponse getSummary() {
        List<RiskNotification> all = notificationRepository.findAll();
        
        int total = all.size();
        
        int critical = (int) all.stream()
            .filter(n -> n.getSeverity() == NotificationSeverity.CRITICAL)
            .count();
        
        Set<String> teams = all.stream()
            .map(RiskNotification::getTeamName)
            .collect(Collectors.toSet());

        return new NotificationSummaryResponse(total, critical, teams);
    }
}

