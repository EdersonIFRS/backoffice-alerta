package com.backoffice.alerta.git.client;

import com.backoffice.alerta.git.dto.GitPullRequestData;
import com.backoffice.alerta.git.dto.GitPullRequestRequest;

/**
 * US#51 - Interface para integração Git (Read-Only)
 * 
 * Responsabilidade: buscar metadados de Pull Request
 * 
 * ⚠️ READ-ONLY TOTAL:
 * - NÃO clonar repositório
 * - NÃO ler código-fonte
 * - NÃO alterar nada
 * - Apenas metadados (arquivos alterados)
 */
public interface GitProviderClient {

    /**
     * Busca metadados de um Pull Request
     * 
     * @param request dados da requisição
     * @return metadados do PR (arquivos alterados)
     */
    GitPullRequestData fetchPullRequest(GitPullRequestRequest request);
}
