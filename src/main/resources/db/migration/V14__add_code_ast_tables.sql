-- V14: Add AST tables for Phase 2
-- Creates tables to store AST nodes and dependencies extracted from source code

CREATE TABLE IF NOT EXISTS code_ast_nodes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    file_path VARCHAR(1024) NOT NULL,
    node_type VARCHAR(64) NOT NULL,
    name VARCHAR(512),
    line_start INT,
    line_end INT,
    parent_node_id UUID,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_code_ast_project ON code_ast_nodes(project_id);
CREATE INDEX IF NOT EXISTS idx_code_ast_file ON code_ast_nodes(file_path);

CREATE TABLE IF NOT EXISTS code_dependencies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    source_file VARCHAR(1024) NOT NULL,
    target_file VARCHAR(1024) NOT NULL,
    dependency_type VARCHAR(64),
    confidence numeric(5,4) DEFAULT 1.0,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_code_deps_project ON code_dependencies(project_id);
CREATE INDEX IF NOT EXISTS idx_code_deps_source ON code_dependencies(source_file);

CREATE TABLE IF NOT EXISTS code_ownership_mapping (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    file_path VARCHAR(1024) NOT NULL,
    business_rule_id VARCHAR(256) NOT NULL,
    evidence_type VARCHAR(64),
    confidence numeric(5,4) DEFAULT 0.75,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_code_ownership_project ON code_ownership_mapping(project_id);
CREATE INDEX IF NOT EXISTS idx_code_ownership_rule ON code_ownership_mapping(business_rule_id);
