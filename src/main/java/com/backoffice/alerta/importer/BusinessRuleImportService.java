package com.backoffice.alerta.importer;

import com.backoffice.alerta.dto.BusinessRuleImportRequest;
import com.backoffice.alerta.dto.BusinessRuleImportResponse;
import com.backoffice.alerta.dto.ExtractedBusinessRule;
import com.backoffice.alerta.dto.ImportedRuleSummary;
import com.backoffice.alerta.importer.extractors.CodeCommentRuleExtractor;
import com.backoffice.alerta.importer.extractors.MarkdownRuleExtractor;
import com.backoffice.alerta.importer.extractors.YamlRuleExtractor;
import com.backoffice.alerta.project.domain.Project;
import com.backoffice.alerta.project.dto.ProjectContext;
import com.backoffice.alerta.project.repository.ProjectRepository;
import com.backoffice.alerta.project.domain.ProjectBusinessRule;
import com.backoffice.alerta.project.repository.ProjectBusinessRuleRepository;
import com.backoffice.alerta.rag.embedding.BusinessRuleEmbeddingProvider;
import com.backoffice.alerta.rag.persistence.JpaBusinessRuleVectorStore;
import com.backoffice.alerta.rules.BusinessRule;
import com.backoffice.alerta.rules.BusinessRuleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * US#68 - Serviço de importação automática de regras de negócio do Git
 * 
 * READ-ONLY absoluto:
 * - Nunca escreve no Git
 * - Nunca executa código
 * - Nunca cria commits/PRs
 * - Apenas lê conteúdo de arquivos
 */
@Service
public class BusinessRuleImportService {

    private static final Logger log = LoggerFactory.getLogger(BusinessRuleImportService.class);

    private final ProjectRepository projectRepository;
    private final BusinessRuleRepository businessRuleRepository;
    private final ProjectBusinessRuleRepository projectBusinessRuleRepository;
    private final BusinessRuleEmbeddingProvider embeddingProvider;
    private final CodeCommentRuleExtractor codeCommentExtractor;
    private final MarkdownRuleExtractor markdownExtractor;
    private final YamlRuleExtractor yamlExtractor;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private JpaBusinessRuleVectorStore jpaVectorStore;

    @Autowired(required = false)
    private com.backoffice.alerta.service.CodeIndexingService codeIndexingService;

    @Autowired(required = false)
    private com.backoffice.alerta.rag.ast.AstIndexingService astIndexingService;

    @Value("${git.providers.github.token:}")
    private String githubToken;

    @Value("${git.providers.gitlab.token:}")
    private String gitlabToken;

    @Value("${rag.code.index-on-onboarding:false}")
    private boolean indexCodeOnOnboarding;

