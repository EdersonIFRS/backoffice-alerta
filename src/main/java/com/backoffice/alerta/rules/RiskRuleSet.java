package com.backoffice.alerta.rules;

import com.backoffice.alerta.dto.PullRequestRequest;

/**
 * Interface que define um conjunto de regras de risco
 */
public interface RiskRuleSet {

    /**
     * Retorna a versão das regras
     */
    String getVersion();

    /**
     * Verifica se um arquivo é crítico baseado no caminho
     */
    boolean isCriticalFile(String filePath);

    /**
     * Verifica se um arquivo é semi-crítico baseado no caminho
     */
    boolean isSemiCriticalFile(String filePath);

    /**
     * Retorna o número de incidentes históricos baseado no caminho
     */
    int getIncidentHistory(String filePath);

    /**
     * Retorna a pontuação para arquivo crítico
     */
    int getCriticalFileScore();

    /**
     * Retorna a pontuação para arquivo semi-crítico
     */
    int getSemiCriticalFileScore();

    /**
     * Retorna a pontuação para arquivos com mais de 100 linhas
     */
    int getLinesOver100Score();

    /**
     * Retorna a pontuação para arquivos entre 50 e 100 linhas
     */
    int getLines50To100Score();

    /**
     * Retorna a pontuação para arquivos sem testes
     */
    int getNoTestScore();

    /**
     * Retorna a pontuação por incidente
     */
    int getIncidentScore();

    /**
     * Retorna a pontuação máxima de incidentes
     */
    int getMaxIncidentScore();

    /**
     * Retorna a pontuação máxima total
     */
    int getMaxScore();
}
