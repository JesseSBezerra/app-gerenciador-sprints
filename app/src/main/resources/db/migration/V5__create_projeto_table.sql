-- Migration: Criação da tabela projeto
CREATE TABLE IF NOT EXISTS projeto (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    descricao TEXT
);

-- Índice para melhorar performance
CREATE INDEX idx_projeto_nome ON projeto(nome);
