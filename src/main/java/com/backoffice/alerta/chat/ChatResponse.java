package com.backoffice.alerta.chat;

import com.backoffice.alerta.project.dto.ProjectContext;
import com.backoffice.alerta.rag.ConfidenceLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

/**
 * Resposta consolidada do chat de análise de impacto
 * 
 * US#46 - Chat Unificado de Análise de Impacto (Engenharia + Negócio)
 */
@Schema(description = "Resposta consolidada do chat de análise de impacto")
public class ChatResponse {
    
    @Schema(description = "Resposta principal em linguagem natural")
    private String answer;
    
    @Schema(description = "Mensagens estruturadas (INFO/WARNING/ACTION)")
    private List<ChatMessageResponse> messages = new ArrayList<>();
    
    @Schema(description = "Nível de confiança geral da resposta", example = "MEDIUM")
    private ConfidenceLevel confidence;
    
    @Schema(description = "Se usou fallback determinístico ou conseguiu usar serviços avançados")
    private boolean usedFallback;
    
    @Schema(description = "Disclaimer legal e de governança")
    private String disclaimer;
    
    @Schema(description = "Contexto de escopo de projeto (se aplicável)")
    private ProjectContext projectContext;
    
    public ChatResponse() {
        this.confidence = ConfidenceLevel.MEDIUM;
        this.usedFallback = false;
        this.disclaimer = "⚠️ Esta resposta é consultiva e não substitui revisão técnica ou aprovação formal.";
    }
    
    // Getters and Setters
    
    public String getAnswer() {
        return answer;
    }
    
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    
    public List<ChatMessageResponse> getMessages() {
        return messages;
    }
    
    public void setMessages(List<ChatMessageResponse> messages) {
        this.messages = messages;
    }
    
    public ConfidenceLevel getConfidence() {
        return confidence;
    }
    
    public void setConfidence(ConfidenceLevel confidence) {
        this.confidence = confidence;
    }
    
    public boolean isUsedFallback() {
        return usedFallback;
    }
    
    public void setUsedFallback(boolean usedFallback) {
        this.usedFallback = usedFallback;
    }
    
    public String getDisclaimer() {
        return disclaimer;
    }
    
    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
    
    public ProjectContext getProjectContext() {
        return projectContext;
    }
    
    public void setProjectContext(ProjectContext projectContext) {
        this.projectContext = projectContext;
    }
}
