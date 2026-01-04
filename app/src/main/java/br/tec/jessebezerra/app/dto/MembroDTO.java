package br.tec.jessebezerra.app.dto;

import br.tec.jessebezerra.app.entity.Funcao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembroDTO {
    private Long id;
    private String nome;
    private Funcao funcao;
    private Boolean ativo;
    private String especialidades;
    
    @Override
    public String toString() {
        return nome;
    }
}
