package com.backoffice.alerta.chat;

import com.backoffice.alerta.project.dto.ProjectContext;
import com.backoffice.alerta.project.domain.Project;
import com.backoffice.alerta.project.repository.ProjectRepository;
import com.backoffice.alerta.rag.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Serviço unificado de chat para análise de impacto
 * 
 * Responsável por:
 * - Analisar perguntas em linguagem natural
 * - Identificar intenção do usuário (keyword-based)
 * - Chamar serviços especializados conforme necessário
 * - Consolidar respostas em linguagem clara
 * - Gerar mensagens estruturadas (INFO/WARNING/ACTION)
 * - Sempre retornar resposta válida (fallback determinístico)
 * 
 * US#46 - Chat Unificado de Análise de Impacto (Engenharia + Negócio)
 * 
 * GOVERNANÇA:
 * - READ-ONLY (nenhuma entidade modificada)
 * - Nenhum evento criado
 * - Nenhuma auditoria gerada
 * - Nenhuma notificação enviada
 * - Apenas consulta e explicação
 */
@Service
public class UnifiedImpactChatService {
    
    private static final Logger log = LoggerFactory.getLogger(UnifiedImpactChatService.class);
    
    private final BusinessRuleRagService ragService;
    private final BusinessRuleCodeImpactRagService codeImpactService;
    private final ProjectRepository projectRepository;
    
    public UnifiedImpactChatService(
            BusinessRuleRagService ragService,
            BusinessRuleCodeImpactRagService codeImpactService,
            ProjectRepository projectRepository) {
        this.ragService = ragService;
        this.codeImpactService = codeImpactService;
        this.projectRepository = projectRepository;
    }
    
    /**
     * Processa consulta do chat e retorna resposta consolidada
     */
    public ChatResponse processQuery(ChatQueryRequest request) {
        log.info("💬 Chat Query: '{}' (focus: {})", request.getQuestion(), request.getFocus());
        
        // US#50: Escopo de projeto (opcional)
        Project project = null;
        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Projeto não encontrado: " + request.getProjectId()));
            log.info("🔎 Análise escopada para Projeto: {} ({})", 
                    project.getName(), request.getProjectId());
        } else {
            log.info("🌐 Análise GLOBAL (sem escopo de projeto)");
        }
        
        ChatResponse response = new ChatResponse();
        StringBuilder answer = new StringBuilder();
        
        try {
            // 1. Analisar intenção da pergunta
            QueryIntent intent = analyzeIntent(request.getQuestion());
            log.info("🎯 Intent detectado: {}", intent);
            
            // 2. Executar consultas baseado na intenção
            if (intent.needsBusinessRules) {
                addBusinessRulesInfo(request, response, answer);
            }
            
            if (intent.needsCodeImpact) {
                addCodeImpactInfo(request, response, answer);
            }
            
            if (intent.needsOwnership) {
                addOwnershipInfo(request, response, answer);
            }
            
            if (intent.needsIncidentHistory) {
                addIncidentHistoryInfo(request, response, answer);
            }
            
            // 3. Adicionar mensagem de ação padrão
            addActionRecommendation(response);
            
            // 4. Definir resposta principal
            if (answer.length() == 0) {
                answer.append("Não encontrei informações específicas para sua pergunta, mas posso ajudar com:\n\n");
                answer.append("• Onde alterar código de uma regra específica\n");
                answer.append("• Quem avisar antes de fazer mudanças\n");
                answer.append("• Histórico de incidentes relacionados\n");
                answer.append("• Dependências e impactos sistêmicos\n\n");
                answer.append("Tente reformular com termos como: 'Pessoa Jurídica', 'pagamento', 'validação CPF', 'tributos'.");
                response.setUsedFallback(true);
                response.setConfidence(ConfidenceLevel.LOW);
            } else {
                response.setConfidence(ConfidenceLevel.MEDIUM);
            }
            
            response.setAnswer(answer.toString());
            
        } catch (Exception e) {
            log.error("❌ Erro no chat: {}", e.getMessage(), e);
            return buildErrorResponse(request, project);
        }
        
        // US#50: Adicionar contexto de projeto
        response.setProjectContext(project != null 
            ? ProjectContext.scoped(project.getId(), project.getName())
            : ProjectContext.global());
        
        log.info("✅ Chat response gerado: {} mensagens, confiança: {}, scoped: {}", 
                 response.getMessages().size(), response.getConfidence(),
                 response.getProjectContext().isScoped());
        
