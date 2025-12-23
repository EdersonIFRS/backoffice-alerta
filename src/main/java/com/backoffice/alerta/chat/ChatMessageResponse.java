package com.backoffice.alerta.chat;

import com.backoffice.alerta.rag.ConfidenceLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

/**
 * Mensagem individual do chat de análise de impacto
 * 
 * US#46 - Chat Unificado de Análise de Impacto
 */
@Schema(description = "Mensagem estruturada do chat de impacto")
public class ChatMessageResponse {
    
    @Schema(description = "Tipo da mensagem", example = "INFO")
    private ChatMessageType type;
    
    @Schema(description = "Título da mensagem", example = "Regras de Negócio Identificadas")
    private String title;
    
    @Schema(description = "Conteúdo detalhado da mensagem")
    private String content;
    
    @Schema(description = "Fontes de informação utilizadas")
    private List<String> sources = new ArrayList<>();
    
    @Schema(description = "Nível de confiança desta mensagem", example = "MEDIUM")
    private ConfidenceLevel confidence;
    
    public ChatMessageResponse() {}
    
    public ChatMessageResponse(ChatMessageType type, String title, String content) {
        this.type = type;
        this.title = title;
        this.content = content;
        this.confidence = ConfidenceLevel.MEDIUM;
    }
    
    public ChatMessageResponse(ChatMessageType type, String title, String content, ConfidenceLevel confidence) {
        this.type = type;
        this.title = title;
        this.content = content;
        this.confidence = confidence;
    }
    
    // Getters and Setters
    
    public ChatMessageType getType() {
        return type;
    }
    
    public void setType(ChatMessageType type) {
        this.type = type;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public List<String> getSources() {
        return sources;
    }
    
    public void setSources(List<String> sources) {
        this.sources = sources;
    }
    
    public ConfidenceLevel getConfidence() {
        return confidence;
    }
    
    public void setConfidence(ConfidenceLevel confidence) {
        this.confidence = confidence;
    }
}
