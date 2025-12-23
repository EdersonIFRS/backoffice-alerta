-- US#30 - Persistência com PostgreSQL/JPA
-- Tabela de notificações de risco

CREATE TABLE risk_notification (
    id UUID PRIMARY KEY,
    audit_id UUID NOT NULL,
    pull_request_id VARCHAR(255) NOT NULL,
    business_rule_id UUID NOT NULL,
    team_name VARCHAR(255) NOT NULL,
    team_type VARCHAR(50) NOT NULL,
    ownership_role VARCHAR(50) NOT NULL,
    notification_trigger VARCHAR(50) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Índices
CREATE INDEX idx_notification_audit_id ON risk_notification(audit_id);
CREATE INDEX idx_notification_team_name ON risk_notification(team_name);
CREATE INDEX idx_notification_severity ON risk_notification(severity);
CREATE INDEX idx_notification_created_at ON risk_notification(created_at DESC);