        return response;
    }
    
    /**
     * Analisa intenção da pergunta (keyword-based, determinístico)
     */
    private QueryIntent analyzeIntent(String question) {
        String q = question.toLowerCase();
        
        QueryIntent intent = new QueryIntent();
        
        // Palavras-chave para identificar necessidade de buscar regras
        List<String> ruleKeywords = Arrays.asList(
            "regra", "política", "negócio", "validação", "cálculo", "tributo", "imposto",
            "pagamento", "pessoa jurídica", "pj", "cpf", "cnpj"
        );
        intent.needsBusinessRules = ruleKeywords.stream().anyMatch(q::contains);
        
        // Palavras-chave para identificar necessidade de código
        List<String> codeKeywords = Arrays.asList(
            "onde", "arquivo", "código", "alterar", "mudar", "modificar", "implementação",
            "classe", "método", "service", "controller"
        );
        intent.needsCodeImpact = codeKeywords.stream().anyMatch(q::contains);
        
        // Palavras-chave para ownership
        List<String> ownershipKeywords = Arrays.asList(
            "quem", "avisar", "responsável", "dono", "time", "equipe", "contato"
        );
        intent.needsOwnership = ownershipKeywords.stream().anyMatch(q::contains);
        
        // Palavras-chave para histórico
        List<String> historyKeywords = Arrays.asList(
            "histórico", "incidente", "problema", "erro", "falha", "já aconteceu", 
            "causou", "produção"
        );
        intent.needsIncidentHistory = historyKeywords.stream().anyMatch(q::contains);
        
        // Se nenhuma intenção específica, buscar tudo
        if (!intent.needsBusinessRules && !intent.needsCodeImpact && 
            !intent.needsOwnership && !intent.needsIncidentHistory) {
            intent.needsBusinessRules = true;
            intent.needsCodeImpact = true;
        }
        
        return intent;
    }
    
    /**
     * Adiciona informações sobre regras de negócio
     */
    private void addBusinessRulesInfo(ChatQueryRequest request, ChatResponse response, StringBuilder answer) {
        try {
            log.info("🔍 Buscando regras de negócio...");
            
            RagQueryRequest ragRequest = new RagQueryRequest(request.getQuestion());
            if (request.getFocus() != null) {
                ragRequest.setFocus(request.getFocus());
            }
            ragRequest.setMaxSources(5);
            ragRequest.setProjectId(request.getProjectId());
            
            RagQueryResponse ragResponse = ragService.query(ragRequest);
            
            if (!ragResponse.getSources().isEmpty()) {
                // ✅ USAR A RESPOSTA INTELIGENTE DO CHATGPT
                if (ragResponse.getAnswer() != null && !ragResponse.getAnswer().isBlank()) {
                    answer.append(ragResponse.getAnswer()).append("\n\n");
                } else {
                    // Fallback para formato técnico se OpenAI não retornou resposta
                    answer.append("📋 **Regras de Negócio Relevantes:**\n\n");
                }
                
                ChatMessageResponse msg = new ChatMessageResponse(
                    ChatMessageType.INFO,
                    "Regras de Negócio Identificadas",
                    String.format("Encontrei %d regra(s) relacionada(s) à sua pergunta.", 
                                  ragResponse.getSources().size())
                );
                
                for (RagSourceReference source : ragResponse.getSources()) {
                    if (ragResponse.getAnswer() == null || ragResponse.getAnswer().isBlank()) {
                        // Só adiciona lista técnica se não tiver resposta do ChatGPT
                        answer.append(String.format("• **%s** (Criticidade: %s)\n", 
                                                   source.getTitle(), source.getCriticality()));
                    }
                    msg.getSources().add(source.getTitle());
                }
                if (ragResponse.getAnswer() == null || ragResponse.getAnswer().isBlank()) {
                    answer.append("\n");
                }
                
                msg.setConfidence(ragResponse.getConfidence());
                response.getMessages().add(msg);
            }
            
        } catch (Exception e) {
            log.warn("⚠️ Erro ao buscar regras: {}", e.getMessage());
        }
    }
    
    /**
     * Adiciona informações sobre impacto em código
     */
    private void addCodeImpactInfo(ChatQueryRequest request, ChatResponse response, StringBuilder answer) {
        try {
            log.info("📄 Analisando impacto em código...");
            
            RagCodeImpactRequest impactRequest = new RagCodeImpactRequest();
            impactRequest.setQuestion(request.getQuestion());
            if (request.getFocus() != null) {
                impactRequest.setFocus(request.getFocus());
            }
            impactRequest.setMaxFiles(5);
            impactRequest.setProjectId(request.getProjectId());
            
            RagCodeImpactResponse impactResponse = codeImpactService.analyzeCodeImpact(impactRequest);
            
            if (!impactResponse.getImpactedFiles().isEmpty()) {
                answer.append("📄 **Arquivos a Alterar:**\n\n");
                
                ChatMessageResponse msg = new ChatMessageResponse(
                    ChatMessageType.ACTION,
                    "Arquivos Impactados",
                    String.format("Identifiquei %d arquivo(s) que precisam de atenção.", 
                                  impactResponse.getImpactedFiles().size())
                );
                
                for (RagCodeImpactResponse.ImpactedFileInfo file : impactResponse.getImpactedFiles()) {
                    answer.append(String.format("• `%s` - Risco: %s\n", 
                                               file.getFilePath(), file.getRiskLevel()));
                    msg.getSources().add(file.getFilePath());
                }
                answer.append("\n");
                
                // Adicionar info de dependências se houver
                if (impactResponse.getDependencyImpact().getDirect() > 0) {
                    ChatMessageResponse warningMsg = new ChatMessageResponse(
                        ChatMessageType.WARNING,
                        "Atenção: Dependências Detectadas",
                        String.format("Esta mudança afeta %d dependência(s) direta(s). " +
                                     "Outras regras podem ser impactadas em cascata.",
                                     impactResponse.getDependencyImpact().getDirect())
                    );
                    warningMsg.setConfidence(ConfidenceLevel.HIGH);
                    response.getMessages().add(warningMsg);
                    
                    answer.append("⚠️ **Atenção:** Esta mudança tem dependências que podem causar efeito cascata.\n\n");
                }
                
                msg.setConfidence(impactResponse.getConfidence());
                response.getMessages().add(msg);
                
                // US#69: Adicionar informações detalhadas de AST se disponíveis
                if (!impactResponse.getAstDetails().isEmpty()) {
                    answer.append("🧩 **Análise Detalhada (Métodos/Classes):**\n\n");
                    
                    ChatMessageResponse astMsg = new ChatMessageResponse(
                        ChatMessageType.INFO,
                        "Detalhes a Nível de AST",
                        String.format("Foram identificados %d método(s)/classe(s) impactado(s) na análise estática de código.",
                                      impactResponse.getAstDetails().size())
                    );
                    
                    for (var astDetail : impactResponse.getAstDetails()) {
                        answer.append(String.format("• **Método**: `%s.%s()` [linhas %d-%d]\n",
                                                   astDetail.getClassName(), 
                                                   astDetail.getMethodName(),
                                                   astDetail.getLineStart(),
                                                   astDetail.getLineEnd()));
                        answer.append(String.format("  → %s\n", astDetail.getReason()));
                        astMsg.getSources().add(String.format("%s:%d", astDetail.getFilePath(), astDetail.getLineStart()));
                    }
                    answer.append("\n");
                    
                    astMsg.setConfidence(ConfidenceLevel.HIGH);
                    response.getMessages().add(astMsg);
                    
                    log.info("🧩 [US#69] Detalhes AST incluídos na resposta do chat | métodos={}", 
                             impactResponse.getAstDetails().size());
                }
            }
            
        } catch (Exception e) {
            log.warn("⚠️ Erro ao analisar código: {}", e.getMessage());
        }
    }
    
    /**
     * Adiciona informações sobre ownership
     */
    private void addOwnershipInfo(ChatQueryRequest request, ChatResponse response, StringBuilder answer) {
        try {
            log.info("👥 Buscando informações de ownership...");
            
            // Reutilizar code impact que já busca ownership
            RagCodeImpactRequest impactRequest = new RagCodeImpactRequest();
            impactRequest.setQuestion(request.getQuestion());
            impactRequest.setMaxFiles(3);
            
            RagCodeImpactResponse impactResponse = codeImpactService.analyzeCodeImpact(impactRequest);
            
            if (!impactResponse.getOwnerships().isEmpty()) {
                answer.append("👥 **Times Responsáveis:**\n\n");
                
                ChatMessageResponse msg = new ChatMessageResponse(
                    ChatMessageType.INFO,
                    "Times que Devem Ser Avisados",
                    String.format("Identifiquei %d time(s) responsável(is) por essas regras.", 
                                  impactResponse.getOwnerships().size())
                );
                
                for (RagCodeImpactResponse.OwnershipInfo ownership : impactResponse.getOwnerships()) {
                    answer.append(String.format("• **%s** (%s) - %s\n", 
                                               ownership.getTeamName(), 
                                               ownership.getRole(),
                                               ownership.getContactEmail()));
                    msg.getSources().add(ownership.getTeamName());
                }
                answer.append("\n");
                
                response.getMessages().add(msg);
            } else {
                answer.append("👥 **Times Responsáveis:** Nenhum ownership específico cadastrado para essas regras.\n\n");
            }
            
        } catch (Exception e) {
            log.warn("⚠️ Erro ao buscar ownership: {}", e.getMessage());
        }
    }
    
    /**
     * Adiciona informações sobre histórico de incidentes
     */
    private void addIncidentHistoryInfo(ChatQueryRequest request, ChatResponse response, StringBuilder answer) {
        try {
            log.info("⚠️ Verificando histórico de incidentes...");
            
            // Usar code impact que já verifica incidentes
            RagCodeImpactRequest impactRequest = new RagCodeImpactRequest();
            impactRequest.setQuestion(request.getQuestion());
            impactRequest.setMaxFiles(3);
            
            RagCodeImpactResponse impactResponse = codeImpactService.analyzeCodeImpact(impactRequest);
            
            boolean hasIncidents = impactResponse.getImpactedRules().stream()
                .anyMatch(RagCodeImpactResponse.ImpactedRuleInfo::isHasIncidents);
            
            if (hasIncidents) {
                ChatMessageResponse warningMsg = new ChatMessageResponse(
                    ChatMessageType.WARNING,
                    "Histórico de Incidentes",
                    "Atenção: Uma ou mais regras relacionadas JÁ causaram incidentes em produção. " +
                    "Revise cuidadosamente antes de alterar."
                );
                warningMsg.setConfidence(ConfidenceLevel.HIGH);
                response.getMessages().add(warningMsg);
                
                answer.append("⚠️ **Atenção:** Histórico de incidentes detectado para essas regras.\n\n");
            } else {
                answer.append("✅ **Histórico:** Nenhum incidente registrado para essas regras.\n\n");
            }
            
        } catch (Exception e) {
            log.warn("⚠️ Erro ao verificar incidentes: {}", e.getMessage());
        }
    }
    
    /**
     * Adiciona recomendação de ação padrão
     */
    private void addActionRecommendation(ChatResponse response) {
        ChatMessageResponse actionMsg = new ChatMessageResponse(
            ChatMessageType.ACTION,
            "Próximos Passos Recomendados",
            "1. Revise os arquivos listados\n" +
            "2. Verifique dependências e impactos\n" +
            "3. Contate os times responsáveis\n" +
            "4. Execute testes antes de produção\n" +
            "5. Documente as mudanças no PR"
        );
        actionMsg.setConfidence(ConfidenceLevel.HIGH);
        response.getMessages().add(actionMsg);
    }
    
    /**
     * Cria resposta de erro amigável
     */
    private ChatResponse buildErrorResponse(ChatQueryRequest request, Project project) {
        ChatResponse response = new ChatResponse();
        response.setAnswer(
            "❌ Ops! Tive um problema ao processar sua pergunta. " +
            "Mas não se preocupe, você pode:\n\n" +
            "• Tentar reformular a pergunta\n" +
            "• Usar termos específicos como nomes de regras\n" +
            "• Perguntar sobre 'Pessoa Jurídica', 'pagamento', 'validação', etc.\n\n" +
            "Ou contate o time de suporte se o problema persistir."
        );
        response.setUsedFallback(true);
        response.setConfidence(ConfidenceLevel.LOW);
        
        // US#50: Adicionar contexto de projeto
        response.setProjectContext(project != null 
            ? ProjectContext.scoped(project.getId(), project.getName())
            : ProjectContext.global());
        
        ChatMessageResponse errorMsg = new ChatMessageResponse(
            ChatMessageType.WARNING,
            "Erro ao Processar Consulta",
            "Houve um erro técnico. Tente novamente ou reformule sua pergunta."
        );
        response.getMessages().add(errorMsg);
        
        return response;
    }
    
    /**
     * Classe interna para representar intenção da query
     */
    private static class QueryIntent {
        boolean needsBusinessRules = false;
        boolean needsCodeImpact = false;
        boolean needsOwnership = false;
        boolean needsIncidentHistory = false;
        
        @Override
        public String toString() {
            return String.format("QueryIntent[rules=%s, code=%s, ownership=%s, incidents=%s]",
                needsBusinessRules, needsCodeImpact, needsOwnership, needsIncidentHistory);
        }
    }
}
