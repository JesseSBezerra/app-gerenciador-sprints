-- Adicionar colunas projeto_id e aplicacao_id na tabela item_sprint
ALTER TABLE item_sprint ADD COLUMN projeto_id INTEGER;
ALTER TABLE item_sprint ADD COLUMN aplicacao_id INTEGER;

-- Criar índices para melhor performance
CREATE INDEX idx_item_sprint_projeto ON item_sprint(projeto_id);
CREATE INDEX idx_item_sprint_aplicacao ON item_sprint(aplicacao_id);

-- Adicionar foreign keys (SQLite suporta FK mas não permite ALTER TABLE ADD CONSTRAINT em versões antigas)
-- As FKs serão validadas pela aplicação
