package br.tec.jessebezerra.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AplicacaoDTO {
    private Long id;
    private String nome;
    private String descricao;
    
    @Override
    public String toString() {
        return nome;
    }
}
