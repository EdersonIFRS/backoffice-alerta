-- US#57 - Preferências de Alertas por Projeto
-- Criação da tabela de preferências de alertas em nível de projeto

CREATE TABLE project_alert_preferences (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL UNIQUE,
    minimum_severity VARCHAR(20),
    delivery_window VARCHAR(20),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_project_alert_pref_project FOREIGN KEY (project_id) REFERENCES projects(id)
);

CREATE TABLE project_alert_allowed_types (
    preference_id UUID NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    CONSTRAINT fk_project_alert_types FOREIGN KEY (preference_id) REFERENCES project_alert_preferences(id) ON DELETE CASCADE
);

CREATE TABLE project_alert_channels (
    preference_id UUID NOT NULL,
    channel VARCHAR(20) NOT NULL,
    CONSTRAINT fk_project_alert_channels FOREIGN KEY (preference_id) REFERENCES project_alert_preferences(id) ON DELETE CASCADE
);

CREATE INDEX idx_project_alert_pref_project ON project_alert_preferences(project_id);
