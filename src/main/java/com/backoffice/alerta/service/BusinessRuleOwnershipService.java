package com.backoffice.alerta.service;

import com.backoffice.alerta.dto.BusinessRuleOwnershipRequest;
import com.backoffice.alerta.dto.BusinessRuleOwnershipResponse;
import com.backoffice.alerta.rules.BusinessRuleOwnership;
import com.backoffice.alerta.rules.BusinessRuleOwnershipRepository;
import com.backoffice.alerta.rules.BusinessRuleRepository;
import com.backoffice.alerta.rules.OwnershipRole;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service para gerenciar ownership organizacional de regras de negócio
 * 
 * Coordena a atribuição de responsabilidade (ownership) de regras a times/equipes
 * organizacionais, com validações de negócio:
 * 
 * - Apenas 1 PRIMARY_OWNER por regra
 * - Regra deve existir antes de atribuir ownership
 * - Múltiplos SECONDARY_OWNER e BACKUP permitidos
 */
@Service
public class BusinessRuleOwnershipService {

    private final BusinessRuleOwnershipRepository ownershipRepository;
    private final BusinessRuleRepository businessRuleRepository;

    public BusinessRuleOwnershipService(BusinessRuleOwnershipRepository ownershipRepository,
                                       BusinessRuleRepository businessRuleRepository) {
        this.ownershipRepository = ownershipRepository;
        this.businessRuleRepository = businessRuleRepository;
    }

    /**
     * Cria um novo ownership organizacional
     * 
     * Validações:
     * - Regra de negócio deve existir
     * - Apenas 1 PRIMARY_OWNER por regra (falha se já existir)
     * - Campos obrigatórios: businessRuleId, teamName, teamType, role, contactEmail
     */
    public BusinessRuleOwnershipResponse createOwnership(BusinessRuleOwnershipRequest request) {
        // Validação 1: campos obrigatórios
        if (request.getBusinessRuleId() == null) {
            throw new IllegalArgumentException("businessRuleId é obrigatório");
        }
        if (request.getTeamName() == null || request.getTeamName().isBlank()) {
            throw new IllegalArgumentException("teamName é obrigatório");
        }
        if (request.getTeamType() == null) {
            throw new IllegalArgumentException("teamType é obrigatório");
        }
        if (request.getRole() == null) {
            throw new IllegalArgumentException("role é obrigatório");
        }
        if (request.getContactEmail() == null || request.getContactEmail().isBlank()) {
            throw new IllegalArgumentException("contactEmail é obrigatório");
        }

        // Validação 2: regra de negócio existe
        if (businessRuleRepository.findById(request.getBusinessRuleId().toString()) == null) {
            throw new IllegalArgumentException(
                "Regra de negócio não encontrada: " + request.getBusinessRuleId()
            );
        }

        // Validação 3: apenas 1 PRIMARY_OWNER por regra
        if (request.getRole() == OwnershipRole.PRIMARY_OWNER) {
            boolean primaryExists = ownershipRepository.existsByBusinessRuleIdAndRole(
                request.getBusinessRuleId(),
                OwnershipRole.PRIMARY_OWNER
            );
            
            if (primaryExists) {
                throw new IllegalStateException(
                    "Já existe um PRIMARY_OWNER para a regra " + request.getBusinessRuleId() + 
                    ". Remova o ownership existente antes de criar um novo PRIMARY_OWNER."
                );
            }
        }

        // Criar entidade imutável
        BusinessRuleOwnership ownership = new BusinessRuleOwnership(
            request.getBusinessRuleId(),
            request.getTeamName(),
            request.getTeamType(),
            request.getRole(),
            request.getContactEmail(),
            request.isApprovalRequired()
        );

        // Salvar e retornar
        BusinessRuleOwnership saved = ownershipRepository.save(ownership);
        return new BusinessRuleOwnershipResponse(saved);
    }

    /**
     * Lista todos os ownerships cadastrados
     */
    public List<BusinessRuleOwnershipResponse> listAllOwnerships() {
        return ownershipRepository.findAll().stream()
                .map(BusinessRuleOwnershipResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Lista ownerships de uma regra específica
     * Retorna PRIMARY, SECONDARY e BACKUP owners
     */
    public List<BusinessRuleOwnershipResponse> listOwnershipsByRule(UUID businessRuleId) {
        return ownershipRepository.findByBusinessRuleId(businessRuleId).stream()
                .map(BusinessRuleOwnershipResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Remove um ownership pelo ID
     * Retorna true se removido, false se não encontrado
     */
    public boolean deleteOwnership(UUID id) {
        return ownershipRepository.deleteById(id);
    }
}

