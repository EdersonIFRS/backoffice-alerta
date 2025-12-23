package com.backoffice.alerta.provider;

import com.backoffice.alerta.provider.dto.PullRequestData;

/**
 * Interface que define o contrato para provedores de dados de Pull Request.
 * Implementações podem buscar dados de GitHub, GitLab, Bitbucket ou APIs internas.
 */
public interface PullRequestProvider {
    
    /**
     * Busca dados completos de um Pull Request.
     * 
     * @param pullRequestId Identificador único do Pull Request
     * @return Dados do Pull Request incluindo arquivos alterados
     */
    PullRequestData fetch(String pullRequestId);
}
