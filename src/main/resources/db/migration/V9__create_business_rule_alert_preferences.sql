-- US#57 - Preferências de Alertas por Regra de Negócio
-- Criação da tabela de preferências de alertas em nível de regra (override de projeto)

CREATE TABLE business_rule_alert_preferences (
    id UUID PRIMARY KEY,
    business_rule_id VARCHAR(255) NOT NULL UNIQUE,
    minimum_severity VARCHAR(20),
    delivery_window VARCHAR(20),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE rule_alert_allowed_types (
    preference_id UUID NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    CONSTRAINT fk_rule_alert_types FOREIGN KEY (preference_id) REFERENCES business_rule_alert_preferences(id) ON DELETE CASCADE
);

CREATE TABLE rule_alert_channels (
    preference_id UUID NOT NULL,
    channel VARCHAR(20) NOT NULL,
    CONSTRAINT fk_rule_alert_channels FOREIGN KEY (preference_id) REFERENCES business_rule_alert_preferences(id) ON DELETE CASCADE
);

CREATE INDEX idx_rule_alert_pref_rule ON business_rule_alert_preferences(business_rule_id);
