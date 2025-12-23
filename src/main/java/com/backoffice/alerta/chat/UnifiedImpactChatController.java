package com.backoffice.alerta.chat;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para chat unificado de análise de impacto
 * 
 * Fornece endpoint conversacional que consolida informações de:
 * - Regras de negócio (RAG)
 * - Impacto técnico (código)
 * - Ownership organizacional
 * - Histórico de incidentes
 * - Dependências sistêmicas
 * 
 * US#46 - Chat Unificado de Análise de Impacto (Engenharia + Negócio)
 * 
 * GOVERNANÇA:
 * - Endpoint READ-ONLY
 * - Nenhuma auditoria criada
 * - Nenhum evento disparado
 * - Apenas consulta e explicação
 * - RBAC: ADMIN, RISK_MANAGER, ENGINEER
 */
@RestController
@RequestMapping("/risk/chat")
@Tag(name = "Chat de Análise de Impacto", description = "Chat unificado para análise de impacto de mudanças (US#46)")
public class UnifiedImpactChatController {
    
    private static final Logger log = LoggerFactory.getLogger(UnifiedImpactChatController.class);
    
    private final UnifiedImpactChatService chatService;
    
    public UnifiedImpactChatController(UnifiedImpactChatService chatService) {
        this.chatService = chatService;
    }
    
    @PostMapping("/query")
    @PreAuthorize("hasAnyRole('ADMIN', 'RISK_MANAGER', 'ENGINEER')")
    @Operation(
        summary = "Consulta no chat de análise de impacto",
        description = """
            **Chat Unificado de Análise de Impacto (US#46)**
            
            Este endpoint permite fazer perguntas em linguagem natural sobre mudanças no sistema
            e receber respostas consolidadas que combinam:
            
            • **Regras de Negócio**: Quais regras são afetadas
            • **Impacto Técnico**: Onde alterar código
            • **Ownership**: Quem avisar antes de mexer
            • **Histórico**: Se já causou incidentes
            • **Dependências**: O que pode quebrar
            
            ---
            
            **Características:**
            - ✅ Read-only (nenhuma modificação no sistema)
            - ✅ Nenhuma auditoria criada
            - ✅ Nenhuma notificação enviada
            - ✅ Resposta sempre retornada (fallback automático)
            - ✅ Múltiplas mensagens estruturadas (INFO/WARNING/ACTION)
            
            ---
            
            **Exemplos de Perguntas:**
            - "Onde alterar o cálculo de horas para Pessoa Jurídica?"
            - "Quem preciso avisar antes de mudar regras de pagamento?"
            - "Isso já causou incidente em produção?"
            - "Quais arquivos mexer para validação de CPF?"
            
            ---
            
            **IMPORTANTE:**
            Esta resposta é **consultiva** e não substitui:
            - Revisão técnica por especialistas
            - Aprovação formal de mudanças
            - Testes de qualidade
            - Processo de deploy estabelecido
            """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Pergunta em linguagem natural sobre impacto de mudanças",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ChatQueryRequest.class),
                examples = {
                    @ExampleObject(
                        name = "Pergunta Técnica",
                        summary = "Onde alterar código específico",
                        description = "Pergunta focada em localizar arquivos e código para alteração",
                        value = """
                            {
                              "question": "Onde alterar o cálculo de horas para Pessoa Jurídica?",
                              "focus": "TECHNICAL"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Pergunta Executiva",
                        summary = "Ownership e governança",
                        description = "Pergunta sobre times responsáveis e aprovações necessárias",
                        value = """
                            {
                              "question": "Quem preciso avisar antes de mudar regras de pagamento e isso já causou problema?",
                              "focus": "EXECUTIVE"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Pergunta de Risco",
                        summary = "Histórico de incidentes",
                        description = "Verificar se mudança similar já causou problemas",
                        value = """
                            {
                              "question": "Alterar validação de CPF já causou incidente em produção?",
                              "environment": "PRODUCTION"
                            }
                            """
                    )
                }
            )
        )
    )
    @ApiResponse(
        responseCode = "200",
        description = "Resposta consolidada do chat",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ChatResponse.class),
            examples = @ExampleObject(
                name = "Resposta Consolidada",
                value = """
                    {
                      "answer": "📋 **Regras de Negócio Relevantes:**\\n\\n• REGRA_CALCULO_HORAS_PJ (Criticidade: CRITICA)\\n\\n📄 **Arquivos a Alterar:**\\n\\n• src/main/java/com/app/payment/PaymentService.java - Risco: HIGH\\n\\n👥 **Times Responsáveis:**\\n\\n• Time Pagamentos (PRIMARY_OWNER) - pagamentos@empresa.com\\n",
                      "messages": [
                        {
                          "type": "INFO",
                          "title": "Regras de Negócio Identificadas",
                          "content": "Encontrei 1 regra(s) relacionada(s) à sua pergunta.",
                          "sources": ["REGRA_CALCULO_HORAS_PJ"],
                          "confidence": "MEDIUM"
                        },
                        {
                          "type": "ACTION",
                          "title": "Arquivos Impactados",
                          "content": "Identifiquei 1 arquivo(s) que precisam de atenção.",
                          "sources": ["src/main/java/com/app/payment/PaymentService.java"],
                          "confidence": "MEDIUM"
                        },
                        {
                          "type": "ACTION",
                          "title": "Próximos Passos Recomendados",
                          "content": "1. Revise os arquivos listados\\n2. Verifique dependências e impactos\\n3. Contate os times responsáveis\\n4. Execute testes antes de produção\\n5. Documente as mudanças no PR",
                          "sources": [],
                          "confidence": "HIGH"
                        }
                      ],
                      "confidence": "MEDIUM",
                      "usedFallback": false,
                      "disclaimer": "⚠️ Esta resposta é consultiva e não substitui revisão técnica ou aprovação formal."
                    }
                    """
            )
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "Requisição inválida (pergunta vazia ou parâmetros incorretos)"
    )
    @ApiResponse(
        responseCode = "401",
        description = "Não autenticado (token JWT ausente ou inválido)"
    )
    @ApiResponse(
        responseCode = "403",
        description = "Sem permissão (role: ADMIN, RISK_MANAGER ou ENGINEER requerida)"
    )
    public ResponseEntity<ChatResponse> queryChat(@Valid @RequestBody ChatQueryRequest request) {
        log.info("💬 POST /risk/chat/query - Question: '{}'", request.getQuestion());
        
        try {
            ChatResponse response = chatService.processQuery(request);
            
            log.info("✅ Chat response: {} mensagens, confiança: {}, fallback: {}", 
                     response.getMessages().size(),
                     response.getConfidence(),
                     response.isUsedFallback());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Erro ao processar chat: {}", e.getMessage(), e);
            
            // Nunca retornar 500 - sempre responder com fallback
            ChatResponse fallback = new ChatResponse();
            fallback.setAnswer(
                "❌ Houve um erro ao processar sua pergunta. " +
                "Tente reformular ou contate o suporte se o problema persistir."
            );
            fallback.setUsedFallback(true);
            fallback.setConfidence(com.backoffice.alerta.rag.ConfidenceLevel.LOW);
            
            ChatMessageResponse errorMsg = new ChatMessageResponse(
                ChatMessageType.WARNING,
                "Erro Técnico",
                "Não foi possível processar completamente sua consulta."
            );
            fallback.getMessages().add(errorMsg);
            
            return ResponseEntity.ok(fallback);
        }
    }
}
