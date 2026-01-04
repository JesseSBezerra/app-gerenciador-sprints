package br.tec.jessebezerra.app.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sprint {
    private Long id;
    private String nome;
    private LocalDate dataInicio;
    private Integer duracaoSemanas;
    private LocalDate dataFim;
}
