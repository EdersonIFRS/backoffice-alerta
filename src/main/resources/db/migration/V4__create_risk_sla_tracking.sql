-- US#30 - Persistência com PostgreSQL/JPA
-- Tabela de rastreamento de SLA

CREATE TABLE risk_sla_tracking (
    id UUID PRIMARY KEY,
    notification_id UUID NOT NULL,
    audit_id UUID NOT NULL,
    pull_request_id VARCHAR(255) NOT NULL,
    risk_level VARCHAR(50) NOT NULL,
    current_level VARCHAR(50) NOT NULL,
    sla_deadline TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    last_escalation_at TIMESTAMP
);

-- Índices
CREATE INDEX idx_sla_status ON risk_sla_tracking(status);
CREATE INDEX idx_sla_audit_id ON risk_sla_tracking(audit_id);
CREATE INDEX idx_sla_created_at ON risk_sla_tracking(created_at DESC);
CREATE INDEX idx_sla_deadline ON risk_sla_tracking(sla_deadline);
