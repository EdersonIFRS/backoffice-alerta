-- US#49: Associação de Regras de Negócio a Projetos
-- Permite associar múltiplas BusinessRules a múltiplos Projects

CREATE TABLE project_business_rules (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    business_rule_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    
    -- Foreign Key apenas para projects (business_rule é gerenciado por JPA)
    CONSTRAINT fk_pbr_project 
        FOREIGN KEY (project_id) 
        REFERENCES projects(id) 
        ON DELETE CASCADE,
    
    -- Evita duplicação
    CONSTRAINT uk_project_business_rule 
        UNIQUE (project_id, business_rule_id)
);

-- Índices para performance
CREATE INDEX idx_pbr_project_id ON project_business_rules(project_id);
CREATE INDEX idx_pbr_business_rule_id ON project_business_rules(business_rule_id);
CREATE INDEX idx_pbr_created_at ON project_business_rules(created_at);
