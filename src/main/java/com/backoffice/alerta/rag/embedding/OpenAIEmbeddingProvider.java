package com.backoffice.alerta.rag.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Provider de embeddings usando OpenAI text-embedding-3-small
 * 
 * Requer:
 * - OPENAI_API_KEY configurada em variável de ambiente
 * - Conexão com internet
 * 
 * Dimensões: 1536 (padrão do modelo)
 * 
 * Caso de erro:
 * - Token ausente
 * - Timeout
 * - Quota excedida
 * -> EmbeddingProviderFactory fará fallback para DUMMY
 * 
 * US#65 - Substituição do DummyEmbedding por Modelo Real
 */
public class OpenAIEmbeddingProvider implements BusinessRuleEmbeddingProvider {
    
    private static final Logger log = LoggerFactory.getLogger(OpenAIEmbeddingProvider.class);
    
    private static final int DIMENSION = 1536; // text-embedding-3-small
    private static final String MODEL = "text-embedding-3-small";
    
    private final HttpClient httpClient;
    private final String apiKey;
    private final String apiUrl;
    private final ObjectMapper objectMapper;
    
    public OpenAIEmbeddingProvider(String apiKey, String apiUrl, int timeoutSeconds) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
        
        log.info("🌐 [US#65] OpenAI Embedding Provider inicializado | model={} | url={}", MODEL, apiUrl);
    }
    
    @Override
    public float[] embed(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new float[DIMENSION];
        }
        
        try {
            return callOpenAI(text);
        } catch (Exception e) {
            log.error("❌ [US#65] Erro ao gerar embedding OpenAI: {}", e.getMessage());
            throw new RuntimeException("Failed to generate OpenAI embedding", e);
        }
    }
    
    private float[] callOpenAI(String text) throws IOException, InterruptedException {
        // Payload JSON
        String jsonPayload = String.format(
            "{\"input\": \"%s\", \"model\": \"%s\"}",
            text.replace("\"", "\\\"").replace("\n", "\\n"),
            MODEL
        );
        
        // Request HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + "/embeddings"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
        
        // Executa request
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("OpenAI API returned " + response.statusCode() + ": " + response.body());
        }
        
        // Parse resposta
        return parseOpenAIResponse(response.body());
    }
    
    private float[] parseOpenAIResponse(String json) throws IOException {
        JsonNode root = objectMapper.readTree(json);
        JsonNode embeddingNode = root.path("data").get(0).path("embedding");
        
        if (embeddingNode == null || !embeddingNode.isArray()) {
            throw new IOException("Invalid OpenAI response format");
        }
        
        float[] embedding = new float[DIMENSION];
        for (int i = 0; i < DIMENSION && i < embeddingNode.size(); i++) {
            embedding[i] = (float) embeddingNode.get(i).asDouble();
        }
        
        return embedding;
    }
    
    @Override
    public int getDimension() {
        return DIMENSION;
    }
}
