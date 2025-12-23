package com.backoffice.alerta.rag;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Cliente LLM REAL usando OpenAI Chat Completions API
 * 
 * GOVERNANÇA CRÍTICA:
 * - LLM APENAS explica, NUNCA decide
 * - LLM NUNCA altera scores, bloqueia PRs ou cria regras
 * - LLM é consultivo (read-only)
 * - Fail-safe automático para Dummy em caso de erro
 * 
 * Configuração:
 * rag.llm.provider=OPENAI
 * rag.llm.openai.api-key=${OPENAI_API_KEY}
 * rag.llm.openai.model=gpt-4o-mini
 */
@Component
@ConditionalOnProperty(name = "rag.llm.provider", havingValue = "OPENAI")
public class OpenAIRagLLMClient implements RagLLMClient {
    
    private static final Logger log = LoggerFactory.getLogger(OpenAIRagLLMClient.class);
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    
    private final String apiKey;
    private final String model;
    private final int timeoutSeconds;
    private final HttpClient httpClient;
    private final Gson gson;
    
    public OpenAIRagLLMClient(
            @Value("${rag.llm.openai.api-key}") String apiKey,
            @Value("${rag.llm.openai.model:gpt-4o-mini}") String model,
            @Value("${rag.llm.openai.timeout-seconds:15}") int timeoutSeconds) {
        
        this.apiKey = apiKey;
        this.model = model;
        this.timeoutSeconds = timeoutSeconds;
        this.gson = new Gson();
        
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
        
        log.info("🤖 [LLM] OpenAI Client inicializado | model={} | timeout={}s", model, timeoutSeconds);
    }
    
    @Override
    public RagAnswer generateAnswer(String question, String context, ExplainFocus focus) {
        try {
            log.info("🤖 [LLM] Calling OpenAI model={}", model);
            
            String prompt = buildPrompt(question, context, focus);
            String requestBody = buildRequestBody(prompt);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENAI_API_URL))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                log.warn("⚠️ [LLM] OpenAI API returned {}: {}", response.statusCode(), response.body());
                return fallbackWithReason("HTTP_" + response.statusCode(), question, context, focus);
            }
            
            String answer = parseOpenAIResponse(response.body());
            int tokens = extractTokenCount(response.body());
            
            log.info("🤖 [LLM] Response received (tokens={})", tokens);
            
            return new RagAnswer(answer, ConfidenceLevel.MEDIUM, true);
            
        } catch (java.net.http.HttpTimeoutException e) {
            log.warn("⚠️ [LLM] Fallback to Dummy client (reason=TIMEOUT)");
            return fallbackWithReason("TIMEOUT", question, context, focus);
            
        } catch (Exception e) {
            log.warn("⚠️ [LLM] Fallback to Dummy client (reason={})", e.getClass().getSimpleName());
            return fallbackWithReason(e.getClass().getSimpleName(), question, context, focus);
        }
    }
    
    /**
     * Constrói prompt com contexto estruturado + avisos de governança
     */
    private String buildPrompt(String question, String context, ExplainFocus focus) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Você é um assistente de análise de impacto de mudanças em software.\n\n");
        
        prompt.append("⚠️ AVISOS IMPORTANTES:\n");
        prompt.append("- Você é CONSULTIVO (read-only)\n");
        prompt.append("- NUNCA invente riscos ou informações\n");
        prompt.append("- NUNCA altere conclusões ou scores já calculados\n");
        prompt.append("- Use APENAS o contexto fornecido\n\n");
        
        prompt.append("📊 CONTEXTO DO SISTEMA:\n");
        prompt.append(context);
        prompt.append("\n\n");
        
        if (focus != null) {
            prompt.append("🎯 FOCO DA EXPLICAÇÃO: ").append(getFocusDescription(focus)).append("\n\n");
        }
        
        prompt.append("❓ PERGUNTA DO USUÁRIO:\n");
        prompt.append(question);
        prompt.append("\n\n");
        
        prompt.append("Responda de forma clara, objetiva e baseada APENAS no contexto fornecido. ");
        prompt.append("Se não houver informação suficiente, diga isso explicitamente.");
        
        return prompt.toString();
    }
    
    private String getFocusDescription(ExplainFocus focus) {
        return switch (focus) {
            case BUSINESS -> "Impacto em regras de negócio";
            case TECHNICAL -> "Detalhes técnicos e implementação";
            case EXECUTIVE -> "Resumo executivo para gestores";
        };
    }
    
    private String buildRequestBody(String prompt) {
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);
        
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);
        requestBody.add("messages", gson.toJsonTree(new Object[]{gson.fromJson(message, Object.class)}));
        requestBody.addProperty("temperature", 0.3); // Baixa criatividade (mais determinístico)
        requestBody.addProperty("max_tokens", 500);
        
        return gson.toJson(requestBody);
    }
    
    private String parseOpenAIResponse(String responseBody) {
        try {
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            return json.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
        } catch (Exception e) {
            log.error("❌ [LLM] Failed to parse OpenAI response: {}", e.getMessage());
            throw new RuntimeException("Invalid OpenAI response format", e);
        }
    }
    
    private int extractTokenCount(String responseBody) {
        try {
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            return json.getAsJsonObject("usage").get("total_tokens").getAsInt();
        } catch (Exception e) {
            return 0;
        }
    }
    
    private RagAnswer fallbackWithReason(String reason, String question, String context, ExplainFocus focus) {
        log.info("🔄 [LLM] Using internal fallback (reason={})", reason);
        
        // Fallback determinístico simples
        String fallbackAnswer = String.format(
            "⚠️ Não foi possível conectar ao OpenAI (motivo: %s).\n\n" +
            "Com base no contexto disponível:\n" +
            "- Consulte as regras de negócio listadas nas fontes abaixo\n" +
            "- Verifique os arquivos impactados mencionados\n" +
            "- Entre em contato com os responsáveis identificados\n\n" +
            "Recomendo tentar novamente em alguns instantes.",
            reason
        );
        
        return new RagAnswer(fallbackAnswer, ConfidenceLevel.LOW, false);
    }
}
