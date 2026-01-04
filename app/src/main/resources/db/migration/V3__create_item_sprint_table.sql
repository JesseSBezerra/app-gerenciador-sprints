-- Migration: Criação da tabela item_sprint
CREATE TABLE IF NOT EXISTS item_sprint (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tipo TEXT NOT NULL,
    titulo TEXT NOT NULL,
    descricao TEXT,
    duracao_semanas INTEGER,
    duracao_dias INTEGER,
    status TEXT NOT NULL,
    sprint_id INTEGER NOT NULL,
    membro_id INTEGER,
    item_pai_id INTEGER,
    FOREIGN KEY (sprint_id) REFERENCES sprint(id) ON DELETE CASCADE,
    FOREIGN KEY (membro_id) REFERENCES membro(id) ON DELETE SET NULL,
    FOREIGN KEY (item_pai_id) REFERENCES item_sprint(id) ON DELETE CASCADE
);

-- Índices para melhorar performance
CREATE INDEX idx_item_sprint_sprint_id ON item_sprint(sprint_id);
CREATE INDEX idx_item_sprint_membro_id ON item_sprint(membro_id);
CREATE INDEX idx_item_sprint_item_pai_id ON item_sprint(item_pai_id);
CREATE INDEX idx_item_sprint_tipo ON item_sprint(tipo);
CREATE INDEX idx_item_sprint_status ON item_sprint(status);
