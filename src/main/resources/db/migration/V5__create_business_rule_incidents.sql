-- US#30 - Persistência com PostgreSQL/JPA
-- Tabela de incidentes de regras de negócio

CREATE TABLE business_rule_incident (
    id UUID PRIMARY KEY,
    business_rule_id UUID NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    severity VARCHAR(50) NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Índices
CREATE INDEX idx_incident_business_rule ON business_rule_incident(business_rule_id);
CREATE INDEX idx_incident_occurred_at ON business_rule_incident(occurred_at DESC);
CREATE INDEX idx_incident_severity ON business_rule_incident(severity);
