package com.backoffice.alerta.rag.ast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AstQueryService {
    private static final Logger log = LoggerFactory.getLogger(AstQueryService.class);

    private final JdbcTemplate jdbc;

    public AstQueryService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Retorna implementações (arquivos, classes, métodos) associadas a uma regra
     */
    public List<Map<String, Object>> findImplementations(String businessRuleId, UUID projectId) {
        List<Map<String, Object>> results = new ArrayList<>();

        try {
            String sql = "SELECT file_path, confidence, evidence_type, metadata FROM code_ownership_mapping WHERE business_rule_id = ? AND project_id = ?";
            List<Map<String, Object>> rows = jdbc.queryForList(sql, businessRuleId, projectId);

            for (Map<String, Object> row : rows) {
                Map<String, Object> item = new HashMap<>();
                item.put("file", row.get("file_path"));
                item.put("confidence", row.get("confidence"));
                item.put("evidence", row.get("evidence_type"));
                item.put("metadata", row.get("metadata"));
                results.add(item);
            }

            // If no explicit ownership mapping, try to infer from AST nodes
            if (results.isEmpty()) {
                String nodeSql = "SELECT node_type, name, line_start, line_end, file_path FROM code_ast_nodes WHERE project_id = ? AND LOWER(name) LIKE LOWER(?) LIMIT 10";
                List<Map<String, Object>> nodeRows = jdbc.queryForList(nodeSql, projectId, "%" + businessRuleId + "%");
                for (Map<String, Object> nr : nodeRows) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("file", nr.get("file_path"));
                    item.put("node_type", nr.get("node_type"));
                    item.put("name", nr.get("name"));
                    item.put("line_start", nr.get("line_start"));
                    item.put("line_end", nr.get("line_end"));
                    item.put("confidence", 0.6);
                    item.put("evidence", "NAMING_CONVENTION");
                    results.add(item);
                }
            }

        } catch (Exception e) {
            log.warn("[AST] Query failed for rule {}: {}", businessRuleId, e.getMessage());
        }

        return results;
    }
}
