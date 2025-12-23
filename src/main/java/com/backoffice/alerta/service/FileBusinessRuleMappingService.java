package com.backoffice.alerta.service;

import com.backoffice.alerta.dto.FileBusinessRuleMappingRequest;
import com.backoffice.alerta.dto.FileBusinessRuleMappingResponse;
import com.backoffice.alerta.rules.BusinessRuleRepository;
import com.backoffice.alerta.rules.FileBusinessRuleMapping;
import com.backoffice.alerta.rules.FileBusinessRuleMappingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço para gerenciar mapeamentos entre arquivos e regras de negócio
 */
@Service
public class FileBusinessRuleMappingService {

    private final FileBusinessRuleMappingRepository mappingRepository;
    private final BusinessRuleRepository businessRuleRepository;

    public FileBusinessRuleMappingService(FileBusinessRuleMappingRepository mappingRepository,
                                         BusinessRuleRepository businessRuleRepository) {
        this.mappingRepository = mappingRepository;
        this.businessRuleRepository = businessRuleRepository;
    }

    /**
     * Cria um novo mapeamento entre arquivo e regra de negócio
     * @param request Dados do mapeamento
     * @return Resposta com o mapeamento criado
     * @throws IllegalArgumentException se a regra não existir ou se o mapeamento for duplicado
     */
    public FileBusinessRuleMappingResponse create(FileBusinessRuleMappingRequest request) {
        // Valida se a regra de negócio existe
        if (!businessRuleRepository.existsById(request.getBusinessRuleId())) {
            throw new IllegalArgumentException(
                "Regra de negócio não encontrada: " + request.getBusinessRuleId()
            );
        }

        // Valida duplicidade
        if (mappingRepository.existsByFilePathAndBusinessRuleId(
                request.getFilePath(), request.getBusinessRuleId())) {
            throw new IllegalArgumentException(
                "Já existe um mapeamento para o arquivo '" + request.getFilePath() + 
                "' com a regra '" + request.getBusinessRuleId() + "'"
            );
        }

        FileBusinessRuleMapping mapping = new FileBusinessRuleMapping(
            request.getFilePath(),
            request.getBusinessRuleId(),
            request.getImpactType()
        );

        FileBusinessRuleMapping savedMapping = mappingRepository.save(mapping);
        return toResponse(savedMapping);
    }

    /**
     * Busca todos os mapeamentos
     * @return Lista de mapeamentos
     */
    public List<FileBusinessRuleMappingResponse> findAll() {
        return mappingRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Busca mapeamentos por caminho de arquivo
     * @param filePath Caminho do arquivo
     * @return Lista de mapeamentos do arquivo
     */
    public List<FileBusinessRuleMappingResponse> findByFilePath(String filePath) {
        return mappingRepository.findByFilePath(filePath).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Busca mapeamentos por ID de regra de negócio
     * @param businessRuleId ID da regra de negócio
     * @return Lista de mapeamentos da regra
     */
    public List<FileBusinessRuleMappingResponse> findByBusinessRuleId(String businessRuleId) {
        return mappingRepository.findByBusinessRuleId(businessRuleId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Converte entidade para DTO de resposta
     * @param mapping Entidade
     * @return DTO de resposta
     */
    private FileBusinessRuleMappingResponse toResponse(FileBusinessRuleMapping mapping) {
        return new FileBusinessRuleMappingResponse(
            mapping.getId(),
            mapping.getFilePath(),
            mapping.getBusinessRuleId(),
            mapping.getImpactType(),
            mapping.getCreatedAt(),
            mapping.getUpdatedAt()
        );
    }
}

