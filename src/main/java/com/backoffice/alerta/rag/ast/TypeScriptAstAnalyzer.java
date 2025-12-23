package com.backoffice.alerta.rag.ast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TypeScriptAstAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(TypeScriptAstAnalyzer.class);

    public List<Map<String, Object>> parse(String filePath, String content) {
        List<Map<String, Object>> nodes = new ArrayList<>();

        // Extract classes
        Pattern classPattern = Pattern.compile("class\\s+([A-Za-z0-9_]+)");
        Matcher classMatcher = classPattern.matcher(content);
        while (classMatcher.find()) {
            Map<String, Object> node = new HashMap<>();
            node.put("node_type", "CLASS");
            node.put("name", classMatcher.group(1));
            node.put("line_start", estimateLine(content, classMatcher.start()));
            nodes.add(node);
        }

        // Extract functions
        Pattern functionPattern = Pattern.compile("function\\s+([A-Za-z0-9_]+)\\s*\\(");
        Matcher functionMatcher = functionPattern.matcher(content);
        while (functionMatcher.find()) {
            Map<String, Object> node = new HashMap<>();
            node.put("node_type", "FUNCTION");
            node.put("name", functionMatcher.group(1));
            node.put("line_start", estimateLine(content, functionMatcher.start()));
            nodes.add(node);
        }

        log.debug("[AST] Parsed {} nodes for {}", nodes.size(), filePath);
        return nodes;
    }

    private int estimateLine(String content, int offset) {
        return Math.max(1, content.substring(0, Math.min(offset, content.length())).split("\\r?\\n").length);
    }
}
