package br.tec.jessebezerra.app.entity;

public enum TipoItem {
    FEATURE("Feature", null),
    HISTORIA("História", FEATURE),
    TAREFA("Tarefa", FEATURE),
    SUB("Sub", null); // SUB pode estar abaixo de HISTORIA ou TAREFA
    
    private final String descricao;
    private final TipoItem tipoPai;
    
    TipoItem(String descricao, TipoItem tipoPai) {
        this.descricao = descricao;
        this.tipoPai = tipoPai;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public TipoItem getTipoPai() {
        return tipoPai;
    }
    
    public boolean podeSerFilhoDe(TipoItem tipo) {
        if (this == SUB) {
            return tipo == HISTORIA || tipo == TAREFA;
        }
        return this.tipoPai == tipo;
    }
    
    @Override
    public String toString() {
        return descricao;
    }
}
