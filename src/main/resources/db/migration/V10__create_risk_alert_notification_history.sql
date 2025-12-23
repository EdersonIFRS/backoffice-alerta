-- US#59 - Histórico e Rastreabilidade de Notificações de Alerta
-- Criação da tabela de histórico de notificações de alertas

CREATE TABLE risk_alert_notification_history (
    id UUID PRIMARY KEY,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    project_id UUID,
    project_name VARCHAR(255),
    business_rule_id VARCHAR(255),
    business_rule_name VARCHAR(255),
    message_summary VARCHAR(255) NOT NULL,
    delivery_reason VARCHAR(255) NOT NULL,
    recipient VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL
);

-- Índices para otimizar queries comuns
CREATE INDEX idx_alert_history_project ON risk_alert_notification_history(project_id);
CREATE INDEX idx_alert_history_rule ON risk_alert_notification_history(business_rule_id);
CREATE INDEX idx_alert_history_status ON risk_alert_notification_history(status);
CREATE INDEX idx_alert_history_severity ON risk_alert_notification_history(severity);
CREATE INDEX idx_alert_history_created ON risk_alert_notification_history(created_at DESC);
CREATE INDEX idx_alert_history_channel ON risk_alert_notification_history(channel);
