package com.backoffice.alerta.rag.ast;

import com.backoffice.alerta.rules.BusinessRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AstIndexingService {

    private static final Logger log = LoggerFactory.getLogger(AstIndexingService.class);

    private final JavaAstAnalyzer javaAnalyzer;
    private final TypeScriptAstAnalyzer tsAnalyzer;
    private final AstRepository repository;

    public AstIndexingService(JavaAstAnalyzer javaAnalyzer, TypeScriptAstAnalyzer tsAnalyzer, AstRepository repository) {
        this.javaAnalyzer = javaAnalyzer;
        this.tsAnalyzer = tsAnalyzer;
        this.repository = repository;
    }

    public void indexBusinessRuleAst(BusinessRule rule, UUID projectId) {
        if (rule == null || rule.getSourceFile() == null) return;

        String filePath = rule.getSourceFile();
        String content = rule.getContent();
        if (content == null) content = "";

        List<Map<String, Object>> nodes;
        if (filePath.toLowerCase().endsWith(".java")) {
            nodes = javaAnalyzer.parse(filePath, content);
        } else if (filePath.toLowerCase().endsWith(".ts") || filePath.toLowerCase().endsWith(".tsx") || filePath.toLowerCase().endsWith(".js")) {
            nodes = tsAnalyzer.parse(filePath, content);
        } else {
            // Nothing to parse
            log.debug("[AST] No parser available for {}", filePath);
            return;
        }

        repository.saveAstNodes(projectId, filePath, nodes);

        // Simple ownership heuristic: if a class or function has the same name as rule id, map it
        String ruleId = rule.getId();
        for (Map<String, Object> node : nodes) {
            Object name = node.get("name");
            if (name != null && name.toString().toLowerCase().contains(ruleId.toLowerCase())) {
                repository.saveOwnershipMapping(projectId, filePath, ruleId, "NAMING_CONVENTION", 0.85);
            }
        }

        log.info("[AST] Indexed AST for rule={} file={} nodes={}", ruleId, filePath, nodes.size());
    }
}
