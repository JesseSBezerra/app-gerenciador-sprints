-- Migration: Criação da tabela aplicacao
CREATE TABLE IF NOT EXISTS aplicacao (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    descricao TEXT
);

-- Índice para melhorar performance
CREATE INDEX idx_aplicacao_nome ON aplicacao(nome);
