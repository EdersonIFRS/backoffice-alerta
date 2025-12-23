-- US#30 - Persistência com PostgreSQL/JPA
-- Tabela de feedback de decisões de risco

CREATE TABLE risk_decision_feedback (
    id UUID PRIMARY KEY,
    audit_id UUID NOT NULL UNIQUE,
    pull_request_id VARCHAR(255) NOT NULL,
    final_decision VARCHAR(50) NOT NULL,
    risk_level VARCHAR(50) NOT NULL,
    outcome VARCHAR(50) NOT NULL,
    comments TEXT,
    author VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Índices
CREATE INDEX idx_feedback_audit_id ON risk_decision_feedback(audit_id);
CREATE INDEX idx_feedback_pull_request ON risk_decision_feedback(pull_request_id);
CREATE INDEX idx_feedback_created_at ON risk_decision_feedback(created_at DESC);
