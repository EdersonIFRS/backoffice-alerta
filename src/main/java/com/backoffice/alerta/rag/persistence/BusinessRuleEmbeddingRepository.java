package com.backoffice.alerta.rag.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository para acesso a embeddings persistidos
 * 
 * US#66 - Persistência de Vetores (Vector DB)
 */
@Repository
public interface BusinessRuleEmbeddingRepository extends JpaRepository<BusinessRuleEmbeddingEntity, UUID> {
    
    /**
     * Busca todos os embeddings de um determinado provider
     * 
     * @param provider Tipo de provider (DUMMY, SENTENCE_TRANSFORMER, OPENAI)
     * @return Lista de embeddings do provider
     */
    List<BusinessRuleEmbeddingEntity> findByProvider(String provider);
    
    /**
     * Conta quantos embeddings existem para um provider
     * 
     * @param provider Tipo de provider
     * @return Quantidade de embeddings
     */
    long countByProvider(String provider);
    
    /**
     * Verifica se existe embedding para uma regra específica
     * 
     * @param businessRuleId ID da regra
     * @return true se existe
     */
    boolean existsByBusinessRuleId(UUID businessRuleId);
    
    /**
     * Busca embeddings por dimensão (útil para validação)
     * 
     * @param dimension Dimensão do vetor
     * @return Lista de embeddings com essa dimensão
     */
    @Query("SELECT e FROM BusinessRuleEmbeddingEntity e WHERE e.dimension = :dimension")
    List<BusinessRuleEmbeddingEntity> findByDimension(int dimension);
}
