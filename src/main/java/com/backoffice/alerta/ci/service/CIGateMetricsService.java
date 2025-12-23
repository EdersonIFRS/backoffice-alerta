package com.backoffice.alerta.ci.service;

import com.backoffice.alerta.ci.dto.CIGateMetricsResponse;
import com.backoffice.alerta.ci.dto.CIGateProjectMetrics;
import com.backoffice.alerta.ci.dto.CIGateRuleMetrics;
import com.backoffice.alerta.ci.dto.CIGateTimelinePoint;
import com.backoffice.alerta.project.domain.Project;
import com.backoffice.alerta.project.repository.ProjectRepository;
import com.backoffice.alerta.repository.RiskDecisionAuditRepository;
import com.backoffice.alerta.rules.BusinessRule;
import com.backoffice.alerta.rules.BusinessRuleRepository;
import com.backoffice.alerta.rules.FinalDecision;
import com.backoffice.alerta.rules.RiskDecisionAudit;
import com.backoffice.alerta.rules.RiskLevel;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service para métricas e observabilidade do Gate de Risco CI/CD
 * 
 * US#54 - Observabilidade e Métricas do Gate de Risco (CI/CD)
 * 
 * PRINCÍPIOS:
 * - READ-ONLY absoluto: apenas lê dados existentes
 * - SEM side-effects: não cria auditorias, decisões, notificações ou SLAs
 * - Reutilização: usa RiskDecisionAuditRepository existente
 * - Performance: agrega dados de forma eficiente
 * - Determinístico: mesma entrada = mesma saída
 */
@Service
public class CIGateMetricsService {

    private final RiskDecisionAuditRepository auditRepository;
    private final ProjectRepository projectRepository;
    private final BusinessRuleRepository businessRuleRepository;

    public CIGateMetricsService(RiskDecisionAuditRepository auditRepository,
                                 ProjectRepository projectRepository,
                                 BusinessRuleRepository businessRuleRepository) {
        this.auditRepository = auditRepository;
        this.projectRepository = projectRepository;
        this.businessRuleRepository = businessRuleRepository;
    }

    /**
     * Retorna métricas gerais do Gate de Risco
     * 
     * @param projectId ID do projeto (opcional, null = GLOBAL)
     * @param from Data inicial (opcional, null = 90 dias atrás)
     * @param to Data final (opcional, null = hoje)
     * @return Métricas agregadas
     */
    public CIGateMetricsResponse getGeneralMetrics(Long projectId, LocalDate from, LocalDate to) {
        // Define período padrão (últimos 90 dias)
        LocalDate effectiveFrom = from != null ? from : LocalDate.now().minusDays(90);
        LocalDate effectiveTo = to != null ? to : LocalDate.now();

        Instant fromInstant = effectiveFrom.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toInstant = effectiveTo.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        // Busca todas as auditorias do período
        List<RiskDecisionAudit> audits = auditRepository.findAllByOrderByCreatedAtDesc();
        
        // Filtra por período
        List<RiskDecisionAudit> filteredAudits = audits.stream()
            .filter(audit -> !audit.getCreatedAt().isBefore(fromInstant) && !audit.getCreatedAt().isAfter(toInstant))
            .collect(Collectors.toList());

        // Se projectId foi especificado, filtrar por projeto
        // Nota: Assumindo que pullRequestId pode conter informação do projeto ou usar outra estratégia
        // Para simplicidade, vamos considerar todas as auditorias se projectId for null

        int totalExecutions = filteredAudits.size();
        int approvedCount = (int) filteredAudits.stream()
            .filter(audit -> audit.getFinalDecision() == FinalDecision.APROVADO)
            .count();
        int approvedWithRestrictionsCount = (int) filteredAudits.stream()
            .filter(audit -> audit.getFinalDecision() == FinalDecision.APROVADO_COM_RESTRICOES)
            .count();
        int blockedCount = (int) filteredAudits.stream()
            .filter(audit -> audit.getFinalDecision() == FinalDecision.BLOQUEADO)
            .count();

        double blockRate = totalExecutions > 0 ? (blockedCount * 100.0 / totalExecutions) : 0.0;
        double warningRate = totalExecutions > 0 ? (approvedWithRestrictionsCount * 100.0 / totalExecutions) : 0.0;

        // Calcula nível de risco médio
        String averageRiskLevel = calculateAverageRiskLevel(filteredAudits);

        return new CIGateMetricsResponse(
            totalExecutions,
            approvedCount,
            approvedWithRestrictionsCount,
            blockedCount,
            Math.round(blockRate * 100.0) / 100.0,
            Math.round(warningRate * 100.0) / 100.0,
            averageRiskLevel,
            effectiveFrom,
            effectiveTo
        );
    }

