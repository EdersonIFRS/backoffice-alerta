// US#48 - Repository JPA para Projetos
package com.backoffice.alerta.project.repository;

import com.backoffice.alerta.project.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * US#48 - Repositório JPA para gestão de Projetos Organizacionais.
 * 
 * Fornece queries customizadas para busca por nome e status ativo.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    /**
     * Busca projeto por nome (case-insensitive).
     * 
     * Usado para validação de unicidade de nome.
     * 
     * @param name Nome do projeto
     * @return Optional contendo projeto se encontrado
     */
    Optional<Project> findByNameIgnoreCase(String name);

    /**
     * Busca todos os projetos ativos.
     * 
     * Projetos desativados são excluídos desta lista.
     * 
     * @return Lista de projetos com active=true
     */
    List<Project> findByActiveTrue();
}
