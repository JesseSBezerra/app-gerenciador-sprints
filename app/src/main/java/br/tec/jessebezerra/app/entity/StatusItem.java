package br.tec.jessebezerra.app.entity;

public enum StatusItem {
    CRIADO("Criado"),
    PLANEJADO("Planejado"),
    REFINADO("Refinado"),
    EM_EXECUCAO("Em Execução"),
    EM_TESTES("Em Testes"),
    CONCLUIDO("Concluído"),
    CANCELADO("Cancelado"),
    IMPEDIDO("Impedido");
    
    private final String descricao;
    
    StatusItem(String descricao) {
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
