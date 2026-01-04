-- Migration inicial: Criação da tabela sprint
CREATE TABLE IF NOT EXISTS sprint (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    data_inicio TEXT NOT NULL,
    duracao_semanas INTEGER NOT NULL,
    data_fim TEXT
);
