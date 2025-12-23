-- US#30 - Persistência com PostgreSQL/JPA
-- Tabela de auditoria de decisões de risco

CREATE TABLE risk_decision_audit (
    id UUID PRIMARY KEY,
    pull_request_id VARCHAR(255) NOT NULL,
    environment VARCHAR(50) NOT NULL,
    risk_level VARCHAR(50) NOT NULL,
    risk_score INTEGER NOT NULL,
    final_decision VARCHAR(50) NOT NULL,
    ai_consulted BOOLEAN NOT NULL,
    ai_summary TEXT,
    policy_snapshot TEXT,
    created_at TIMESTAMP NOT NULL
);

-- Tabelas auxiliares para coleções
CREATE TABLE risk_audit_impacted_rules (
    audit_id UUID NOT NULL,
    business_rule_id VARCHAR(255),
    FOREIGN KEY (audit_id) REFERENCES risk_decision_audit(id) ON DELETE CASCADE
);

CREATE TABLE risk_audit_incident_summary (
    audit_id UUID NOT NULL,
    severity VARCHAR(50) NOT NULL,
    count INTEGER NOT NULL,
    FOREIGN KEY (audit_id) REFERENCES risk_decision_audit(id) ON DELETE CASCADE
);

CREATE TABLE risk_audit_restrictions (
    audit_id UUID NOT NULL,
    restriction TEXT,
    FOREIGN KEY (audit_id) REFERENCES risk_decision_audit(id) ON DELETE CASCADE
);

-- Índices para melhorar performance
CREATE INDEX idx_audit_pull_request ON risk_decision_audit(pull_request_id);
CREATE INDEX idx_audit_created_at ON risk_decision_audit(created_at DESC);
CREATE INDEX idx_audit_final_decision ON risk_decision_audit(final_decision);
