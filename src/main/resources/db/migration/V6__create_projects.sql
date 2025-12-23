-- US#48 - Cadastro de Projetos Reais (Contexto de Produto)
-- Tabela para armazenar informações de projetos organizacionais

CREATE TABLE projects (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    repository_type VARCHAR(50) NOT NULL,
    repository_url VARCHAR(500) NOT NULL,
    default_branch VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX idx_projects_name ON projects(name);
CREATE INDEX idx_projects_active ON projects(active);
CREATE INDEX idx_projects_type ON projects(type);
