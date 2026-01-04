-- Adicionar coluna prioridade na tabela item_sprint
ALTER TABLE item_sprint ADD COLUMN prioridade INTEGER DEFAULT 0;

-- Criar índice para prioridade
CREATE INDEX idx_item_sprint_prioridade ON item_sprint(prioridade);

-- Comentário: 
-- Prioridade: 0 = Baixa, 1 = Média, 2 = Alta, 3 = Crítica
