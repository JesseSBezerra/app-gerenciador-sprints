-- Migration: Criação da tabela membro
CREATE TABLE IF NOT EXISTS membro (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    funcao TEXT NOT NULL,
    ativo INTEGER NOT NULL DEFAULT 1,
    especialidades TEXT
);
