package com.backoffice.alerta.rag;

import com.backoffice.alerta.project.dto.ProjectContext;
import com.backoffice.alerta.project.domain.Project;
import com.backoffice.alerta.project.repository.ProjectRepository;
import com.backoffice.alerta.project.domain.ProjectBusinessRule;
import com.backoffice.alerta.project.repository.ProjectBusinessRuleRepository;
import com.backoffice.alerta.rag.cache.QueryEmbeddingCacheProvider;
import com.backoffice.alerta.rag.embedding.BusinessRuleEmbeddingProvider;
import com.backoffice.alerta.rag.persistence.JpaBusinessRuleVectorStore;
import com.backoffice.alerta.rag.vector.BusinessRuleVectorStore;
import com.backoffice.alerta.rules.BusinessRuleOwnership;
import com.backoffice.alerta.rules.BusinessRuleOwnershipRepository;
import com.backoffice.alerta.repository.BusinessRuleIncidentRepository;
import com.backoffice.alerta.rules.BusinessRule;
import com.backoffice.alerta.rules.BusinessRuleRepository;
import com.backoffice.alerta.rules.Criticality;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service RAG para consultas sobre regras de negócio
 * 
 * US#43: Busca keyword-based + fallback determinístico
 * US#44: Busca semântica com embeddings + hybrid search
 * US#66: Suporte a vector store persistente
 * 
 * Read-only, não modifica dados
 */
@Service
public class BusinessRuleRagService {
    
    private static final Logger log = LoggerFactory.getLogger(BusinessRuleRagService.class);
    
    private final BusinessRuleRepository ruleRepository;
    private final BusinessRuleOwnershipRepository ownershipRepository;
    private final BusinessRuleIncidentRepository incidentRepository;
    private final RagLLMClient llmClient;
    private final BusinessRuleEmbeddingProvider embeddingProvider;
    private final BusinessRuleVectorStore vectorStore;
    private final ProjectRepository projectRepository;
    private final ProjectBusinessRuleRepository projectBusinessRuleRepository;
    private final QueryEmbeddingCacheProvider queryEmbeddingCache;
    private final Gson gson;
    
    // US#66: VectorStore persistente (opcional, pode ser null se usando in-memory)
    @Autowired(required = false)
    private JpaBusinessRuleVectorStore jpaVectorStore;
    
    @Autowired(required = false)
    private com.backoffice.alerta.rag.ast.AstQueryService astQueryService;
    
    public BusinessRuleRagService(
            BusinessRuleRepository ruleRepository,
            BusinessRuleOwnershipRepository ownershipRepository,
            BusinessRuleIncidentRepository incidentRepository,
            RagLLMClient llmClient,
            BusinessRuleEmbeddingProvider embeddingProvider,
            BusinessRuleVectorStore vectorStore,
            ProjectRepository projectRepository,
            ProjectBusinessRuleRepository projectBusinessRuleRepository,
            QueryEmbeddingCacheProvider queryEmbeddingCache) {
        this.ruleRepository = ruleRepository;
        this.ownershipRepository = ownershipRepository;
        this.incidentRepository = incidentRepository;
        this.llmClient = llmClient;
        this.embeddingProvider = embeddingProvider;
        this.vectorStore = vectorStore;
        this.projectRepository = projectRepository;
        this.projectBusinessRuleRepository = projectBusinessRuleRepository;
        this.queryEmbeddingCache = queryEmbeddingCache;
        this.gson = new Gson();
    }
    
    public RagQueryResponse query(RagQueryRequest request) {
        log.info("🔍 RAG Query: '{}' (focus: {}, maxSources: {})", 
                 request.getQuestion(), request.getFocus(), request.getMaxSources());
        
        // US#63: Maps para rastrear scores durante a busca
        Map<String, Double> semanticScores = new HashMap<>();
        Map<String, Integer> keywordScores = new HashMap<>();
        
        // US#50: Escopo de projeto (opcional)
        Project project = null;
        Set<String> allowedRuleIds = null;
        
        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Projeto não encontrado: " + request.getProjectId()));
            
            log.info("🔎 Análise escopada para Projeto: {} ({})", 
                    project.getName(), request.getProjectId());
            
            List<ProjectBusinessRule> associations = projectBusinessRuleRepository
                .findByProjectId(request.getProjectId());
            
            // Converter IDs das regras para UUIDs (para match com busca semântica)
            allowedRuleIds = associations.stream()
                .map(ProjectBusinessRule::getBusinessRuleId)
                .map(ruleId -> UUID.nameUUIDFromBytes(ruleId.getBytes()).toString())
                .collect(Collectors.toSet());
            
            log.info("📋 Regras permitidas no projeto: {} regras", allowedRuleIds.size());
        } else {
            log.info("🌐 Análise GLOBAL (sem escopo de projeto)");
        }
        
