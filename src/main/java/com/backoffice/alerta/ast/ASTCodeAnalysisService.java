package com.backoffice.alerta.ast;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * US#69 - Serviço de análise estática de código Java com AST.
 * READ-ONLY - sem persistência, sem execução de código.
 */
@Service
public class ASTCodeAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(ASTCodeAnalysisService.class);
    private static final Pattern BUSINESS_RULE_PATTERN = Pattern.compile("@BusinessRule\\s+([A-Z0-9_]+)");

    /**
     * Analisa arquivos Java e identifica impactos relacionados a regras de negócio.
     *
     * @param javaFiles Mapa de filePath -> conteúdo do arquivo
     * @return Lista de detalhes de impacto a nível de AST
     */
    public List<ASTImpactDetail> analyzeFiles(Map<String, String> javaFiles) {
        if (javaFiles == null || javaFiles.isEmpty()) {
            return new ArrayList<>();
        }

        log.info("🧩 [US#69] AST parsing iniciado | arquivos={}", javaFiles.size());
        List<ASTImpactDetail> impacts = new ArrayList<>();
        JavaParser parser = new JavaParser();

        for (Map.Entry<String, String> entry : javaFiles.entrySet()) {
            String filePath = entry.getKey();
            String content = entry.getValue();

            try {
                ParseResult<CompilationUnit> result = parser.parse(content);
                
                if (!result.isSuccessful()) {
                    log.warn("⚠️ [US#69] Erro ao parsear arquivo {} | ignorado", filePath);
                    continue;
                }

                Optional<CompilationUnit> cu = result.getResult();
                if (cu.isEmpty()) {
                    continue;
                }

                impacts.addAll(analyzeCompilationUnit(cu.get(), filePath));

            } catch (Exception e) {
                log.warn("⚠️ [US#69] Erro ao parsear arquivo {} | ignorado | erro={}", 
                    filePath, e.getMessage());
            }
        }

        log.info("🧩 [US#69] AST parsing finalizado | impactos={}", impacts.size());
        return impacts;
    }

    /**
     * Analisa uma CompilationUnit e extrai impactos.
     */
    private List<ASTImpactDetail> analyzeCompilationUnit(CompilationUnit cu, String filePath) {
        List<ASTImpactDetail> impacts = new ArrayList<>();

        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
            String className = classDecl.getFullyQualifiedName()
                .orElse(classDecl.getNameAsString());
            
            log.debug("📄 [US#69] Classe analisada: {}", className);

            classDecl.getMethods().forEach(method -> {
                String methodName = method.getNameAsString();
                log.debug("🔍 [US#69] Método encontrado: {}()", methodName);

                Optional<String> ruleId = extractBusinessRuleId(method, className, methodName);
                
                if (ruleId.isPresent()) {
                    log.info("🔗 [US#69] Regra associada: {}", ruleId.get());
                    
                    ASTImpactDetail impact = new ASTImpactDetail(
                        filePath,
                        className,
                        methodName,
                        method.getBegin().map(pos -> pos.line).orElse(0),
                        method.getEnd().map(pos -> pos.line).orElse(0),
                        "Implementa regra de negócio " + ruleId.get()
                    );
                    
                    impacts.add(impact);
                }
            });
        });

        return impacts;
    }

    /**
     * Extrai ID de regra de negócio através de 3 estratégias:
     * 1. Comentário // @BusinessRule REGRA_ID
     * 2. Convenção de nomenclatura (nome contém ID)
     * 3. Anotações personalizadas (futuro)
     */
    private Optional<String> extractBusinessRuleId(MethodDeclaration method, 
                                                   String className, 
                                                   String methodName) {
        // Estratégia 1: Comentário @BusinessRule
        Optional<String> fromComment = extractFromComment(method);
        if (fromComment.isPresent()) {
            return fromComment;
        }

        // Estratégia 2: Convenção de nomenclatura
        Optional<String> fromNaming = extractFromNaming(className, methodName);
        if (fromNaming.isPresent()) {
            return fromNaming;
        }

        return Optional.empty();
    }

    /**
     * Extrai ID de regra de comentários JavaDoc ou inline.
     */
    private Optional<String> extractFromComment(MethodDeclaration method) {
        // Verificar comentário Javadoc
        if (method.getJavadoc().isPresent()) {
            String javadoc = method.getJavadoc().get().toText();
            Matcher matcher = BUSINESS_RULE_PATTERN.matcher(javadoc);
            if (matcher.find()) {
                return Optional.of(matcher.group(1));
            }
        }

        // Verificar comentários inline acima do método
        Optional<Comment> comment = method.getComment();
        if (comment.isPresent()) {
            Matcher matcher = BUSINESS_RULE_PATTERN.matcher(comment.get().getContent());
            if (matcher.find()) {
                return Optional.of(matcher.group(1));
            }
        }

        return Optional.empty();
    }

    /**
     * Extrai ID de regra através de convenção de nomenclatura.
     * Exemplo: validateREGRA_001() -> REGRA_001
     */
    private Optional<String> extractFromNaming(String className, String methodName) {
        Pattern namingPattern = Pattern.compile("(REGRA_[A-Z0-9_]+)");
        
        // Buscar no nome do método
        Matcher methodMatcher = namingPattern.matcher(methodName);
        if (methodMatcher.find()) {
            return Optional.of(methodMatcher.group(1));
        }

        // Buscar no nome da classe
        Matcher classMatcher = namingPattern.matcher(className);
        if (classMatcher.find()) {
            return Optional.of(classMatcher.group(1));
        }

        return Optional.empty();
    }

    /**
     * Extrai lista de chamadas de métodos dentro de um método.
     * Útil para análise de grafo de chamadas (call graph).
     */
    public List<ASTCallGraphEdge> extractMethodCalls(CompilationUnit cu, ASTMethodNode sourceMethod) {
        List<ASTCallGraphEdge> edges = new ArrayList<>();

        cu.findAll(MethodCallExpr.class).forEach(call -> {
            String calledMethodName = call.getNameAsString();
            List<String> argTypes = call.getArguments().stream()
                .map(arg -> arg.calculateResolvedType().describe())
                .collect(Collectors.toList());

            // Simplified - na prática precisaria de Symbol Solver para resolver tipos
            ASTMethodNode callee = new ASTMethodNode(
                "UnknownClass", 
                calledMethodName,
                argTypes,
                sourceMethod.getFilePath(),
                call.getBegin().map(pos -> pos.line).orElse(0),
                call.getEnd().map(pos -> pos.line).orElse(0)
            );

            edges.add(new ASTCallGraphEdge(sourceMethod, callee));
        });

        return edges;
    }
}