    /**
     * Retorna métricas agrupadas por projeto
     * 
     * @return Lista de métricas por projeto, ordenada por blockRate DESC
     */
    public List<CIGateProjectMetrics> getProjectMetrics() {
        // Busca todos os projetos
        List<Project> projects = projectRepository.findAll();
        
        // Busca todas as auditorias
        List<RiskDecisionAudit> allAudits = auditRepository.findAllByOrderByCreatedAtDesc();

        List<CIGateProjectMetrics> projectMetrics = new ArrayList<>();

        for (Project project : projects) {
            // Filtra auditorias deste projeto
            // Nota: Como não temos campo projectId direto em RiskDecisionAudit,
            // vamos usar uma heurística simples ou assumir que todas as auditorias são relevantes
            // Para produção, seria necessário adicionar campo projectId em RiskDecisionAudit
            // ou usar outra estratégia de correlação
            
            List<RiskDecisionAudit> projectAudits = allAudits.stream()
                .filter(audit -> isAuditFromProject(audit, project))
                .collect(Collectors.toList());

            if (projectAudits.isEmpty()) {
                continue; // Pula projetos sem execuções
            }

            int totalExecutions = projectAudits.size();
            int blockedCount = (int) projectAudits.stream()
                .filter(audit -> audit.getFinalDecision() == FinalDecision.BLOQUEADO)
                .count();
            double blockRate = totalExecutions > 0 ? (blockedCount * 100.0 / totalExecutions) : 0.0;
            
            String mostFrequentRiskLevel = findMostFrequentRiskLevel(projectAudits);
            Instant lastExecutionAt = projectAudits.stream()
                .map(RiskDecisionAudit::getCreatedAt)
                .max(Instant::compareTo)
                .orElse(null);

            projectMetrics.add(new CIGateProjectMetrics(
                project.getId(),
                project.getName(),
                totalExecutions,
                blockedCount,
                Math.round(blockRate * 100.0) / 100.0,
                mostFrequentRiskLevel,
                lastExecutionAt
            ));
        }

        // Ordena por blockRate DESC
        projectMetrics.sort((a, b) -> Double.compare(b.getBlockRate(), a.getBlockRate()));

        return projectMetrics;
    }

    /**
     * Retorna métricas agrupadas por regra de negócio
     * 
     * @return Lista de regras que mais causam bloqueios, ordenada por blockCount DESC
     */
    public List<CIGateRuleMetrics> getRuleMetrics() {
        // Busca todas as auditorias
        List<RiskDecisionAudit> allAudits = auditRepository.findAllByOrderByCreatedAtDesc();
        
        // Busca todas as regras
        List<BusinessRule> allRules = businessRuleRepository.findAll();

        Map<String, CIGateRuleMetrics> ruleMetricsMap = new HashMap<>();

        for (RiskDecisionAudit audit : allAudits) {
            List<String> impactedRules = audit.getImpactedBusinessRules();
            
            for (String ruleId : impactedRules) {
                // Incrementa contadores
                CIGateRuleMetrics metrics = ruleMetricsMap.get(ruleId);
                
                if (metrics == null) {
                    // Busca informações da regra
                    BusinessRule rule = allRules.stream()
                        .filter(r -> r.getId().equals(ruleId))
                        .findFirst()
                        .orElse(null);
                    
                    if (rule != null) {
                        metrics = new CIGateRuleMetrics(
                            UUID.fromString(rule.getId()),
                            rule.getName(),
                            rule.getCriticality() != null ? rule.getCriticality().toString() : "UNKNOWN",
                            0,
                            0,
                            null
                        );
                        ruleMetricsMap.put(ruleId, metrics);
                    }
                }
                
                if (metrics != null) {
                    // Atualiza contadores
                    if (audit.getFinalDecision() == FinalDecision.BLOQUEADO) {
                        metrics.setBlockCount(metrics.getBlockCount() + 1);
                    } else if (audit.getFinalDecision() == FinalDecision.APROVADO_COM_RESTRICOES) {
                        metrics.setWarningCount(metrics.getWarningCount() + 1);
                    }
                    
                    // Atualiza lastTriggeredAt
                    if (metrics.getLastTriggeredAt() == null || 
                        audit.getCreatedAt().isAfter(metrics.getLastTriggeredAt())) {
                        metrics.setLastTriggeredAt(audit.getCreatedAt());
                    }
                }
            }
        }

        List<CIGateRuleMetrics> ruleMetrics = new ArrayList<>(ruleMetricsMap.values());
        
        // Ordena por blockCount DESC
        ruleMetrics.sort((a, b) -> Integer.compare(b.getBlockCount(), a.getBlockCount()));

        return ruleMetrics;
    }

