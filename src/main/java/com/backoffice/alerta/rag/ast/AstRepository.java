package com.backoffice.alerta.rag.ast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class AstRepository {
    private static final Logger log = LoggerFactory.getLogger(AstRepository.class);

    private final JdbcTemplate jdbc;

    public AstRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void saveAstNodes(UUID projectId, String filePath, List<Map<String, Object>> nodes) {
        if (nodes == null || nodes.isEmpty()) return;

        for (Map<String, Object> node : nodes) {
            try {
                jdbc.update(
                    "INSERT INTO code_ast_nodes (id, project_id, file_path, node_type, name, line_start, metadata) VALUES (gen_random_uuid(), ?, ?, ?, ?, ?, ?::jsonb)",
                    projectId,
                    filePath,
                    node.getOrDefault("node_type", "UNKNOWN"),
                    node.getOrDefault("name", null),
                    node.getOrDefault("line_start", null),
                    node.containsKey("metadata") ? node.get("metadata").toString() : null
                );
            } catch (Exception e) {
                log.warn("[AST] Failed to persist node for {}: {}", filePath, e.getMessage());
            }
        }
        log.debug("[AST] Persisted {} nodes for {}", nodes.size(), filePath);
    }

    public void saveOwnershipMapping(UUID projectId, String filePath, String businessRuleId, String evidenceType, double confidence) {
        try {
            jdbc.update(
                "INSERT INTO code_ownership_mapping (id, project_id, file_path, business_rule_id, evidence_type, confidence) VALUES (gen_random_uuid(), ?, ?, ?, ?, ?)",
                projectId,
                filePath,
                businessRuleId,
                evidenceType,
                confidence
            );
        } catch (Exception e) {
            log.debug("[AST] Ownership mapping insert failed (may already exist): {}", e.getMessage());
        }
    }
}
