package br.tec.jessebezerra.app.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class DateUtils {
    
    public static LocalDate calcularDataFim(LocalDate dataInicio, int duracaoSemanas) {
        if (dataInicio == null || duracaoSemanas <= 0) {
            return null;
        }
        
        // Calcular dias úteis: duração em semanas * 5 dias - 1 (último dia é inclusivo)
        int diasUteis = (duracaoSemanas * 5) - 1;
        LocalDate dataAtual = dataInicio;
        int diasAdicionados = 0;
        
        while (diasAdicionados < diasUteis) {
            dataAtual = dataAtual.plusDays(1);
            
            if (dataAtual.getDayOfWeek() != DayOfWeek.SATURDAY && 
                dataAtual.getDayOfWeek() != DayOfWeek.SUNDAY) {
                diasAdicionados++;
            }
        }
        
        return dataAtual;
    }
}
