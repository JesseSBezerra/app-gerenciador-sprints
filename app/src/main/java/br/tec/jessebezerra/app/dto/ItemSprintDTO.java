package br.tec.jessebezerra.app.dto;

import br.tec.jessebezerra.app.entity.StatusItem;
import br.tec.jessebezerra.app.entity.TipoItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemSprintDTO {
    private Long id;
    private TipoItem tipo;
    private String titulo;
    private String descricao;
    private Integer duracaoSemanas; // Para Feature, História, Tarefa
    private Integer duracaoDias; // Para SUB
    private StatusItem status;
    private Long sprintId;
    private String sprintNome; // Para exibição
    private Long membroId;
    private String membroNome; // Para exibição
    private Long itemPaiId;
    private String itemPaiTitulo; // Para exibição
    private Long projetoId;
    private String projetoNome; // Para exibição
    private Long aplicacaoId;
    private String aplicacaoNome; // Para exibição
    private Integer prioridade; // Ordem de prioridade (usado para ordenação de SUBs)
    private String codigoExterno; // Código externo do item
    private java.time.LocalDate dataConclusao; // Data de conclusão da SUB
    
    @Override
    public String toString() {
        return titulo;
    }
}
