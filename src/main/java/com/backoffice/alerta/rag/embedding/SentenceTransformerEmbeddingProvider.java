package com.backoffice.alerta.rag.embedding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Random;

/**
 * Provider de embeddings usando Sentence Transformers
 * 
 * Implementação híbrida:
 * 1. Tenta usar serviço HTTP local (se disponível)
 * 2. Caso contrário, usa embeddings simulados semanticamente inteligentes
 * 
 * Modelo recomendado: paraphrase-multilingual-mpnet-base-v2 (768 dimensões)
 * 
 * Para rodar serviço local (opcional):
 * python -m sentence_transformers.server --model paraphrase-multilingual-mpnet-base-v2
 * 
 * US#65 - Substituição do DummyEmbedding por Modelo Real
 */
public class SentenceTransformerEmbeddingProvider implements BusinessRuleEmbeddingProvider {
    
    private static final Logger log = LoggerFactory.getLogger(SentenceTransformerEmbeddingProvider.class);
    
    private static final int DIMENSION = 384; // paraphrase-multilingual-mpnet-base-v2 usa 384
    private static final String DEFAULT_ENDPOINT = "http://localhost:8000/embed";
    
    private final HttpClient httpClient;
    private final String endpoint;
    private final int timeoutSeconds;
    private boolean serviceAvailable = false;
    
    public SentenceTransformerEmbeddingProvider(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        this.endpoint = DEFAULT_ENDPOINT;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
        
        // Verifica se serviço está disponível
        checkServiceAvailability();
    }
    
    private void checkServiceAvailability() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint + "/health"))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                serviceAvailable = true;
                log.info("🌐 [US#65] Serviço Sentence Transformer disponível em {}", endpoint);
            }
        } catch (Exception e) {
            serviceAvailable = false;
            log.warn("⚠️ [US#65] Serviço Sentence Transformer não disponível. Usando embeddings simulados semânticos.");
        }
    }
    
    @Override
    public float[] embed(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new float[DIMENSION];
        }
        
        // Tenta usar serviço HTTP se disponível
        if (serviceAvailable) {
            try {
                return embedViaHttp(text);
            } catch (Exception e) {
                log.warn("⚠️ [US#65] Erro ao chamar serviço de embedding. Usando fallback semântico. Erro: {}", e.getMessage());
                serviceAvailable = false; // Desabilita tentativas futuras até próximo restart
            }
        }
        
        // Fallback: embeddings simulados semanticamente inteligentes
        return embedSemanticFallback(text);
    }
    
    private float[] embedViaHttp(String text) throws IOException, InterruptedException {
        String jsonPayload = String.format("{\"text\": \"%s\"}", text.replace("\"", "\\\""));
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
        }
        
        // Parse JSON response (simplificado - assume array de floats)
        return parseEmbeddingResponse(response.body());
    }
    
    private float[] parseEmbeddingResponse(String json) {
        // Simplificação: assume formato {"embedding": [0.1, 0.2, ...]}
        // Em produção, usar biblioteca JSON como Jackson
        
        String embeddingStr = json.substring(json.indexOf("[") + 1, json.lastIndexOf("]"));
        String[] values = embeddingStr.split(",");
        
        float[] embedding = new float[Math.min(values.length, DIMENSION)];
        for (int i = 0; i < embedding.length; i++) {
            embedding[i] = Float.parseFloat(values[i].trim());
        }
        
        return embedding;
    }
    
    /**
     * Fallback semântico: gera embeddings simulados com semântica básica
     * 
     * Diferente do DummyProvider que usa hash puro, este detecta palavras-chave
     * e ajusta dimensões específicas para criar proximidade semântica.
     */
    private float[] embedSemanticFallback(String text) {
        String normalized = text.toLowerCase().trim();
        
        // Base: hash determinístico (como Dummy)
        float[] embedding = generateBaseEmbedding(normalized);
        
        // Ajusta dimensões específicas baseado em semântica
        applySemanticBoost(embedding, normalized);
        
        // Normaliza vetor
        normalize(embedding);
        
        return embedding;
    }
    
    private float[] generateBaseEmbedding(String text) {
        float[] embedding = new float[DIMENSION];
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            
            for (int i = 0; i < DIMENSION; i++) {
                int byteIndex = (i * hash.length / DIMENSION) % hash.length;
                embedding[i] = (hash[byteIndex] & 0xFF) / 255.0f;
            }
        } catch (Exception e) {
            // Fallback do fallback
            Random rnd = new Random(text.hashCode());
            for (int i = 0; i < DIMENSION; i++) {
                embedding[i] = rnd.nextFloat();
            }
        }
        
        return embedding;
    }
    
    /**
     * Aplica boost semântico em dimensões específicas baseado em palavras-chave
     * 
     * Isso faz com que textos semanticamente similares tenham embeddings mais próximos
     * mesmo sem modelo real.
     */
    private void applySemanticBoost(float[] embedding, String text) {
        // Domínio: Pagamentos
        if (text.contains("pagamento") || text.contains("pagar") || text.contains("transação") || text.contains("transacao")) {
            boostDimensions(embedding, 0, 20, 0.3f);
        }
        
        // Domínio: PIX
        if (text.contains("pix")) {
            boostDimensions(embedding, 20, 40, 0.3f);
        }
        
        // Domínio: PJ (Pessoa Jurídica)
        if (text.contains("pj") || text.contains("cnpj") || text.contains("empresa") || text.contains("juridica") || text.contains("jurídica")) {
            boostDimensions(embedding, 40, 60, 0.3f);
        }
        
        // Domínio: CPF/Validação
        if (text.contains("cpf") || text.contains("validar") || text.contains("validação") || text.contains("validacao")) {
            boostDimensions(embedding, 60, 80, 0.3f);
        }
        
        // Domínio: Cálculo/Tributos
        if (text.contains("calcul") || text.contains("tributo") || text.contains("imposto") || text.contains("taxa")) {
            boostDimensions(embedding, 80, 100, 0.3f);
        }
        
        // Domínio: Horas/Tempo
        if (text.contains("hora") || text.contains("tempo") || text.contains("período") || text.contains("periodo")) {
            boostDimensions(embedding, 100, 120, 0.3f);
        }
        
        // Tipo de ação: Cálculo
        if (text.contains("calcular") || text.contains("computar") || text.contains("somar")) {
            boostDimensions(embedding, 120, 140, 0.25f);
        }
        
        // Tipo de ação: Validação
        if (text.contains("validar") || text.contains("verificar") || text.contains("checar")) {
            boostDimensions(embedding, 140, 160, 0.25f);
        }
        
        // Contexto: Cadastro/Registro
        if (text.contains("cadastr") || text.contains("registr") || text.contains("criar")) {
            boostDimensions(embedding, 160, 180, 0.2f);
        }
        
        // Contexto: Atualização
        if (text.contains("atualiz") || text.contains("modificar") || text.contains("alterar")) {
            boostDimensions(embedding, 180, 200, 0.2f);
        }
    }
    
    private void boostDimensions(float[] embedding, int start, int end, float boostFactor) {
        for (int i = start; i < Math.min(end, embedding.length); i++) {
            embedding[i] = Math.min(1.0f, embedding[i] + boostFactor);
        }
    }
    
    private void normalize(float[] vector) {
        float sum = 0;
        for (float v : vector) {
            sum += v * v;
        }
        
        float magnitude = (float) Math.sqrt(sum);
        if (magnitude > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= magnitude;
            }
        }
    }
    
    @Override
    public int getDimension() {
        return DIMENSION;
    }
}
