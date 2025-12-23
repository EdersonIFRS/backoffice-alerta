package com.backoffice.alerta.service;

import com.backoffice.alerta.dto.BusinessImpactRequest;
import com.backoffice.alerta.dto.BusinessImpactResponse;
import com.backoffice.alerta.dto.BusinessRuleOwnershipResponse;
import com.backoffice.alerta.dto.ImpactedBusinessRuleResponse;
import com.backoffice.alerta.rules.*;
import com.backoffice.alerta.repository.BusinessRuleIncidentRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço para análise de impacto de negócio em Pull Requests
 */
@Service
public class BusinessImpactAnalysisService {

    private final FileBusinessRuleMappingRepository mappingRepository;
    private final BusinessRuleRepository businessRuleRepository;
    private final BusinessRuleIncidentRepository incidentRepository;
    private final BusinessRuleOwnershipRepository ownershipRepository;

    public BusinessImpactAnalysisService(FileBusinessRuleMappingRepository mappingRepository,
                                        BusinessRuleRepository businessRuleRepository,
                                        BusinessRuleIncidentRepository incidentRepository,
                                        BusinessRuleOwnershipRepository ownershipRepository) {
        this.mappingRepository = mappingRepository;
        this.businessRuleRepository = businessRuleRepository;
        this.incidentRepository = incidentRepository;
        this.ownershipRepository = ownershipRepository;
    }

    /**
     * Analisa o impacto de negócio de um Pull Request
     * @param request Requisição com arquivos alterados
     * @return Análise de impacto
     * @throws IllegalArgumentException se dados forem inválidos
     */
    public BusinessImpactResponse analyze(BusinessImpactRequest request) {
        validateRequest(request);

        // Mapeia regras de negócio impactadas agrupadas por ID
        Map<String, RuleImpactData> impactedRulesMap = new HashMap<>();

        // Para cada arquivo alterado
        for (String filePath : request.getChangedFiles()) {
            // Busca mapeamentos deste arquivo
            List<FileBusinessRuleMapping> mappings = mappingRepository.findByFilePath(filePath);

            // Para cada mapeamento encontrado
            for (FileBusinessRuleMapping mapping : mappings) {
                String ruleId = mapping.getBusinessRuleId();

                // Se a regra ainda não foi processada, cria nova entrada
                if (!impactedRulesMap.containsKey(ruleId)) {
                    impactedRulesMap.put(ruleId, new RuleImpactData(mapping));
                } else {
                    // Adiciona o arquivo e atualiza o tipo de impacto se necessário
                    RuleImpactData existingData = impactedRulesMap.get(ruleId);
                    existingData.addFile(filePath);
                    
                    // Se houver impacto direto, prioriza sobre indireto
                    if (mapping.getImpactType() == ImpactType.DIRECT) {
                        existingData.setImpactType(ImpactType.DIRECT);
                    }
                }
            }
        }

        // Converte mapa para lista de respostas
        List<ImpactedBusinessRuleResponse> impactedRules = new ArrayList<>();
        RiskLevel overallRisk = null;

        for (Map.Entry<String, RuleImpactData> entry : impactedRulesMap.entrySet()) {
            String ruleId = entry.getKey();
            RuleImpactData impactData = entry.getValue();

            // Busca a regra de negócio completa
            Optional<BusinessRule> ruleOpt = businessRuleRepository.findById(ruleId);
            if (ruleOpt.isPresent()) {
                BusinessRule rule = ruleOpt.get();

                // Busca incidentes históricos da regra
                UUID ruleUuid = UUID.fromString(ruleId);
                List<BusinessRuleIncident> incidents = incidentRepository.findByBusinessRuleIdOrderByOccurredAtDesc(ruleUuid);

                // Calcula o nível de risco base
                RiskLevel riskLevel = RiskLevel.calculateRiskLevel(
                    rule.getCriticality(), 
                    impactData.getImpactType()
                );

                // Ajusta risco com base nos incidentes
                RiskLevel adjustedRiskLevel = adjustRiskLevelWithIncidents(riskLevel, incidents);

                // Gera explicação incluindo incidentes
                String explanation = generateExplanation(rule, impactData, incidents);

                // Busca ownerships da regra
                List<BusinessRuleOwnership> ownerships = ownershipRepository
                    .findByBusinessRuleId(UUID.fromString(ruleId));
                List<BusinessRuleOwnershipResponse> ownershipResponses = ownerships.stream()
                    .map(BusinessRuleOwnershipResponse::new)
                    .collect(Collectors.toList());

                // Cria resposta
                ImpactedBusinessRuleResponse ruleResponse = new ImpactedBusinessRuleResponse(
                    rule.getId(),
                    rule.getName(),
                    rule.getDomain(),
                    rule.getCriticality(),
                    impactData.getImpactType(),
                    new ArrayList<>(impactData.getFiles()),
                    adjustedRiskLevel,
                    explanation,
                    ownershipResponses
                );

                impactedRules.add(ruleResponse);

                // Atualiza risco geral
                overallRisk = RiskLevel.max(overallRisk, adjustedRiskLevel);
            }
        }

        // Se nenhuma regra foi impactada, risco é baixo
        if (overallRisk == null) {
            overallRisk = RiskLevel.BAIXO;
        }

        return new BusinessImpactResponse(
            request.getPullRequestId(),
            impactedRules,
            overallRisk
        );
    }

