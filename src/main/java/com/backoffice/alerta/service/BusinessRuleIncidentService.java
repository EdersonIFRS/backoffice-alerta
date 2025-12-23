package com.backoffice.alerta.service;

import com.backoffice.alerta.dto.BusinessRuleIncidentRequest;
import com.backoffice.alerta.dto.BusinessRuleIncidentResponse;
import com.backoffice.alerta.rules.BusinessRuleIncident;
import com.backoffice.alerta.repository.BusinessRuleIncidentRepository;
import com.backoffice.alerta.rules.BusinessRuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço para gerenciar incidentes históricos de regras de negócio
 */
@Service
public class BusinessRuleIncidentService {

    private final BusinessRuleIncidentRepository incidentRepository;
    private final BusinessRuleRepository businessRuleRepository;

    public BusinessRuleIncidentService(BusinessRuleIncidentRepository incidentRepository,
                                      BusinessRuleRepository businessRuleRepository) {
        this.incidentRepository = incidentRepository;
        this.businessRuleRepository = businessRuleRepository;
    }

    /**
     * Cria um novo incidente
     * @param request Dados do incidente
     * @return Resposta com o incidente criado
     * @throws IllegalArgumentException se a regra não existir ou dados forem inválidos
     */
    public BusinessRuleIncidentResponse create(BusinessRuleIncidentRequest request) {
        validateRequest(request);

        // Valida se a regra de negócio existe
        if (!businessRuleRepository.existsById(request.getBusinessRuleId())) {
            throw new IllegalArgumentException(
                "Regra de negócio não encontrada: " + request.getBusinessRuleId()
            );
        }

        UUID businessRuleUuid = UUID.fromString(request.getBusinessRuleId());
        BusinessRuleIncident incident = new BusinessRuleIncident(
            businessRuleUuid,
            request.getTitle(),
            request.getDescription(),
            request.getSeverity(),
            request.getOccurredAt()
        );

        BusinessRuleIncident savedIncident = incidentRepository.save(incident);
        return toResponse(savedIncident);
    }

    /**
     * Busca todos os incidentes
     * @return Lista de incidentes
     */
    public List<BusinessRuleIncidentResponse> findAll() {
        return incidentRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Busca incidentes por ID de regra de negócio
     * @param businessRuleId ID da regra de negócio
     * @return Lista de incidentes da regra
     */
    public List<BusinessRuleIncidentResponse> findByBusinessRuleId(String businessRuleId) {
        UUID businessRuleUuid = UUID.fromString(businessRuleId);
        return incidentRepository.findByBusinessRuleIdOrderByOccurredAtDesc(businessRuleUuid).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Valida a requisição
     */
    private void validateRequest(BusinessRuleIncidentRequest request) {
        if (request.getBusinessRuleId() == null || request.getBusinessRuleId().trim().isEmpty()) {
            throw new IllegalArgumentException("businessRuleId é obrigatório");
        }

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("title é obrigatório");
        }

        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("description é obrigatório");
        }

        if (request.getSeverity() == null) {
            throw new IllegalArgumentException("severity é obrigatório");
        }

        if (request.getOccurredAt() == null) {
            throw new IllegalArgumentException("occurredAt é obrigatório");
        }
    }

    /**
     * Converte entidade para DTO de resposta
     * @param incident Entidade
     * @return DTO de resposta
     */
    private BusinessRuleIncidentResponse toResponse(BusinessRuleIncident incident) {
        return new BusinessRuleIncidentResponse(
            incident.getId().toString(),
            incident.getBusinessRuleId().toString(),
            incident.getTitle(),
            incident.getDescription(),
            incident.getSeverity(),
            incident.getOccurredAt(),
            incident.getCreatedAt()
        );
    }
}