        // 1. Retrieval: buscar dados reais do sistema
        List<BusinessRule> relevantRules = retrieveRelevantRules(request, allowedRuleIds, semanticScores, keywordScores);
        
        if (relevantRules.isEmpty()) {
            return buildNoDataResponse(request, project);
        }
        
        // 2. Ranking: ordenar por relevância
        List<BusinessRule> rankedRules = rankRules(relevantRules, request.getMaxSources());
        
        // 3. Construir contexto estruturado
        String context = buildStructuredContext(rankedRules, request);
        
        // 4. Gerar resposta com IA (ou fallback)
        RagQueryResponse response = generateResponse(request, context, rankedRules);
        
        // US#63: Popular scores detalhados
        populateRuleScores(response, rankedRules, semanticScores, keywordScores, response.isUsedFallback());
        
        // US#50: Adicionar contexto de projeto
        response.setProjectContext(project != null 
            ? ProjectContext.scoped(project.getId(), project.getName())
            : ProjectContext.global());
        
        log.info("✅ RAG Response: confidence={}, sources={}, usedFallback={}, scoped={}", 
                 response.getConfidence(), response.getSources().size(), 
                 response.isUsedFallback(), response.getProjectContext().isScoped());
        
        return response;
    }
    
    private List<BusinessRule> retrieveRelevantRules(RagQueryRequest request, Set<String> allowedRuleIds,
                                                     Map<String, Double> semanticScores, Map<String, Integer> keywordScores) {
        log.info("🔍 Iniciando hybrid search para pergunta: '{}'", request.getQuestion());
        
        // US#44: Busca semântica (com fallback automático)
        Set<String> semanticRuleIds = retrieveSemanticRules(request.getQuestion(), request.getMaxSources() * 2, semanticScores);
        log.info("📊 Busca semântica retornou: {} IDs -> {}", semanticRuleIds.size(), semanticRuleIds);
        
        // US#43: Busca keyword-based (mantida para robustez)
        Set<String> keywordRuleIds = retrieveKeywordRules(request.getQuestion(), keywordScores);
        log.info("📊 Busca keyword retornou: {} IDs -> {}", keywordRuleIds.size(), keywordRuleIds);
        
        // Merge: união dos resultados
        Set<String> mergedIds = new HashSet<>();
        mergedIds.addAll(semanticRuleIds);
        mergedIds.addAll(keywordRuleIds);
        
        // US#50: Filtrar por projeto se especificado
        if (allowedRuleIds != null) {
            mergedIds.retainAll(allowedRuleIds);
            log.info("📊 Após filtro de projeto: {} IDs", mergedIds.size());
        }
        
        log.info("📊 Hybrid search total: {} IDs únicos", mergedIds.size());
        
        // Buscar objetos completos
        List<BusinessRule> allRulesInRepo = ruleRepository.findAll();
        log.info("🔍 DEBUG: Total de regras no repositório ANTES do filtro: {}", allRulesInRepo.size());
        
        List<BusinessRule> allRules;
        if (allowedRuleIds != null) {
            // US#50: Buscar apenas regras do projeto
            allRules = allRulesInRepo.stream()
                .filter(rule -> allowedRuleIds.contains(UUID.nameUUIDFromBytes(rule.getId().getBytes()).toString()))
                .collect(Collectors.toList());
            log.info("📊 Total de regras no projeto: {}", allRules.size());
        } else {
            allRules = ruleRepository.findAll();
            log.info("📊 Total de regras no repositório: {}", allRules.size());
        }
        
        // PASSO 5: Fallback se busca retornar vazia
        if (mergedIds.isEmpty() && !allRules.isEmpty()) {
            log.warn("⚠️ Nenhum match - ativando fallback: retornar TOP 3 por criticidade");
            return allRules.stream()
                .sorted((r1, r2) -> Integer.compare(
                    getCriticalityScore(r2.getCriticality()),
                    getCriticalityScore(r1.getCriticality())
                ))
                .limit(3)
                .collect(Collectors.toList());
        }
        
        List<BusinessRule> matchedRules = allRules.stream()
            .filter(rule -> mergedIds.contains(UUID.nameUUIDFromBytes(rule.getId().getBytes()).toString()))
            .collect(Collectors.toList());
        
        log.info("✅ Regras encontradas após filtro: {}", matchedRules.size());
        
        return matchedRules;
    }
    
    private Set<String> retrieveSemanticRules(String question, int topK, Map<String, Double> semanticScores) {
        try {
            if (question == null || question.trim().isEmpty()) {
                log.warn("⚠️ Query vazia, pulando busca semântica");
                return Collections.emptySet();
            }
            
            // US#64: Normalizar query para chave de cache
            String normalizedQuery = normalizeQuery(question);
            
            // US#64: Tentar buscar embedding no cache
            float[] queryEmbedding = queryEmbeddingCache.get(normalizedQuery)
                    .orElseGet(() -> {
                        // Cache MISS: gerar embedding
                        log.info("🔍 Gerando embedding para query...");
                        float[] embedding = embeddingProvider.embed(question);
                        log.info("✅ Query embedding gerado: dimensão {}", embedding.length);
                        
                        // US#64: Salvar no cache
                        queryEmbeddingCache.put(normalizedQuery, embedding);
                        
                        return embedding;
                    });
            
            // US#66: Usar JpaVectorStore se disponível, senão in-memory
            List<UUID> topRuleIds;
            if (jpaVectorStore != null) {
                topRuleIds = jpaVectorStore.findTopK(queryEmbedding, topK);
            } else {
                topRuleIds = vectorStore.findTopK(queryEmbedding, topK);
            }
            
            // US#63: Capturar scores de similaridade
            for (UUID ruleId : topRuleIds) {
                try {
                    // Calcular cosine similarity real
                    float[] ruleEmbedding;
                    if (jpaVectorStore != null) {
                        ruleEmbedding = jpaVectorStore.getEmbedding(ruleId);
                    } else {
                        ruleEmbedding = vectorStore.getEmbedding(ruleId);
                    }
                    
                    if (ruleEmbedding != null) {
                        double similarity = cosineSimilarity(queryEmbedding, ruleEmbedding);
                        semanticScores.put(ruleId.toString(), similarity);
                    }
                } catch (Exception e) {
                    log.debug("⚠️ Não foi possível calcular similarity para {}", ruleId);
                }
            }
            
            Set<String> ruleIds = topRuleIds.stream()
                .map(UUID::toString)
                .collect(Collectors.toSet());
            
            log.info("✅ Busca semântica: {} regras encontradas", ruleIds.size());
            return ruleIds;
            
        } catch (Exception e) {
            log.warn("⚠️ Falha na busca semântica, usando apenas keywords: {}", e.getMessage());
            return Collections.emptySet();
        }
    }
    
    /**
     * US#63: Calcula cosine similarity entre dois vetores
     */
    private double cosineSimilarity(float[] vec1, float[] vec2) {
        if (vec1.length != vec2.length) {
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    private Set<String> retrieveKeywordRules(String question, Map<String, Integer> keywordScores) {
        String questionLower = question.toLowerCase();
        List<BusinessRule> allRules = ruleRepository.findAll();
        
        // US#63: Contar matches por regra
        Set<String> matchedIds = new HashSet<>();
        
        for (BusinessRule rule : allRules) {
            int matchCount = countKeywordMatches(rule, questionLower);
            if (matchCount > 0) {
                matchedIds.add(rule.getId());
                keywordScores.put(rule.getId(), matchCount);
            }
        }
        
        return matchedIds;
    }
    
    /**
     * US#63: Conta quantos termos da query deram match na regra
     */
    private int countKeywordMatches(BusinessRule rule, String questionLower) {
        if (rule == null || questionLower == null) {
            return 0;
        }
        
        String ruleName = normalize(rule.getName());
        String ruleDesc = normalize(rule.getDescription());
        String searchText = ruleName + " " + ruleDesc;
        String questionNorm = normalize(questionLower);
        
        int matchCount = 0;
        
        // Pagamento
        if (contains(questionNorm, "pagamento", "payment")) {
            if (contains(searchText, "pagamento", "payment", "pagar", "pago")) {
                matchCount++;
            }
        }
        
        // PIX
        if (contains(questionNorm, "pix")) {
            if (contains(searchText, "pix")) {
                matchCount++;
            }
        }
        
        // Pessoa Jurídica / Corporativo / CNPJ
        if (contains(questionNorm, "pj", "juridica", "corporativo", "empresa", "cnpj")) {
            if (contains(searchText, "pj", "juridica", "corporativo", "cnpj", "pessoa juridica")) {
                matchCount++;
            }
        }
        
        // CPF / Pessoa Física
        if (contains(questionNorm, "cpf", "fisica", "individual")) {
            if (contains(searchText, "cpf", "fisica", "pessoa fisica")) {
                matchCount++;
            }
        }
        
        // Validação / Documentos / Cadastro
        if (contains(questionNorm, "validar", "validacao", "documento", "cadastro", "registro")) {
            if (contains(searchText, "validacao", "validar", "cadastro", "registro", "documento")) {
                matchCount++;
            }
        }
        
        // Cálculo / Horas / Trabalho
        if (contains(questionNorm, "calculo", "hora", "trabalho")) {
            if (contains(searchText, "calculo", "hora", "trabalho")) {
                matchCount++;
            }
        }
        
        if (matchCount > 0) {
            log.info("✅ Match keyword [{} matches]: [{}] {}", matchCount, rule.getId(), rule.getName());
        }
        
        return matchCount;
    }
    
    private String normalize(String text) {
        if (text == null) return "";
        return text.toLowerCase()
            .replace("á", "a").replace("à", "a").replace("ã", "a")
            .replace("é", "e").replace("ê", "e")
            .replace("í", "i")
            .replace("ó", "o").replace("õ", "o").replace("ô", "o")
            .replace("ú", "u")
            .replace("ç", "c");
    }
    
    private boolean contains(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    private List<BusinessRule> rankRules(List<BusinessRule> rules, int maxSources) {
        // US#44: Re-ranking híbrido (semântica + criticidade + incidentes)
        log.debug("🎯 Re-ranking {} regras...", rules.size());
        
        // Calcula scores compostos
        List<RankedRule> rankedRules = rules.stream()
            .map(rule -> {
                double criticalityScore = getCriticalityScore(rule.getCriticality()) * 10.0; // 10-40
                long incidentCount = getIncidentCount(rule.getId());
                double incidentScore = Math.min(incidentCount * 5.0, 20.0); // Max 20
                
                // Score total: criticidade + incidentes (até 60 pontos)
                double totalScore = criticalityScore + incidentScore;
                
                return new RankedRule(rule, totalScore);
            })
            .sorted(Comparator.comparingDouble(RankedRule::score).reversed())
            .limit(maxSources)
            .collect(Collectors.toList());
        
        log.debug("✅ Top {} regras selecionadas", rankedRules.size());
        
        return rankedRules.stream()
            .map(RankedRule::rule)
            .collect(Collectors.toList());
    }
    
    /**
     * Record interno para ranking
     */
    private record RankedRule(BusinessRule rule, double score) {}
    
    private int getCriticalityScore(Criticality criticality) {
        return switch (criticality) {
            case CRITICA -> 4;
            case ALTA -> 3;
            case MEDIA -> 2;
            case BAIXA -> 1;
        };
    }
    
    private long getIncidentCount(String ruleId) {
        try {
            UUID uuid = UUID.fromString(ruleId);
            return incidentRepository.findByBusinessRuleIdOrderByOccurredAtDesc(uuid).size();
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }
    
    private String buildStructuredContext(List<BusinessRule> rules, RagQueryRequest request) {
        JsonObject context = new JsonObject();
        
        // Rules
        JsonArray rulesArray = new JsonArray();
        for (BusinessRule rule : rules) {
            JsonObject ruleObj = new JsonObject();
            ruleObj.addProperty("id", rule.getId());
            ruleObj.addProperty("name", rule.getName());
            ruleObj.addProperty("description", rule.getDescription());
            
            // Adicionar conte\u00fado completo se dispon\u00edvel
            if (rule.getContent() != null && !rule.getContent().isBlank()) {
                ruleObj.addProperty("content", rule.getContent());
            }
                        // Adicionar sourceFile se disponível
            if (rule.getSourceFile() != null && !rule.getSourceFile().isBlank()) {
                ruleObj.addProperty("sourceFile", rule.getSourceFile());
            }
                        ruleObj.addProperty("domain", rule.getDomain().name());
            ruleObj.addProperty("criticality", rule.getCriticality().name());
            rulesArray.add(ruleObj);
        }
        context.add("rules", rulesArray);
        
        // Incidents
        JsonArray incidentsArray = new JsonArray();
        for (BusinessRule rule : rules) {
            try {
                UUID ruleUuid = UUID.fromString(rule.getId());
                var incidents = incidentRepository.findByBusinessRuleIdOrderByOccurredAtDesc(ruleUuid);
                for (var incident : incidents) {
                    JsonObject incidentObj = new JsonObject();
                    incidentObj.addProperty("ruleId", rule.getId());
                    incidentObj.addProperty("severity", incident.getSeverity().name());
                    incidentObj.addProperty("description", incident.getDescription());
                    incidentsArray.add(incidentObj);
                }
            } catch (IllegalArgumentException e) {
                // ID inválido, ignorar
            }
        }
        context.add("incidents", incidentsArray);
        
        // Ownerships
        JsonArray ownershipsArray = new JsonArray();
        for (BusinessRule rule : rules) {
            try {
                UUID ruleUuid = UUID.fromString(rule.getId());
                List<BusinessRuleOwnership> ownerships = ownershipRepository.findByBusinessRuleId(ruleUuid);
                if (!ownerships.isEmpty()) {
                    BusinessRuleOwnership ownership = ownerships.get(0);
                    JsonObject ownershipObj = new JsonObject();
                    ownershipObj.addProperty("ruleId", rule.getId());
                    ownershipObj.addProperty("teamName", ownership.getTeamName());
                    ownershipObj.addProperty("contactEmail", ownership.getContactEmail());
                    ownershipsArray.add(ownershipObj);
                }
            } catch (IllegalArgumentException e) {
                // ID inválido, ignorar
            }
        }
        context.add("ownerships", ownershipsArray);
        
        // AST Implementations (Phase 2 enrichment)
        JsonArray astImplArray = new JsonArray();
        if (astQueryService != null) {
            for (BusinessRule rule : rules) {
                try {
                    UUID projectId = request.getProjectId();
                    List<Map<String, Object>> impls = astQueryService.findImplementations(rule.getId(), projectId != null ? projectId : null);
                    for (Map<String, Object> impl : impls) {
                        JsonObject implObj = new JsonObject();
                        implObj.addProperty("ruleId", rule.getId());
                        if (impl.get("file") != null) implObj.addProperty("file", impl.get("file").toString());
                        if (impl.get("node_type") != null) implObj.addProperty("nodeType", impl.get("node_type").toString());
                        if (impl.get("name") != null) implObj.addProperty("name", impl.get("name").toString());
                        if (impl.get("line_start") != null) implObj.addProperty("lineStart", Double.valueOf(impl.get("line_start").toString()).intValue());
                        if (impl.get("line_end") != null) implObj.addProperty("lineEnd", Double.valueOf(impl.get("line_end").toString()).intValue());
                        if (impl.get("confidence") != null) implObj.addProperty("confidence", Double.valueOf(impl.get("confidence").toString()));
                        if (impl.get("evidence") != null) implObj.addProperty("evidence", impl.get("evidence").toString());
                        astImplArray.add(implObj);
                    }
                } catch (Exception e) {
                    log.debug("[AST] Error enriching rule {}: {}", rule.getId(), e.getMessage());
                }
            }
        }
        context.add("astImplementations", astImplArray);
        
        return gson.toJson(context);
    }
    
    private RagQueryResponse generateResponse(RagQueryRequest request, String context, List<BusinessRule> rules) {
        RagQueryResponse response = new RagQueryResponse();
        
        try {
            // Tentar usar IA
            RagLLMClient.RagAnswer aiAnswer = llmClient.generateAnswer(
                request.getQuestion(),
                context,
                request.getFocus()
            );
            
            if (aiAnswer.isSuccess() && aiAnswer.getAnswer() != null && !aiAnswer.getAnswer().isBlank()) {
                response.setAnswer(aiAnswer.getAnswer());
                response.setConfidence(aiAnswer.getConfidence());
                response.setUsedFallback(false);
            } else {
                // Fallback
                generateFallbackResponse(response, request, rules);
            }
            
        } catch (Exception e) {
            log.warn("⚠️ IA falhou, usando fallback determinístico: {}", e.getMessage());
            generateFallbackResponse(response, request, rules);
        }
        
        // Adicionar fontes
        populateSources(response, rules);
        populateOwnerships(response, rules);
        populateImpacts(response, rules);
        
        return response;
    }
    
    private void generateFallbackResponse(RagQueryResponse response, RagQueryRequest request, List<BusinessRule> rules) {
        StringBuilder answer = new StringBuilder();
        
        answer.append(String.format("📊 Encontrei %d regra(s) relevante(s) no sistema:\n\n", rules.size()));
        
        for (int i = 0; i < Math.min(3, rules.size()); i++) {
            BusinessRule rule = rules.get(i);
            answer.append(String.format("• %s (%s, %s)\n", 
                rule.getName(), 
                rule.getDomain().name(), 
                rule.getCriticality().name()
            ));
            answer.append(String.format("  %s\n\n", rule.getDescription()));
        }
        
        if (rules.size() > 3) {
            answer.append(String.format("... e mais %d regra(s). Veja as fontes abaixo.\n\n", rules.size() - 3));
        }
        
        answer.append("⚠️ Resposta gerada de forma determinística (IA indisponível).");
        
        response.setAnswer(answer.toString());
        response.setConfidence(rules.size() >= 3 ? ConfidenceLevel.MEDIUM : ConfidenceLevel.LOW);
        response.setUsedFallback(true);
    }
    
    private void populateSources(RagQueryResponse response, List<BusinessRule> rules) {
        List<RagSourceReference> sources = new ArrayList<>();
        
        for (BusinessRule rule : rules) {
            RagSourceReference source = new RagSourceReference();
            source.setType("BUSINESS_RULE");
            source.setId(rule.getId());
            source.setTitle(rule.getName());
            source.setDomain(rule.getDomain());
            source.setCriticality(rule.getCriticality());
            source.setSummary(rule.getDescription());
            sources.add(source);
        }
        
        response.setSources(sources);
    }
    
    private void populateOwnerships(RagQueryResponse response, List<BusinessRule> rules) {
        List<OwnershipSummary> ownerships = new ArrayList<>();
        
        for (BusinessRule rule : rules) {
            try {
                UUID ruleUuid = UUID.fromString(rule.getId());
                List<BusinessRuleOwnership> ruleOwnerships = ownershipRepository.findByBusinessRuleId(ruleUuid);
                if (!ruleOwnerships.isEmpty()) {
                    BusinessRuleOwnership own = ruleOwnerships.get(0);
                    OwnershipSummary summary = new OwnershipSummary();
                    summary.setRuleId(rule.getId());
                    summary.setRuleName(rule.getName());
                    summary.setOwner(own.getTeamName());
                    summary.setTeam(own.getTeamType().name());
                    summary.setContact(own.getContactEmail());
                    ownerships.add(summary);
                }
            } catch (IllegalArgumentException e) {
                // ID inválido, ignorar
            }
        }
        
        response.setOwnerships(ownerships);
    }
    
    private void populateImpacts(RagQueryResponse response, List<BusinessRule> rules) {
        List<String> impacts = new ArrayList<>();
        
        for (BusinessRule rule : rules) {
            long incidentCount = getIncidentCount(rule.getId());
            if (incidentCount > 0) {
                impacts.add(String.format("Regra '%s' tem %d incidente(s) registrado(s)", 
                    rule.getName(), incidentCount));
            }
        }
        
        if (impacts.isEmpty()) {
            impacts.add("Nenhum incidente registrado para as regras encontradas");
        }
        
        response.setRelatedImpacts(impacts);
    }
    
    /**
     * US#63: Popular scores detalhados de cada regra retornada
     */
    private void populateRuleScores(RagQueryResponse response, List<BusinessRule> rankedRules,
                                    Map<String, Double> semanticScores, Map<String, Integer> keywordScores,
                                    boolean usedFallback) {
        List<RagRuleScoreDetail> ruleScores = new ArrayList<>();
        
        for (int i = 0; i < rankedRules.size(); i++) {
            BusinessRule rule = rankedRules.get(i);
            String ruleId = rule.getId();
            
            double semanticScore = semanticScores.getOrDefault(ruleId, 0.0);
            int keywordScore = keywordScores.getOrDefault(ruleId, 0);
            
            // Determinar tipo de match
            RagMatchType matchType;
            boolean includedByFallback = usedFallback && semanticScore == 0.0 && keywordScore == 0;
            
            if (includedByFallback) {
                matchType = RagMatchType.FALLBACK;
            } else if (semanticScore > 0.0 && keywordScore > 0) {
                matchType = RagMatchType.HYBRID;
            } else if (semanticScore > 0.0) {
                matchType = RagMatchType.SEMANTIC;
            } else if (keywordScore > 0) {
                matchType = RagMatchType.KEYWORD;
            } else {
                // Caso não tenha score mas foi incluída (possível fallback)
                matchType = RagMatchType.FALLBACK;
                includedByFallback = true;
            }
            
            RagRuleScoreDetail scoreDetail = new RagRuleScoreDetail(
                ruleId,
                rule.getName(),
                matchType,
                semanticScore,
                keywordScore,
                i + 1, // finalRankPosition (1-based)
                includedByFallback
            );
            
            ruleScores.add(scoreDetail);
            
            // US#63: Log obrigatório
            log.info("📊 RAG Score | Rule={} | semantic={} | keyword={} | type={} | rank={}", 
                     rule.getName(), 
                     String.format("%.2f", semanticScore),
                     keywordScore,
                     matchType,
                     i + 1);
        }
        
        response.setRuleScores(ruleScores);
        
        // Log de fallback se aplicável
        long fallbackCount = ruleScores.stream().filter(RagRuleScoreDetail::isIncludedByFallback).count();
        if (fallbackCount > 0) {
            log.info("⚠️ RAG Fallback aplicado para {} regras", fallbackCount);
        }
    }
    
    private RagQueryResponse buildNoDataResponse(RagQueryRequest request, Project project) {
        RagQueryResponse response = new RagQueryResponse();
        response.setAnswer("❌ Não encontrei dados relevantes no sistema para responder essa pergunta. " +
                          "Tente reformular ou use termos como: pagamento, PIX, CPF, CNPJ, PJ.");
        response.setConfidence(ConfidenceLevel.LOW);
        response.setUsedFallback(true);
        response.setRelatedImpacts(List.of("Nenhum dado encontrado"));
        
        // US#50: Adicionar contexto de projeto
        response.setProjectContext(project != null 
            ? ProjectContext.scoped(project.getId(), project.getName())
            : ProjectContext.global());
        
        return response;
    }
    
    /**
     * US#64 - Normaliza a query para usar como chave de cache.
     * 
     * Normalização:
     * - lowercase
     * - trim
     * - remover acentos
     * - múltiplos espaços → espaço único
     * 
     * Garante que queries semanticamente iguais tenham a mesma chave.
     * 
     * @param query Query original
     * @return Query normalizada
     */
    private String normalizeQuery(String query) {
        if (query == null) {
            return "";
        }
        
        // 1. Remover acentos
        String normalized = Normalizer.normalize(query, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");
        
        // 2. Lowercase
        normalized = normalized.toLowerCase();
        
        // 3. Múltiplos espaços → espaço único
        normalized = normalized.replaceAll("\\s+", " ");
        
        // 4. Trim
        normalized = normalized.trim();
        
        return normalized;
    }
}
