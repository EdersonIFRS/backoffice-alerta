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
 * US#52 - Implementação REAL do GitHub Provider Client
 * 
 * ⚠️ READ-ONLY absoluto:
 * - Apenas leitura de PRs
 * - Sem commits, comentários ou webhooks
 * - Consultivo 100%
 * 
 * Autenticação via Personal Access Token (variável de ambiente)
 */
@Component
public class GitHubProviderClient implements GitProviderClient {

    private static final Logger log = LoggerFactory.getLogger(GitHubProviderClient.class);
    private static final String GITHUB_API_BASE = "https://api.github.com";
    
    @Value("${git.github.token:}")
    private String githubToken;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GitHubProviderClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public GitPullRequestData fetchPullRequest(GitPullRequestRequest request) {
        log.info("🔗 [GITHUB] Conectando ao GitHub para buscar PR #{} do repositório {}", 
                request.getPullRequestNumber(), request.getRepositoryUrl());

        // Validar token
        if (githubToken == null || githubToken.trim().isEmpty()) {
            log.warn("⚠️ [GITHUB] Token não configurado. Use variável de ambiente GITHUB_TOKEN");
            throw new IllegalStateException("GitHub token não configurado");
        }

        try {
            // Extrair owner/repo da URL
            String[] ownerAndRepo = extractOwnerAndRepo(request.getRepositoryUrl());
            String owner = ownerAndRepo[0];
            String repo = ownerAndRepo[1];
            String prNumber = request.getPullRequestNumber();

            // 1. Buscar dados básicos do PR
            String prUrl = String.format("%s/repos/%s/%s/pulls/%s", GITHUB_API_BASE, owner, repo, prNumber);
            JsonNode prData = makeGitHubRequest(prUrl);
            
            log.info("📄 [GITHUB] Pull Request encontrado: {}", prData.path("title").asText());

            // 2. Buscar arquivos alterados do PR
            String filesUrl = String.format("%s/repos/%s/%s/pulls/%s/files", GITHUB_API_BASE, owner, repo, prNumber);
            JsonNode filesData = makeGitHubRequest(filesUrl);
            
            log.info("📂 [GITHUB] {} arquivo(s) alterado(s) carregados", filesData.size());

            // 3. Converter para modelo interno
            return convertToPullRequestData(request.getPullRequestNumber(), prData, filesData);

        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("❌ [GITHUB] Token inválido ou expirado (401 Unauthorized)");
            throw new IllegalStateException("GitHub token inválido ou expirado", e);
        } catch (HttpClientErrorException.Forbidden e) {
            log.error("❌ [GITHUB] Acesso negado - verifique permissões do token (403 Forbidden)");
            throw new IllegalStateException("GitHub token sem permissões suficientes", e);
        } catch (HttpClientErrorException.NotFound e) {
            log.error("❌ [GITHUB] Repositório ou PR não encontrado (404 Not Found)");
            throw new IllegalArgumentException("Repositório ou Pull Request não encontrado no GitHub", e);
        } catch (Exception e) {
            log.error("❌ [GITHUB] Erro ao buscar PR: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar Pull Request do GitHub: " + e.getMessage(), e);
        }
    }

    /**
     * Faz requisição autenticada para API do GitHub
     */
    private JsonNode makeGitHubRequest(String url) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        headers.set("Accept", "application/vnd.github+json");
        headers.set("X-GitHub-Api-Version", "2022-11-28");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return objectMapper.readTree(response.getBody());
    }

    /**
     * Converte resposta da API GitHub para modelo interno
     */
    private GitPullRequestData convertToPullRequestData(String prNumber, JsonNode prData, JsonNode filesData) {
        GitPullRequestData result = new GitPullRequestData();
        
        // Metadados básicos
        result.setPullRequestId("PR-" + prNumber);
        result.setTitle(prData.path("title").asText());
        result.setAuthor(prData.path("user").path("login").asText());
        result.setSourceBranch(prData.path("head").path("ref").asText());
        result.setTargetBranch(prData.path("base").path("ref").asText());
        
        // Status do PR
        String state = prData.path("state").asText();
        boolean merged = prData.path("merged").asBoolean(false);
        
        if (merged) {
            result.setStatus(PullRequestStatus.MERGED);
        } else if ("open".equalsIgnoreCase(state)) {
            result.setStatus(PullRequestStatus.OPEN);
        } else {
            result.setStatus(PullRequestStatus.CLOSED);
        }

        // Arquivos alterados
        List<GitPullRequestFile> changedFiles = new ArrayList<>();
        if (filesData.isArray()) {
            for (JsonNode fileNode : filesData) {
                String filename = fileNode.path("filename").asText();
                String status = fileNode.path("status").asText();
                
                // Mapear status do GitHub para changeType
                String changeType = mapGitHubStatus(status);
                
                changedFiles.add(new GitPullRequestFile(filename, changeType));
            }
        }
        result.setChangedFiles(changedFiles);

        return result;
    }

    /**
     * Mapeia status do GitHub para changeType padrão
     */
    private String mapGitHubStatus(String githubStatus) {
        switch (githubStatus.toLowerCase()) {
            case "added":
                return "ADDED";
            case "removed":
                return "DELETED";
            case "modified":
            case "changed":
                return "MODIFIED";
            case "renamed":
                return "MODIFIED"; // Renomeação tratada como modificação
            default:
                return "MODIFIED";
        }
    }

    /**
     * Extrai owner e repo de URL do GitHub
     * 
     * Suporta formatos:
     * - https://github.com/owner/repo
     * - https://github.com/owner/repo.git
     * - git@github.com:owner/repo.git
     */
    private String[] extractOwnerAndRepo(String repositoryUrl) {
        // Padrão para HTTPS
        Pattern httpsPattern = Pattern.compile("github\\.com[:/]([^/]+)/([^/.]+)");
        Matcher matcher = httpsPattern.matcher(repositoryUrl);
        
        if (matcher.find()) {
            String owner = matcher.group(1);
            String repo = matcher.group(2).replace(".git", "");
            return new String[]{owner, repo};
        }
        
        throw new IllegalArgumentException("URL do repositório inválida para GitHub: " + repositoryUrl);
    }
}
