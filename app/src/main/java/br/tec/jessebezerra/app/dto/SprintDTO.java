package br.tec.jessebezerra.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SprintDTO {
    private Long id;
    private String nome;
    private LocalDate dataInicio;
    private Integer duracaoSemanas;
    private LocalDate dataFim;
    
    @Override
    public String toString() {
        return nome;
    }
}
