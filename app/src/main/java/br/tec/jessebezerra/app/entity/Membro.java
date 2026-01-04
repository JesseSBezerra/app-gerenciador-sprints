package br.tec.jessebezerra.app.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Membro {
    private Long id;
    private String nome;
    private Funcao funcao;
    private Boolean ativo;
    private String especialidades;
}
