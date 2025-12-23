package com.backoffice.alerta.rules;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Repositório em memória para mapeamentos entre arquivos e regras de negócio
 */
@Repository
public class FileBusinessRuleMappingRepository {

    private final Map<String, FileBusinessRuleMapping> storage = new ConcurrentHashMap<>();

    /**
     * Salva um mapeamento
     * @param mapping Mapeamento a ser salvo
     * @return Mapeamento salvo
     */
    public FileBusinessRuleMapping save(FileBusinessRuleMapping mapping) {
        storage.put(mapping.getId(), mapping);
        return mapping;
    }

    /**
     * Busca todos os mapeamentos
     * @return Lista de todos os mapeamentos
     */
    public List<FileBusinessRuleMapping> findAll() {
        return storage.values().stream().toList();
    }

    /**
     * Busca mapeamentos por caminho de arquivo
     * @param filePath Caminho do arquivo
     * @return Lista de mapeamentos do arquivo
     */
    public List<FileBusinessRuleMapping> findByFilePath(String filePath) {
        return storage.values().stream()
            .filter(mapping -> mapping.getFilePath().equals(filePath))
            .collect(Collectors.toList());
    }

    /**
     * Busca mapeamentos por ID de regra de negócio
     * @param businessRuleId ID da regra de negócio
     * @return Lista de mapeamentos da regra
     */
    public List<FileBusinessRuleMapping> findByBusinessRuleId(String businessRuleId) {
        return storage.values().stream()
            .filter(mapping -> mapping.getBusinessRuleId().equals(businessRuleId))
            .collect(Collectors.toList());
    }

    /**
     * Verifica se já existe um mapeamento para o mesmo arquivo e regra
     * @param filePath Caminho do arquivo
     * @param businessRuleId ID da regra de negócio
     * @return true se existe, false caso contrário
     */
    public boolean existsByFilePathAndBusinessRuleId(String filePath, String businessRuleId) {
        return storage.values().stream()
            .anyMatch(mapping -> 
                mapping.getFilePath().equals(filePath) && 
                mapping.getBusinessRuleId().equals(businessRuleId)
            );
    }
}
