package com.backoffice.alerta.service;

import com.backoffice.alerta.dto.BusinessRuleRequest;
import com.backoffice.alerta.dto.BusinessRuleResponse;
import com.backoffice.alerta.rules.BusinessRule;
import com.backoffice.alerta.rules.BusinessRuleRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço para gerenciar regras de negócio
 */
@Service
public class BusinessRuleService {

    private final BusinessRuleRepository repository;

    public BusinessRuleService(BusinessRuleRepository repository) {
        this.repository = repository;
    }

    /**
     * Cria uma nova regra de negócio
     * @param request Dados da regra a ser criada
     * @return Resposta com a regra criada
     * @throws IllegalArgumentException se o ID já existir
     */
    public BusinessRuleResponse create(BusinessRuleRequest request) {
        if (repository.existsById(request.getId())) {
            throw new IllegalArgumentException("Já existe uma regra com o ID: " + request.getId());
        }

        BusinessRule rule = new BusinessRule(
            request.getId(),
            request.getName(),
            request.getDomain(),
            request.getDescription(),
            request.getCriticality(),
            request.getOwner()
        );

        BusinessRule savedRule = repository.save(rule);
        return toResponse(savedRule);
    }

    /**
     * Busca todas as regras de negócio
     * @return Lista de regras
     */
    public List<BusinessRuleResponse> findAll() {
        return repository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Busca uma regra de negócio por ID
     * @param id ID da regra
     * @return Resposta com a regra encontrada
     * @throws IllegalArgumentException se a regra não for encontrada
     */
    public BusinessRuleResponse findById(String id) {
        BusinessRule rule = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Regra não encontrada: " + id));
        return toResponse(rule);
    }

    /**
     * Converte entidade para DTO de resposta
     * @param rule Entidade
     * @return DTO de resposta
     */
    private BusinessRuleResponse toResponse(BusinessRule rule) {
        return new BusinessRuleResponse(
            rule.getId(),
            rule.getName(),
            rule.getDomain(),
            rule.getDescription(),
            rule.getCriticality(),
            rule.getOwner(),
            rule.getCreatedAt(),
            rule.getUpdatedAt()
        );
    }
}

