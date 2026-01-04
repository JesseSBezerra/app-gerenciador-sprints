package br.tec.jessebezerra.app.entity;

public enum Funcao {
    BACKEND("Backend"),
    FRONTEND("Frontend");
    
    private final String descricao;
    
    Funcao(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    @Override
    public String toString() {
        return descricao;
    }
}
