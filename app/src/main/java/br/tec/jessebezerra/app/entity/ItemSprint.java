package br.tec.jessebezerra.app.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemSprint {
    private Long id;
    private TipoItem tipo;
    private String titulo;
    private String descricao;
    private Integer duracaoSemanas; // Para Feature, História, Tarefa
    private Integer duracaoDias; // Para SUB
    private StatusItem status;
    private Long sprintId;
    private Long membroId;
    private Long itemPaiId; // Autorelacionamento
    private Long projetoId;
    private Long aplicacaoId;
    private Integer prioridade; // Ordem de prioridade (usado para ordenação de SUBs)
    private String codigoExterno; // Código externo do item
    private java.time.LocalDate dataConclusao; // Data de conclusão da SUB
}
