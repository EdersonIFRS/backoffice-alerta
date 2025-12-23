package com.backoffice.alerta.provider;

import com.backoffice.alerta.provider.dto.PullRequestData;
import com.backoffice.alerta.provider.dto.PullRequestFileData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementação real de PullRequestProvider que busca dados do GitHub via API.
 * Requer token de autenticação configurado em application.properties ou variável de ambiente.
 */
@Component("gitHubPullRequestProvider")
public class GitHubPullRequestProvider implements PullRequestProvider {

    private static final String GITHUB_API_BASE = "https://api.github.com";
    
    @Value("${github.token:}")
    private String githubToken;
    
    @Value("${github.repository:}")
    private String githubRepository;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GitHubPullRequestProvider() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public PullRequestData fetch(String pullRequestId) {
        validateConfiguration();
        
        try {
            // 1. Buscar dados básicos do PR
            String prUrl = String.format("%s/repos/%s/pulls/%s", GITHUB_API_BASE, githubRepository, pullRequestId);
            JsonNode prData = makeGitHubRequest(prUrl);
            
            // 2. Buscar arquivos do PR
            String filesUrl = String.format("%s/repos/%s/pulls/%s/files", GITHUB_API_BASE, githubRepository, pullRequestId);
            JsonNode filesData = makeGitHubRequest(filesUrl);
            
            // 3. Converter para modelo interno
            return convertToPullRequestData(pullRequestId, prData, filesData);
            
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Pull Request " + pullRequestId + " não encontrado no GitHub");
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new IllegalStateException("Token GitHub inválido ou expirado. Configure GITHUB_TOKEN.");
        } catch (HttpClientErrorException.Forbidden e) {
            if (e.getMessage().contains("rate limit")) {
                throw new IllegalStateException("Rate limit do GitHub excedido. Aguarde antes de fazer novas requisições.");
            }
            throw new IllegalStateException("Acesso negado ao GitHub: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar Pull Request do GitHub: " + e.getMessage(), e);
        }
    }

    private JsonNode makeGitHubRequest(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + githubToken);
        headers.set("Accept", "application/vnd.github.v3+json");
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        
        try {
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao parsear resposta do GitHub", e);
        }
    }

    private PullRequestData convertToPullRequestData(String pullRequestId, JsonNode prData, JsonNode filesData) {
        String repository = prData.path("base").path("repo").path("full_name").asText();
        String author = prData.path("user").path("login").asText();
        
        List<PullRequestFileData> files = new ArrayList<>();
        if (filesData.isArray()) {
            for (JsonNode fileNode : filesData) {
                String filename = fileNode.path("filename").asText();
                int additions = fileNode.path("additions").asInt(0);
                int deletions = fileNode.path("deletions").asInt(0);
                
                // hasTest permanece null - será inferido pelo sistema se necessário
                files.add(new PullRequestFileData(filename, additions, deletions, null));
            }
        }
        
        return new PullRequestData(pullRequestId, repository, author, files);
    }

    private void validateConfiguration() {
        if (githubToken == null || githubToken.trim().isEmpty()) {
            throw new IllegalStateException(
                "Token GitHub não configurado. " +
                "Configure a variável de ambiente GITHUB_TOKEN ou propriedade github.token"
            );
        }
        
        if (githubRepository == null || githubRepository.trim().isEmpty()) {
            throw new IllegalStateException(
                "Repositório GitHub não configurado. " +
                "Configure a propriedade github.repository (formato: owner/repo)"
            );
        }
    }
}