    public BusinessRuleImportService(
            ProjectRepository projectRepository,
            BusinessRuleRepository businessRuleRepository,
            ProjectBusinessRuleRepository projectBusinessRuleRepository,
            BusinessRuleEmbeddingProvider embeddingProvider,
            CodeCommentRuleExtractor codeCommentExtractor,
            MarkdownRuleExtractor markdownExtractor,
            YamlRuleExtractor yamlExtractor) {
        this.projectRepository = projectRepository;
        this.businessRuleRepository = businessRuleRepository;
        this.projectBusinessRuleRepository = projectBusinessRuleRepository;
        this.embeddingProvider = embeddingProvider;
        this.codeCommentExtractor = codeCommentExtractor;
        this.markdownExtractor = markdownExtractor;
        this.yamlExtractor = yamlExtractor;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Importa regras de negócio de um repositório Git
     * 
     * @param request Requisição com dados do repositório
     * @return Response com estatísticas da importação
     */
    public BusinessRuleImportResponse importRules(BusinessRuleImportRequest request) {
        try {
            log.info("📥 [US#68] Importação iniciada | repo={} | branch={} | dryRun={}", 
                    request.getRepositoryUrl(), request.getBranch(), request.isDryRun());

            // 1. Validar projeto
            Optional<Project> projectOpt = projectRepository.findById(request.getProjectId());
            if (projectOpt.isEmpty()) {
                log.error("❌ [US#68] Projeto não encontrado | id={}", request.getProjectId());
                return createEmptyResponse();
            }

            Project project = projectOpt.get();
            ProjectContext projectContext = new ProjectContext(project.getId(), project.getName());

            if (request.isDryRun()) {
                log.info("🧪 [US#68] Dry-run ativo | nenhuma persistência realizada");
            }

            // 2. Listar arquivos do repositório
            List<RepositoryFile> files = listRepositoryFiles(request);
            log.info("📂 [US#68] {} arquivo(s) encontrado(s)", files.size());

            // 3. Extrair regras de cada arquivo
            List<ExtractedBusinessRule> extractedRules = new ArrayList<>();
            for (RepositoryFile file : files) {
                try {
                    log.debug("🔍 [US#68] Analisando arquivo | path={}", file.getPath());
                    
                    Optional<ExtractedBusinessRule> ruleOpt = extractRuleFromFile(file);
                    if (ruleOpt.isPresent()) {
                        extractedRules.add(ruleOpt.get());
                        log.info("✅ [US#68] Regra detectada | id={} | file={}", 
                                ruleOpt.get().getRuleId(), file.getPath());
                    }
                } catch (Exception e) {
                    log.warn("⚠️ [US#68] Erro ao processar arquivo {} | error={}", file.getPath(), e.getMessage());
                }
            }

            log.info("📊 [US#68] {} regra(s) detectada(s)", extractedRules.size());

            // 4. Processar regras (criar/atualizar)
            int created = 0;
            int updated = 0;
            int skipped = 0;
            List<ImportedRuleSummary> summaries = new ArrayList<>();

            for (ExtractedBusinessRule extracted : extractedRules) {
                try {
                    String action = processRule(extracted, project, request.isDryRun());
                    
                    summaries.add(new ImportedRuleSummary(
                        extracted.getRuleId(),
                        extracted.getName(),
                        extracted.getSourceFile(),
                        action
                    ));

                    switch (action) {
                        case "CREATED" -> created++;
                        case "UPDATED" -> updated++;
                        case "SKIPPED" -> skipped++;
                    }

                } catch (Exception e) {
                    log.error("❌ [US#68] Erro ao processar regra {} | error={}", 
                            extracted.getRuleId(), e.getMessage());
                    skipped++;
                    summaries.add(new ImportedRuleSummary(
                        extracted.getRuleId(),
                        extracted.getName(),
                        extracted.getSourceFile(),
                        "SKIPPED"
                    ));
                }
            }

            log.info("📊 [US#68] Importação concluída | detectadas={} | criadas={} | atualizadas={} | ignoradas={}", 
                    extractedRules.size(), created, updated, skipped);

            return new BusinessRuleImportResponse(
                extractedRules.size(),
                created,
                updated,
                skipped,
                summaries,
                projectContext,
                false
            );

        } catch (Exception e) {
            log.error("❌ [US#68] Erro na importação | error={}", e.getMessage(), e);
            return createEmptyResponse();
        }
    }

    /**
     * Lista arquivos do repositório Git
     */
    private List<RepositoryFile> listRepositoryFiles(BusinessRuleImportRequest request) throws Exception {
        if (request.getProvider() == BusinessRuleImportRequest.GitProvider.GITHUB) {
            return listGitHubFiles(request);
        } else {
            return listGitLabFiles(request);
        }
    }

    /**
     * Lista arquivos do GitHub via API
     */
    private List<RepositoryFile> listGitHubFiles(BusinessRuleImportRequest request) throws Exception {
        log.info("🔗 [US#68] Conectando ao GitHub | repo={}", request.getRepositoryUrl());

        if (githubToken == null || githubToken.trim().isEmpty()) {
            log.warn("⚠️ [US#68] GitHub token não configurado");
            throw new IllegalStateException("GitHub token não configurado");
        }

        // Extrair owner/repo da URL
        String[] parts = extractOwnerAndRepo(request.getRepositoryUrl());
        String owner = parts[0];
        String repo = parts[1];

        // Listar árvore de arquivos recursivamente
        String url = String.format("https://api.github.com/repos/%s/%s/git/trees/%s?recursive=1", 
                                  owner, repo, request.getBranch());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        headers.set("Accept", "application/vnd.github+json");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        JsonNode tree = objectMapper.readTree(response.getBody()).path("tree");
        
        List<RepositoryFile> files = new ArrayList<>();
        if (tree.isArray()) {
            for (JsonNode node : tree) {
                String path = node.path("path").asText();
                String type = node.path("type").asText();
                
                // Apenas arquivos (não diretórios) com extensões suportadas
                if ("blob".equals(type) && isSupportedFile(path)) {
                    String content = fetchGitHubFileContent(owner, repo, path, request.getBranch());
                    files.add(new RepositoryFile(path, content));
                }
            }
        }

        return files;
    }

    /**
     * Busca conteúdo de um arquivo do GitHub
     */
    private String fetchGitHubFileContent(String owner, String repo, String path, String branch) throws Exception {
        String url = String.format("https://api.github.com/repos/%s/%s/contents/%s?ref=%s", 
                                  owner, repo, path, branch);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        headers.set("Accept", "application/vnd.github+json");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        JsonNode fileData = objectMapper.readTree(response.getBody());
        String encodedContent = fileData.path("content").asText();
        
        // Decodificar Base64
        byte[] decoded = Base64.getDecoder().decode(encodedContent.replaceAll("\\s", ""));
        return new String(decoded, StandardCharsets.UTF_8);
    }

    /**
     * Lista arquivos do GitLab via API (implementação simplificada)
     */
    private List<RepositoryFile> listGitLabFiles(BusinessRuleImportRequest request) throws Exception {
        log.info("🔗 [US#68] Conectando ao GitLab | repo={}", request.getRepositoryUrl());
        
        // Implementação simplificada para GitLab
        // TODO: Implementar parsing completo da API GitLab se necessário
        
        return new ArrayList<>();
    }

    /**
     * Extrai owner/repo de URL GitHub
     */
    private String[] extractOwnerAndRepo(String url) {
        Pattern pattern = Pattern.compile("github\\.com[/:]([^/]+)/([^/\\.]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return new String[]{matcher.group(1), matcher.group(2)};
        }
        throw new IllegalArgumentException("URL inválida: " + url);
    }

    /**
     * Verifica se arquivo tem extensão suportada
     */
    private boolean isSupportedFile(String path) {
        String lower = path.toLowerCase();
        return lower.endsWith(".java") || 
               lower.endsWith(".md") || 
               lower.endsWith(".yaml") || 
               lower.endsWith(".yml");
    }

    /**
     * Extrai regra de arquivo usando extractors apropriados
     */
    private Optional<ExtractedBusinessRule> extractRuleFromFile(RepositoryFile file) {
        String path = file.getPath().toLowerCase();
        
        if (path.endsWith(".java")) {
            return codeCommentExtractor.extract(file.getContent(), file.getPath());
        } else if (path.endsWith(".md")) {
            return markdownExtractor.extract(file.getContent(), file.getPath());
        } else if (path.endsWith(".yaml") || path.endsWith(".yml")) {
            return yamlExtractor.extract(file.getContent(), file.getPath());
        }
        
        return Optional.empty();
    }

    /**
     * Processa regra: cria, atualiza ou ignora
     * 
     * @return "CREATED" | "UPDATED" | "SKIPPED"
     */
    private String processRule(ExtractedBusinessRule extracted, Project project, boolean dryRun) {
        // Verificar se regra já existe
        Optional<BusinessRule> existingOpt = businessRuleRepository.findById(extracted.getRuleId());
        
        if (existingOpt.isPresent()) {
            // Regra existe - UPDATE
            if (!dryRun) {
                BusinessRule existing = existingOpt.get();
                existing.setName(extracted.getName());
                existing.setDescription(extracted.getDescription());
                existing.setContent(extracted.getContent()); // Atualizar conteúdo completo
                existing.setSourceFile(extracted.getSourceFile()); // Atualizar caminho do arquivo
                
                // Atualizar criticality apenas se fornecida
                if (extracted.getCriticality() != null) {
                    existing.setCriticality(extracted.getCriticality());
                }
                
                // Atualizar domain apenas se fornecido
                if (extracted.getDomain() != null) {
                    existing.setDomain(extracted.getDomain());
                }
                
                businessRuleRepository.save(existing);
                
                // Garantir associação ao projeto existe (verificar antes de tentar inserir)
                boolean associationExists = projectBusinessRuleRepository
                    .existsByProjectIdAndBusinessRuleId(project.getId(), extracted.getRuleId());
                
                if (!associationExists) {
                    try {
                        ProjectBusinessRule association = new ProjectBusinessRule(
                            project.getId(),
                            extracted.getRuleId(),
                            "SYSTEM_IMPORT_US68"
                        );
                        projectBusinessRuleRepository.saveAndFlush(association);
                        log.debug("🔗 [US#68] Associação criada | project={} | rule={}", project.getId(), extracted.getRuleId());
                    } catch (Exception e) {
                        // Em caso de race condition, ignorar - associação pode ter sido criada por outra thread
                        log.trace("🔗 [US#68] Erro ao criar associação (race condition?) | project={} | rule={} | error={}", 
                                project.getId(), extracted.getRuleId(), e.getMessage());
                    }
                } else {
                    log.trace("🔗 [US#68] Associação já existe | project={} | rule={}", project.getId(), extracted.getRuleId());
                }
                
                // Reindexar embedding
                indexRuleEmbedding(existing, project.getId());
                
                log.info("♻️ [US#68] Regra atualizada | id={}", extracted.getRuleId());
            } else {
                log.info("🧪 [US#68] [DRY-RUN] Regra seria atualizada | id={}", extracted.getRuleId());
            }
            return "UPDATED";
            
        } else {
            // Regra não existe - CREATE
            if (!dryRun) {
                BusinessRule newRule = new BusinessRule(
                    extracted.getRuleId(),
                    extracted.getName(),
                    extracted.getDomain(),
                    extracted.getDescription(),
                    extracted.getCriticality(),
                    extracted.getOwner()
                );
                newRule.setContent(extracted.getContent()); // Salvar conteúdo completo
                newRule.setSourceFile(extracted.getSourceFile()); // Salvar caminho do arquivo
                businessRuleRepository.save(newRule);
                
                // Associar ao projeto
                ProjectBusinessRule association = new ProjectBusinessRule(
                    project.getId(),
                    extracted.getRuleId(),
                    "SYSTEM_IMPORT_US68"
                );
                projectBusinessRuleRepository.save(association);
                
                // Indexar embedding
                indexRuleEmbedding(newRule, project.getId());
                
                log.info("✅ [US#68] Regra criada | id={}", extracted.getRuleId());
            } else {
                log.info("🧪 [US#68] [DRY-RUN] Regra seria criada | id={}", extracted.getRuleId());
            }
            return "CREATED";
        }
    }

    /**
     * Indexa embedding da regra no Vector Store
     */
    private void indexRuleEmbedding(BusinessRule rule, UUID projectId) {
        try {
            String text = buildTextForEmbedding(rule);
            float[] embedding = embeddingProvider.embed(text);
            
            if (jpaVectorStore != null) {
                // Converte String ID para UUID deterministicamente via hash (US#68)
                UUID ruleUuid = UUID.nameUUIDFromBytes(rule.getId().getBytes());
                jpaVectorStore.save(ruleUuid, embedding);
                log.debug("📊 [US#68] Embedding indexado | rule={}", rule.getId());
            }

            // Indexar código se habilitado (FASE 1)
            if (indexCodeOnOnboarding && codeIndexingService != null) {
                codeIndexingService.indexBusinessRuleCode(rule, projectId);
            }

            // Indexar AST se habilitado (FASE 2 - opcional)
            if (indexCodeOnOnboarding && astIndexingService != null) {
                try {
                    astIndexingService.indexBusinessRuleAst(rule, projectId);
                } catch (Exception e) {
                    log.warn("⚠️ [US#68] Erro ao indexar AST | rule={} | error={}", rule.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("⚠️ [US#68] Erro ao indexar embedding | rule={} | error={}", 
                    rule.getId(), e.getMessage());
        }
    }

    /**
     * Constrói texto para embedding
     */
    private String buildTextForEmbedding(BusinessRule rule) {
        return String.format("%s %s %s %s %s",
            rule.getId(),
            rule.getName(),
            rule.getDescription(),
            rule.getDomain(),
            rule.getCriticality()
        );
    }

    /**
     * Cria response vazio em caso de erro
     */
    private BusinessRuleImportResponse createEmptyResponse() {
        return new BusinessRuleImportResponse(0, 0, 0, 0, new ArrayList<>(), null, false);
    }

    /**
     * Classe interna para representar arquivo do repositório
     */
    private static class RepositoryFile {
        private final String path;
        private final String content;

        public RepositoryFile(String path, String content) {
            this.path = path;
            this.content = content;
        }

        public String getPath() {
            return path;
        }

        public String getContent() {
            return content;
        }
    }
}
