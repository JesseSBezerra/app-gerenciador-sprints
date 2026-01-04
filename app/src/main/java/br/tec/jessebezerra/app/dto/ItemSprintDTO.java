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
    
    @Override
    public String toString() {
        return titulo;
    }
}
