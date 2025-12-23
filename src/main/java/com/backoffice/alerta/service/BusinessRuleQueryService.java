package com.backoffice.alerta.service;

import com.backoffice.alerta.dto.BusinessRuleExplanationResponse;
import com.backoffice.alerta.dto.BusinessRuleSearchResponse;
import com.backoffice.alerta.repository.BusinessRuleIncidentRepository;
import com.backoffice.alerta.rules.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service para consulta inteligente e explicação de regras de negócio
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
@Service
public class BusinessRuleQueryService {

    private static final Logger log = LoggerFactory.getLogger(BusinessRuleQueryService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final BusinessRuleRepository businessRuleRepository;
    private final FileBusinessRuleMappingRepository fileMappingRepository;
    private final BusinessRuleIncidentRepository incidentRepository;
    private final BusinessRuleOwnershipRepository ownershipRepository;

    public BusinessRuleQueryService(
            BusinessRuleRepository businessRuleRepository,
            FileBusinessRuleMappingRepository fileMappingRepository,
            BusinessRuleIncidentRepository incidentRepository,
            BusinessRuleOwnershipRepository ownershipRepository) {
        this.businessRuleRepository = businessRuleRepository;
        this.fileMappingRepository = fileMappingRepository;
        this.incidentRepository = incidentRepository;
        this.ownershipRepository = ownershipRepository;
    }

    /**
     * Busca regras de negócio com filtros opcionais
     */
    public List<BusinessRuleSearchResponse> searchBusinessRules(
            String query,
            Domain domain,
            Criticality criticality,
            Boolean hasIncidents,
            String ownedByTeam) {

        log.info("🔍 Buscando regras: query={}, domain={}, criticality={}, hasIncidents={}, team={}", 
            query, domain, criticality, hasIncidents, ownedByTeam);

        List<BusinessRule> allRules = businessRuleRepository.findAll();

        // Aplicar filtros
        List<BusinessRule> filteredRules = allRules.stream()
            .filter(rule -> matchesQuery(rule, query))
            .filter(rule -> matchesDomain(rule, domain))
            .filter(rule -> matchesCriticality(rule, criticality))
            .filter(rule -> matchesIncidents(rule, hasIncidents))
            .filter(rule -> matchesTeam(rule, ownedByTeam))
            .collect(Collectors.toList());

        log.info("✅ {} regras encontradas", filteredRules.size());

        return filteredRules.stream()
            .map(this::toSearchResponse)
            .collect(Collectors.toList());
    }

    /**
     * Obtém explicação detalhada de uma regra de negócio
     */
    public Optional<BusinessRuleExplanationResponse> explainBusinessRule(String ruleId) {
        log.info("📖 Gerando explicação para regra: {}", ruleId);

        Optional<BusinessRule> ruleOpt = businessRuleRepository.findById(ruleId);
        if (ruleOpt.isEmpty()) {
            log.warn("⚠️ Regra não encontrada: {}", ruleId);
            return Optional.empty();
        }

        BusinessRule rule = ruleOpt.get();
        BusinessRuleExplanationResponse explanation = new BusinessRuleExplanationResponse();

        // Dados básicos
        explanation.setId(rule.getId());
        explanation.setName(rule.getName());
        explanation.setDomain(rule.getDomain());
        explanation.setCriticality(rule.getCriticality());

        // Descrição de negócio
        explanation.setBusinessDescription(generateBusinessDescription(rule));

        // Impacto operacional
        explanation.setOperationalImpact(generateOperationalImpact(rule));

        // Por que importa
        explanation.setWhyItMatters(generateWhyItMatters(rule));

        // Riscos conhecidos
        explanation.setKnownRisks(generateKnownRisks(rule));

        // Incidentes históricos
        explanation.setHistoricalIncidents(generateHistoricalIncidentSummary(rule));

        // Arquivos que implementam
        explanation.setImplementedByFiles(getImplementedFiles(rule.getId()));

        // Times responsáveis
        explanation.setOwnedByTeams(getOwnershipInfo(rule.getId()));

        // Dicas de risco
        explanation.setRiskHints(generateRiskHints(rule, explanation.getHistoricalIncidents()));

        log.info("✅ Explicação gerada com sucesso para regra: {}", ruleId);

        return Optional.of(explanation);
    }

    // ========== Métodos de Filtro ==========

    private boolean matchesQuery(BusinessRule rule, String query) {
        if (query == null || query.isBlank()) return true;
        String lowerQuery = query.toLowerCase();
        return rule.getName().toLowerCase().contains(lowerQuery) ||
               (rule.getDescription() != null && rule.getDescription().toLowerCase().contains(lowerQuery));
    }

    private boolean matchesDomain(BusinessRule rule, Domain domain) {
        return domain == null || rule.getDomain() == domain;
    }

    private boolean matchesCriticality(BusinessRule rule, Criticality criticality) {
        return criticality == null || rule.getCriticality() == criticality;
    }

    private boolean matchesIncidents(BusinessRule rule, Boolean hasIncidents) {
        if (hasIncidents == null) return true;
        UUID ruleUuid = convertToUUID(rule.getId());
        if (ruleUuid == null) return !hasIncidents;
        
        List<BusinessRuleIncident> incidents = incidentRepository.findByBusinessRuleIdOrderByOccurredAtDesc(ruleUuid);
        return hasIncidents ? !incidents.isEmpty() : incidents.isEmpty();
    }

    private boolean matchesTeam(BusinessRule rule, String ownedByTeam) {
        if (ownedByTeam == null || ownedByTeam.isBlank()) return true;
        UUID ruleUuid = convertToUUID(rule.getId());
        if (ruleUuid == null) return false;
        
        List<BusinessRuleOwnership> ownerships = ownershipRepository.findByBusinessRuleId(ruleUuid);
        return ownerships.stream()
            .anyMatch(o -> o.getTeamName().toLowerCase().contains(ownedByTeam.toLowerCase()));
    }

    // ========== Conversão para Response ==========

    private BusinessRuleSearchResponse toSearchResponse(BusinessRule rule) {
        BusinessRuleSearchResponse response = new BusinessRuleSearchResponse();
        response.setId(rule.getId());
        response.setName(rule.getName());
        response.setDomain(rule.getDomain());
        response.setCriticality(rule.getCriticality());
        response.setShortDescription(truncateDescription(rule.getDescription()));
        return response;
    }

    private String truncateDescription(String description) {
        if (description == null) return "Sem descrição disponível";
        if (description.length() <= 100) return description;
        return description.substring(0, 97) + "...";
    }

    // ========== Geração de Explicações (Determinístico) ==========

    private String generateBusinessDescription(BusinessRule rule) {
        if (rule.getDescription() != null && !rule.getDescription().isBlank()) {
            return rule.getDescription();
        }
        return String.format("Regra de negócio '%s' no domínio de %s com criticidade %s.",
            rule.getName(), rule.getDomain().getDescription(), rule.getCriticality().getDescription());
    }

    private String generateOperationalImpact(BusinessRule rule) {
        StringBuilder impact = new StringBuilder();
        
        switch (rule.getCriticality()) {
            case CRITICA -> impact.append("IMPACTO CRÍTICO: Falhas nesta regra podem causar perda financeira significativa, " +
                "violação de compliance ou interrupção de serviço crítico. ");
            case ALTA -> impact.append("IMPACTO ALTO: Falhas podem afetar operações importantes, causar inconsistências de dados " +
                "ou impactar negativamente a experiência do usuário. ");
            case MEDIA -> impact.append("IMPACTO MÉDIO: Falhas podem causar inconveniências operacionais, mas com impacto limitado. ");
            case BAIXA -> impact.append("IMPACTO BAIXO: Falhas têm impacto operacional mínimo e podem ser corrigidas sem urgência. ");
        }

        switch (rule.getDomain()) {
            case PAYMENT -> impact.append("Afeta processamento de pagamentos e transações financeiras.");
            case BILLING -> impact.append("Afeta faturamento e cobrança de clientes.");
            case ORDER -> impact.append("Afeta processamento e gestão de pedidos.");
            case USER -> impact.append("Afeta gestão de usuários e autenticação.");
            case GENERIC -> impact.append("Afeta processos genéricos do sistema.");
        }

        return impact.toString();
    }

    private String generateWhyItMatters(BusinessRule rule) {
        StringBuilder matters = new StringBuilder();

        if (rule.getDomain() == Domain.PAYMENT) {
            matters.append("💰 Impacta diretamente receita e compliance financeiro. ");
        } else if (rule.getDomain() == Domain.BILLING) {
            matters.append("💵 Afeta faturamento e relacionamento com clientes. ");
        } else if (rule.getDomain() == Domain.ORDER) {
            matters.append("📦 Influencia satisfação do cliente e cumprimento de SLAs. ");
        }

        if (rule.getCriticality() == Criticality.CRITICA || rule.getCriticality() == Criticality.ALTA) {
            matters.append("⚠️ Mudanças nesta regra exigem revisão cuidadosa e teste rigoroso. ");
        }

        matters.append("Esta regra faz parte do núcleo de negócio e deve ser mantida com alta qualidade.");

        return matters.toString();
    }

    private List<String> generateKnownRisks(BusinessRule rule) {
        List<String> risks = new ArrayList<>();

        if (rule.getCriticality() == Criticality.CRITICA) {
            risks.add("Risco de impacto financeiro direto em caso de falha");
            risks.add("Requer aprovação de múltiplos stakeholders para mudanças");
        }

        if (rule.getDomain() == Domain.PAYMENT) {
            risks.add("Risco de fraude se validações forem removidas");
            risks.add("Compliance com regulamentações financeiras (PCI-DSS)");
        }

        if (rule.getDomain() == Domain.BILLING) {
            risks.add("Risco de cobrança incorreta levando a chargebacks");
        }

        List<String> files = getImplementedFiles(rule.getId());
        if (files.size() > 5) {
            risks.add("Regra implementada em múltiplos arquivos - alto acoplamento");
        }

        if (risks.isEmpty()) {
            risks.add("Sem riscos críticos conhecidos - monitoramento contínuo recomendado");
        }

        return risks;
    }

    private BusinessRuleExplanationResponse.HistoricalIncidentSummary generateHistoricalIncidentSummary(BusinessRule rule) {
        BusinessRuleExplanationResponse.HistoricalIncidentSummary summary = 
            new BusinessRuleExplanationResponse.HistoricalIncidentSummary();

        UUID ruleUuid = convertToUUID(rule.getId());
        if (ruleUuid == null) {
            summary.setTotalIncidents(0);
            summary.setCriticalIncidents(0);
            summary.setHighIncidents(0);
            summary.setSummary("Sem histórico de incidentes registrados.");
            return summary;
        }

        List<BusinessRuleIncident> incidents = incidentRepository.findByBusinessRuleIdOrderByOccurredAtDesc(ruleUuid);
        
        summary.setTotalIncidents(incidents.size());
        summary.setCriticalIncidents((int) incidents.stream().filter(i -> i.getSeverity() == IncidentSeverity.CRITICAL).count());
        summary.setHighIncidents((int) incidents.stream().filter(i -> i.getSeverity() == IncidentSeverity.HIGH).count());

        if (!incidents.isEmpty()) {
            Instant lastIncident = incidents.get(0).getOccurredAt();
            LocalDateTime lastIncidentDate = LocalDateTime.ofInstant(lastIncident, ZoneId.systemDefault());
            summary.setLastIncidentDate(lastIncidentDate.format(DATE_FORMATTER));
            summary.setSummary(String.format(
                "%d incidentes registrados (%d críticos, %d altos). Último incidente: %s",
                summary.getTotalIncidents(),
                summary.getCriticalIncidents(),
                summary.getHighIncidents(),
                summary.getLastIncidentDate()
            ));
        } else {
            summary.setSummary("✅ Nenhum incidente registrado - regra estável.");
        }

        return summary;
    }

    private List<String> getImplementedFiles(String ruleId) {
        List<FileBusinessRuleMapping> mappings = fileMappingRepository.findByBusinessRuleId(ruleId);
        return mappings.stream()
            .map(FileBusinessRuleMapping::getFilePath)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    private List<BusinessRuleExplanationResponse.OwnershipInfo> getOwnershipInfo(String ruleId) {
        UUID ruleUuid = convertToUUID(ruleId);
        if (ruleUuid == null) return Collections.emptyList();

        List<BusinessRuleOwnership> ownerships = ownershipRepository.findByBusinessRuleId(ruleUuid);
        
        return ownerships.stream()
            .map(o -> {
                BusinessRuleExplanationResponse.OwnershipInfo info = 
                    new BusinessRuleExplanationResponse.OwnershipInfo();
                info.setTeamName(o.getTeamName());
                info.setRole(o.getRole().toString());
                return info;
            })
            .collect(Collectors.toList());
    }

    private List<String> generateRiskHints(BusinessRule rule, BusinessRuleExplanationResponse.HistoricalIncidentSummary incidents) {
        List<String> hints = new ArrayList<>();

        // Hint baseado em criticidade
        if (rule.getCriticality() == Criticality.CRITICA) {
            hints.add("⚠️ CRÍTICO: Qualquer mudança deve passar por revisão executiva");
        }

        // Hint baseado em incidentes
        if (incidents.getTotalIncidents() > 5) {
            hints.add("🔴 ATENÇÃO: Regra com histórico frequente de incidentes - revisar implementação");
        } else if (incidents.getCriticalIncidents() > 0) {
            hints.add("🟠 CUIDADO: Regra já causou incidentes críticos - testes rigorosos obrigatórios");
        } else if (incidents.getTotalIncidents() == 0) {
            hints.add("✅ Regra estável sem histórico de incidentes");
        }

        // Hint baseado em ownership
        List<BusinessRuleExplanationResponse.OwnershipInfo> owners = getOwnershipInfo(rule.getId());
        if (owners.isEmpty()) {
            hints.add("⚠️ SEM OWNERSHIP DEFINIDO: Atribuir time responsável urgentemente");
        } else {
            long primaryOwners = owners.stream().filter(o -> o.getRole().equals("PRIMARY_OWNER")).count();
            if (primaryOwners == 0) {
                hints.add("🟡 Sem PRIMARY_OWNER: Definir responsável principal");
            }
        }

        // Hint baseado em arquivos
        List<String> files = getImplementedFiles(rule.getId());
        if (files.isEmpty()) {
            hints.add("ℹ️ Nenhum arquivo mapeado - considerar documentar implementação");
        } else if (files.size() > 10) {
            hints.add("⚠️ Regra altamente distribuída - considerar refatoração");
        }

        return hints;
    }

    // ========== Utilitários ==========

    private UUID convertToUUID(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
