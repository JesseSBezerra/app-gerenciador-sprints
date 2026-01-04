package br.tec.jessebezerra.app.service;

import br.tec.jessebezerra.app.dto.ItemSprintDTO;
import br.tec.jessebezerra.app.entity.TipoItem;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço especializado para gerenciar Tarefas e suas SUBs.
 * Encapsula regras de negócio específicas de Tarefas.
 */
public class TarefaService {
    
    private final ItemSprintService itemSprintService;
    
    public TarefaService() {
        this.itemSprintService = new ItemSprintService();
    }
    
    /**
     * Cria uma nova Tarefa
     */
    public ItemSprintDTO createTarefa(ItemSprintDTO dto) {
        dto.setTipo(TipoItem.TAREFA);
        return itemSprintService.create(dto);
    }
    
    /**
     * Atualiza uma Tarefa existente
     */
    public ItemSprintDTO updateTarefa(ItemSprintDTO dto) {
        dto.setTipo(TipoItem.TAREFA);
        return itemSprintService.update(dto);
    }
    
    /**
     * Exclui uma Tarefa
     */
    public void deleteTarefa(Long id) {
        itemSprintService.delete(id);
    }
    
    /**
     * Busca uma Tarefa por ID
     */
    public ItemSprintDTO findTarefaById(Long id) {
        return itemSprintService.findById(id)
            .filter(item -> item.getTipo() == TipoItem.TAREFA)
            .orElse(null);
    }
    
    /**
     * Lista todas as Tarefas
     */
    public List<ItemSprintDTO> findAllTarefas() {
        return itemSprintService.findAll().stream()
            .filter(item -> item.getTipo() == TipoItem.TAREFA)
            .collect(Collectors.toList());
    }
    
    /**
     * Lista Tarefas de uma Sprint específica
     */
    public List<ItemSprintDTO> findTarefasBySprint(Long sprintId) {
        return itemSprintService.findBySprintId(sprintId).stream()
            .filter(item -> item.getTipo() == TipoItem.TAREFA)
            .collect(Collectors.toList());
    }
    
    /**
     * Lista Tarefas de uma Feature específica
     */
    public List<ItemSprintDTO> findTarefasByFeature(Long featureId) {
        return itemSprintService.findByItemPaiId(featureId).stream()
            .filter(item -> item.getTipo() == TipoItem.TAREFA)
            .collect(Collectors.toList());
    }
    
    /**
     * Lista todas as SUBs de uma Tarefa
     */
    public List<ItemSprintDTO> findSubsByTarefa(Long tarefaId) {
        return itemSprintService.findByItemPaiId(tarefaId).stream()
            .filter(item -> item.getTipo() == TipoItem.SUB)
            .collect(Collectors.toList());
    }
    
    /**
     * Cria uma SUB vinculada a uma Tarefa
     */
    public ItemSprintDTO createSub(ItemSprintDTO dto, Long tarefaId, Long sprintId) {
        dto.setTipo(TipoItem.SUB);
        dto.setItemPaiId(tarefaId);
        dto.setSprintId(sprintId);
        
        // Validar que a SUB não ultrapassa a duração da Tarefa
        ItemSprintDTO tarefa = findTarefaById(tarefaId);
        if (tarefa != null && tarefa.getDuracaoSemanas() != null) {
            int duracaoTarefaEmDias = tarefa.getDuracaoSemanas() * 5;
            if (dto.getDuracaoDias() != null && dto.getDuracaoDias() > duracaoTarefaEmDias) {
                throw new IllegalArgumentException(
                    String.format("A duração da SUB (%d dias) não pode ultrapassar a duração da Tarefa (%d dias).",
                        dto.getDuracaoDias(), duracaoTarefaEmDias));
            }
        }
        
        return itemSprintService.create(dto);
    }
    
    /**
     * Atualiza uma SUB
     */
    public ItemSprintDTO updateSub(ItemSprintDTO dto) {
        dto.setTipo(TipoItem.SUB);
        
        // Validar que a SUB não ultrapassa a duração da Tarefa
        if (dto.getItemPaiId() != null) {
            ItemSprintDTO tarefa = findTarefaById(dto.getItemPaiId());
            if (tarefa != null && tarefa.getDuracaoSemanas() != null) {
                int duracaoTarefaEmDias = tarefa.getDuracaoSemanas() * 5;
                if (dto.getDuracaoDias() != null && dto.getDuracaoDias() > duracaoTarefaEmDias) {
                    throw new IllegalArgumentException(
                        String.format("A duração da SUB (%d dias) não pode ultrapassar a duração da Tarefa (%d dias).",
                            dto.getDuracaoDias(), duracaoTarefaEmDias));
                }
            }
        }
        
        return itemSprintService.update(dto);
    }
    
    /**
     * Exclui uma SUB
     */
    public void deleteSub(Long id) {
        itemSprintService.delete(id);
    }
    
    /**
     * Valida se uma Tarefa pode ser criada/atualizada
     */
    public void validateTarefa(ItemSprintDTO dto) {
        if (dto.getTitulo() == null || dto.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("O título da Tarefa é obrigatório.");
        }
        
        if (dto.getStatus() == null) {
            throw new IllegalArgumentException("O status da Tarefa é obrigatório.");
        }
        
        if (dto.getSprintId() == null) {
            throw new IllegalArgumentException("A Tarefa deve estar vinculada a uma Sprint.");
        }
        
        if (dto.getDuracaoSemanas() == null || dto.getDuracaoSemanas() <= 0) {
            throw new IllegalArgumentException("A duração da Tarefa deve ser maior que zero.");
        }
    }
    
    /**
     * Valida se uma SUB pode ser criada/atualizada
     */
    public void validateSub(ItemSprintDTO dto) {
        if (dto.getTitulo() == null || dto.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("O título da SUB é obrigatório.");
        }
        
        if (dto.getStatus() == null) {
            throw new IllegalArgumentException("O status da SUB é obrigatório.");
        }
        
        if (dto.getDuracaoDias() == null || dto.getDuracaoDias() <= 0) {
            throw new IllegalArgumentException("A duração da SUB deve ser maior que zero.");
        }
    }
    
    /**
     * Calcula o total de dias alocados em SUBs de uma Tarefa
     */
    public int calculateTotalSubsDuration(Long tarefaId) {
        return findSubsByTarefa(tarefaId).stream()
            .mapToInt(sub -> sub.getDuracaoDias() != null ? sub.getDuracaoDias() : 0)
            .sum();
    }
    
    /**
     * Verifica se uma Tarefa tem SUBs vinculadas
     */
    public boolean hasSubTasks(Long tarefaId) {
        return !findSubsByTarefa(tarefaId).isEmpty();
    }
    
    /**
     * Calcula a duração disponível para novas SUBs
     */
    public int calculateAvailableDurationForSubs(Long tarefaId) {
        ItemSprintDTO tarefa = findTarefaById(tarefaId);
        if (tarefa == null || tarefa.getDuracaoSemanas() == null) {
            return 0;
        }
        
        int duracaoTotalDias = tarefa.getDuracaoSemanas() * 5;
        int duracaoAlocadaDias = calculateTotalSubsDuration(tarefaId);
        
        return Math.max(0, duracaoTotalDias - duracaoAlocadaDias);
    }
}
