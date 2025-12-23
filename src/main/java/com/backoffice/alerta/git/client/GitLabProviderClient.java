package com.backoffice.alerta.git.client;

import com.backoffice.alerta.git.PullRequestStatus;
import com.backoffice.alerta.git.dto.GitPullRequestData;
import com.backoffice.alerta.git.dto.GitPullRequestFile;
import com.backoffice.alerta.git.dto.GitPullRequestRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * US#52 - Implementação REAL do GitLab Provider Client
 * 
 * ⚠️ READ-ONLY absoluto:
 * - Apenas leitura de Merge Requests
 * - Sem commits, comentários ou webhooks
 * - Consultivo 100%
 * 
 * Autenticação via Personal Access Token (variável de ambiente)
 */
@Component
public class GitLabProviderClient implements GitProviderClient {

    private static final Logger log = LoggerFactory.getLogger(GitLabProviderClient.class);
    private static final String GITLAB_API_BASE = "https://gitlab.com/api/v4";
    
    @Value("${git.gitlab.token:}")
    private String gitlabToken;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GitLabProviderClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public GitPullRequestData fetchPullRequest(GitPullRequestRequest request) {
        log.info("🔗 [GITLAB] Conectando ao GitLab para buscar MR #{} do repositório {}", 
                request.getPullRequestNumber(), request.getRepositoryUrl());

        // Validar token
        if (gitlabToken == null || gitlabToken.trim().isEmpty()) {
            log.warn("⚠️ [GITLAB] Token não configurado. Use variável de ambiente GITLAB_TOKEN");
            throw new IllegalStateException("GitLab token não configurado");
        }

        try {
            // Extrair project path da URL (owner/repo → project_id via encode)
            String projectPath = extractProjectPath(request.getRepositoryUrl());
            String encodedPath = projectPath.replace("/", "%2F");
            String mrNumber = request.getPullRequestNumber();

            // 1. Buscar dados básicos do Merge Request
            String mrUrl = String.format("%s/projects/%s/merge_requests/%s", 
                    GITLAB_API_BASE, encodedPath, mrNumber);
            JsonNode mrData = makeGitLabRequest(mrUrl);
            
            log.info("📄 [GITLAB] Merge Request encontrado: {}", mrData.path("title").asText());

            // 2. Buscar arquivos alterados do MR
            String changesUrl = String.format("%s/projects/%s/merge_requests/%s/changes", 
                    GITLAB_API_BASE, encodedPath, mrNumber);
            JsonNode changesData = makeGitLabRequest(changesUrl);
            
            JsonNode changes = changesData.path("changes");
            log.info("📂 [GITLAB] {} arquivo(s) alterado(s) carregados", changes.size());

            // 3. Converter para modelo interno
            return convertToPullRequestData(request.getPullRequestNumber(), mrData, changesData);

        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("❌ [GITLAB] Token inválido ou expirado (401 Unauthorized)");
            throw new IllegalStateException("GitLab token inválido ou expirado", e);
        } catch (HttpClientErrorException.Forbidden e) {
            log.error("❌ [GITLAB] Acesso negado - verifique permissões do token (403 Forbidden)");
            throw new IllegalStateException("GitLab token sem permissões suficientes", e);
        } catch (HttpClientErrorException.NotFound e) {
            log.error("❌ [GITLAB] Projeto ou MR não encontrado (404 Not Found)");
            throw new IllegalArgumentException("Projeto ou Merge Request não encontrado no GitLab", e);
        } catch (Exception e) {
            log.error("❌ [GITLAB] Erro ao buscar MR: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar Merge Request do GitLab: " + e.getMessage(), e);
        }
    }

    /**
     * Faz requisição autenticada para API do GitLab
     */
    private JsonNode makeGitLabRequest(String url) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("PRIVATE-TOKEN", gitlabToken);
        headers.set("Accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return objectMapper.readTree(response.getBody());
    }

    /**
     * Converte resposta da API GitLab para modelo interno
     */
    private GitPullRequestData convertToPullRequestData(String mrNumber, JsonNode mrData, JsonNode changesData) {
        GitPullRequestData result = new GitPullRequestData();
        
        // Metadados básicos
        result.setPullRequestId("MR-" + mrNumber);
        result.setTitle(mrData.path("title").asText());
        result.setAuthor(mrData.path("author").path("username").asText());
        result.setSourceBranch(mrData.path("source_branch").asText());
        result.setTargetBranch(mrData.path("target_branch").asText());
        
        // Status do MR
        String state = mrData.path("state").asText();
        String mergeStatus = mrData.path("merge_status").asText();
        
        if ("merged".equalsIgnoreCase(state)) {
            result.setStatus(PullRequestStatus.MERGED);
        } else if ("opened".equalsIgnoreCase(state)) {
            result.setStatus(PullRequestStatus.OPEN);
        } else {
            result.setStatus(PullRequestStatus.CLOSED);
        }

        // Arquivos alterados
        List<GitPullRequestFile> changedFiles = new ArrayList<>();
        JsonNode changes = changesData.path("changes");
        
        if (changes.isArray()) {
            for (JsonNode changeNode : changes) {
                String oldPath = changeNode.path("old_path").asText();
                String newPath = changeNode.path("new_path").asText();
                boolean newFile = changeNode.path("new_file").asBoolean(false);
                boolean deletedFile = changeNode.path("deleted_file").asBoolean(false);
                boolean renamedFile = changeNode.path("renamed_file").asBoolean(false);
                
                // Determinar changeType
                String changeType;
                String filePath;
                
                if (newFile) {
                    changeType = "ADDED";
                    filePath = newPath;
                } else if (deletedFile) {
                    changeType = "DELETED";
                    filePath = oldPath;
                } else {
                    changeType = "MODIFIED";
                    filePath = newPath; // Usar new_path para renamed também
                }
                
                changedFiles.add(new GitPullRequestFile(filePath, changeType));
            }
        }
        result.setChangedFiles(changedFiles);

        return result;
    }

    /**
     * Extrai project path de URL do GitLab
     * 
     * Suporta formatos:
     * - https://gitlab.com/owner/repo
     * - https://gitlab.com/owner/repo.git
     * - git@gitlab.com:owner/repo.git
     * - https://gitlab.com/group/subgroup/repo
     */
    private String extractProjectPath(String repositoryUrl) {
        // Padrão para HTTPS e SSH
        Pattern pattern = Pattern.compile("gitlab\\.com[:/](.+?)(?:\\.git)?$");
        Matcher matcher = pattern.matcher(repositoryUrl);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        throw new IllegalArgumentException("URL do repositório inválida para GitLab: " + repositoryUrl);
    }
}