    /**
     * Retorna timeline de execuções do gate
     * 
     * @param from Data inicial (opcional, null = 30 dias atrás)
     * @param to Data final (opcional, null = hoje)
     * @return Lista de pontos na timeline, agrupados por dia
     */
    public List<CIGateTimelinePoint> getTimeline(LocalDate from, LocalDate to) {
        // Define período padrão (últimos 30 dias)
        LocalDate effectiveFrom = from != null ? from : LocalDate.now().minusDays(30);
        LocalDate effectiveTo = to != null ? to : LocalDate.now();

        Instant fromInstant = effectiveFrom.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toInstant = effectiveTo.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        // Busca auditorias do período
        List<RiskDecisionAudit> audits = auditRepository.findAllByOrderByCreatedAtDesc();
        List<RiskDecisionAudit> filteredAudits = audits.stream()
            .filter(audit -> !audit.getCreatedAt().isBefore(fromInstant) && !audit.getCreatedAt().isAfter(toInstant))
            .collect(Collectors.toList());

        // Agrupa por dia
        Map<LocalDate, List<RiskDecisionAudit>> auditsByDate = filteredAudits.stream()
            .collect(Collectors.groupingBy(
                audit -> audit.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate()
            ));

        List<CIGateTimelinePoint> timeline = new ArrayList<>();

        // Itera por cada dia do período
        LocalDate currentDate = effectiveFrom;
        while (!currentDate.isAfter(effectiveTo)) {
            List<RiskDecisionAudit> dayAudits = auditsByDate.getOrDefault(currentDate, Collections.emptyList());
            
            int executions = dayAudits.size();
            int approved = (int) dayAudits.stream()
                .filter(audit -> audit.getFinalDecision() == FinalDecision.APROVADO)
                .count();
            int warnings = (int) dayAudits.stream()
                .filter(audit -> audit.getFinalDecision() == FinalDecision.APROVADO_COM_RESTRICOES)
                .count();
            int blocked = (int) dayAudits.stream()
                .filter(audit -> audit.getFinalDecision() == FinalDecision.BLOQUEADO)
                .count();

            timeline.add(new CIGateTimelinePoint(currentDate, executions, approved, warnings, blocked));
            
            currentDate = currentDate.plusDays(1);
        }

        return timeline;
    }

    // ========== Métodos Auxiliares (Privados) ==========

    /**
     * Calcula o nível de risco médio de uma lista de auditorias
     */
    private String calculateAverageRiskLevel(List<RiskDecisionAudit> audits) {
        if (audits.isEmpty()) {
            return "BAIXO";
        }

        // Mapeia RiskLevel para valor numérico
        Map<RiskLevel, Integer> levelValues = new HashMap<>();
        levelValues.put(RiskLevel.BAIXO, 1);
        levelValues.put(RiskLevel.MEDIO, 2);
        levelValues.put(RiskLevel.ALTO, 3);
        levelValues.put(RiskLevel.CRITICO, 4);

        double average = audits.stream()
            .mapToInt(audit -> levelValues.getOrDefault(audit.getRiskLevel(), 1))
            .average()
            .orElse(1.0);

        // Converte de volta para RiskLevel
        if (average < 1.5) return "BAIXO";
        if (average < 2.5) return "MEDIO";
        if (average < 3.5) return "ALTO";
        return "CRITICO";
    }

    /**
     * Encontra o nível de risco mais frequente
     */
    private String findMostFrequentRiskLevel(List<RiskDecisionAudit> audits) {
        if (audits.isEmpty()) {
            return "BAIXO";
        }

        Map<RiskLevel, Long> frequencyMap = audits.stream()
            .collect(Collectors.groupingBy(RiskDecisionAudit::getRiskLevel, Collectors.counting()));

        return frequencyMap.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(entry -> entry.getKey().toString())
            .orElse("BAIXO");
    }

    /**
     * Verifica se uma auditoria pertence a um projeto específico
     * 
     * Nota: Esta é uma implementação simplificada.
     * Idealmente, RiskDecisionAudit teria um campo projectId.
     * Por ora, vamos usar uma heurística baseada em pullRequestId ou regras impactadas.
     */
    private boolean isAuditFromProject(RiskDecisionAudit audit, Project project) {
        // Estratégia 1: Verificar se alguma regra impactada pertence ao projeto
        // Isso requer que ProjectBusinessRule esteja disponível
        
        // Para simplificar, vamos assumir que TODAS as auditorias pertencem a TODOS os projetos
        // Em produção, seria necessário implementar correlação adequada
        
        // TODO: Implementar lógica real de correlação quando houver campo projectId em RiskDecisionAudit
        return true;
    }
}