    /**
     * Valida a requisição
     */
    private void validateRequest(BusinessImpactRequest request) {
        if (request.getPullRequestId() == null || request.getPullRequestId().trim().isEmpty()) {
            throw new IllegalArgumentException("pullRequestId é obrigatório");
        }

        if (request.getChangedFiles() == null || request.getChangedFiles().isEmpty()) {
            throw new IllegalArgumentException("changedFiles não pode ser vazio");
        }
    }

    /**
     * Gera explicação em linguagem de negócio
     */
    private String generateExplanation(BusinessRule rule, RuleImpactData impactData, 
                                      List<BusinessRuleIncident> incidents) {
        StringBuilder explanation = new StringBuilder();

        if (impactData.getImpactType() == ImpactType.DIRECT) {
            if (impactData.getFiles().size() == 1) {
                String fileName = extractFileName(impactData.getFiles().iterator().next());
                explanation.append(String.format(
                    "Arquivo %s implementa diretamente a regra de negócio '%s' (%s)",
                    fileName, rule.getName(), rule.getDescription()
                ));
            } else {
                explanation.append(String.format(
                    "%d arquivos implementam diretamente a regra de negócio '%s' (%s)",
                    impactData.getFiles().size(), rule.getName(), rule.getDescription()
                ));
            }
        } else {
            if (impactData.getFiles().size() == 1) {
                String fileName = extractFileName(impactData.getFiles().iterator().next());
                explanation.append(String.format(
                    "Arquivo %s influencia indiretamente a regra de negócio '%s' (%s)",
                    fileName, rule.getName(), rule.getDescription()
                ));
            } else {
                explanation.append(String.format(
                    "%d arquivos influenciam indiretamente a regra de negócio '%s' (%s)",
                    impactData.getFiles().size(), rule.getName(), rule.getDescription()
                ));
            }
        }

        // Adiciona informação sobre incidentes se houver
        if (incidents != null && !incidents.isEmpty()) {
            long criticalCount = incidents.stream()
                .filter(i -> i.getSeverity() == IncidentSeverity.CRITICAL)
                .count();
            long highCount = incidents.stream()
                .filter(i -> i.getSeverity() == IncidentSeverity.HIGH)
                .count();

            if (criticalCount > 0 || highCount > 0) {
                explanation.append(". ATENÇÃO: A regra '").append(rule.getName()).append("'");
                
                if (criticalCount > 0 && highCount > 0) {
                    explanation.append(" possui ")
                        .append(criticalCount).append(" incidente(s) CRÍTICO(S) e ")
                        .append(highCount).append(" incidente(s) de ALTA severidade");
                } else if (criticalCount > 0) {
                    explanation.append(" possui ")
                        .append(criticalCount).append(" incidente(s) CRÍTICO(S)");
                } else {
                    explanation.append(" possui ")
                        .append(highCount).append(" incidente(s) de ALTA severidade");
                }
                
                explanation.append(" registrados em produção, elevando o risco da mudança");
            } else if (incidents.size() > 0) {
                explanation.append(". A regra possui ")
                    .append(incidents.size())
                    .append(" incidente(s) histórico(s) registrado(s)");
            }
        }

        return explanation.toString();
    }

    /**
     * Ajusta o nível de risco com base nos incidentes históricos
     */
    private RiskLevel adjustRiskLevelWithIncidents(RiskLevel baseRiskLevel, 
                                                   List<BusinessRuleIncident> incidents) {
        if (incidents == null || incidents.isEmpty()) {
            return baseRiskLevel;
        }

        // Calcula peso total dos incidentes
        int totalIncidentWeight = incidents.stream()
            .mapToInt(incident -> incident.getSeverity().getRiskWeight())
            .sum();

        // Não eleva risco se peso for baixo
        if (totalIncidentWeight < 15) {
            return baseRiskLevel;
        }

        // Eleva risco baseado no peso dos incidentes
        if (totalIncidentWeight >= 40) {
            // Peso muito alto: eleva para CRÍTICO
            return RiskLevel.CRITICO;
        } else if (totalIncidentWeight >= 25) {
            // Peso alto: eleva para ALTO se não for CRÍTICO
            return baseRiskLevel == RiskLevel.CRITICO ? RiskLevel.CRITICO : RiskLevel.ALTO;
        } else {
            // Peso médio: eleva um nível (se não for CRÍTICO)
            return switch (baseRiskLevel) {
                case BAIXO -> RiskLevel.MEDIO;
                case MEDIO -> RiskLevel.ALTO;
                case ALTO, CRITICO -> RiskLevel.CRITICO;
            };
        }
    }

    /**
     * Extrai apenas o nome do arquivo do caminho completo
     */
    private String extractFileName(String filePath) {
        if (filePath == null) return "";
        int lastSlash = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }

    /**
     * Classe auxiliar para agregar dados de impacto por regra
     */
    private static class RuleImpactData {
        private ImpactType impactType;
        private final Set<String> files;

        public RuleImpactData(FileBusinessRuleMapping mapping) {
            this.impactType = mapping.getImpactType();
            this.files = new HashSet<>();
            this.files.add(mapping.getFilePath());
        }

        public void addFile(String filePath) {
            files.add(filePath);
        }

        public ImpactType getImpactType() {
            return impactType;
        }

        public void setImpactType(ImpactType impactType) {
            this.impactType = impactType;
        }

        public Set<String> getFiles() {
            return files;
        }
    }
}

