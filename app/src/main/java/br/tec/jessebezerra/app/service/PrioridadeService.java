package br.tec.jessebezerra.app.service;

import br.tec.jessebezerra.app.dto.ItemSprintDTO;
import br.tec.jessebezerra.app.entity.TipoItem;
import br.tec.jessebezerra.app.repository.ItemSprintRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço para gerenciar prioridades de SUBs
 */
public class PrioridadeService {
    
    private final ItemSprintRepository itemSprintRepository;
    private final ItemSprintService itemSprintService;
    
    public PrioridadeService() {
        this.itemSprintRepository = new ItemSprintRepository();
        this.itemSprintService = new ItemSprintService();
    }
    
    /**
     * Reordena SUBs de um item pai (História ou Tarefa)
     * Move uma SUB de uma posição para outra e recalcula as prioridades
     * 
     * @param subId ID da SUB a ser movida
     * @param newPosition Nova posição (0-based)
     */
    public void reorderSub(Long subId, int newPosition) {
        // Buscar a SUB
        ItemSprintDTO sub = itemSprintService.findById(subId)
            .orElseThrow(() -> new RuntimeException("SUB não encontrada"));
        
        if (sub.getTipo() != TipoItem.SUB) {
            throw new RuntimeException("Item não é uma SUB");
        }
        
        if (sub.getItemPaiId() == null) {
            throw new RuntimeException("SUB não possui item pai");
        }
        
        // Buscar todas as SUBs do mesmo pai
        List<ItemSprintDTO> subs = itemSprintService.findByItemPaiId(sub.getItemPaiId())
            .stream()
            .filter(item -> item.getTipo() == TipoItem.SUB)
            .sorted((a, b) -> {
                // Ordenar por prioridade existente, ou por ID se não houver prioridade
                Integer prioA = a.getPrioridade() != null ? a.getPrioridade() : Integer.MAX_VALUE;
                Integer prioB = b.getPrioridade() != null ? b.getPrioridade() : Integer.MAX_VALUE;
                int cmp = prioA.compareTo(prioB);
                if (cmp == 0) {
                    return a.getId().compareTo(b.getId());
                }
                return cmp;
            })
            .collect(Collectors.toList());
        
        // Encontrar posição atual da SUB
        int currentPosition = -1;
        for (int i = 0; i < subs.size(); i++) {
            if (subs.get(i).getId().equals(subId)) {
                currentPosition = i;
                break;
            }
        }
        
        if (currentPosition == -1) {
            throw new RuntimeException("SUB não encontrada na lista de SUBs do pai");
        }
        
        // Validar nova posição
        if (newPosition < 0 || newPosition >= subs.size()) {
            throw new RuntimeException("Posição inválida");
        }
        
        // Se a posição não mudou, não fazer nada
        if (currentPosition == newPosition) {
            return;
        }
        
        // Remover SUB da posição atual
        ItemSprintDTO movedSub = subs.remove(currentPosition);
        
        // Inserir na nova posição
        subs.add(newPosition, movedSub);
        
        // Recalcular prioridades (0, 1, 2, 3, ...)
        for (int i = 0; i < subs.size(); i++) {
            ItemSprintDTO s = subs.get(i);
            s.setPrioridade(i);
            itemSprintService.update(s);
        }
    }
    
    /**
     * Inicializa prioridades para SUBs que não possuem
     * Ordena por ID e atribui prioridades sequenciais
     * 
     * @param itemPaiId ID do item pai (História ou Tarefa)
     */
    public void initializePriorities(Long itemPaiId) {
        List<ItemSprintDTO> subs = itemSprintService.findByItemPaiId(itemPaiId)
            .stream()
            .filter(item -> item.getTipo() == TipoItem.SUB)
            .sorted((a, b) -> a.getId().compareTo(b.getId()))
            .collect(Collectors.toList());
        
        for (int i = 0; i < subs.size(); i++) {
            ItemSprintDTO sub = subs.get(i);
            if (sub.getPrioridade() == null) {
                sub.setPrioridade(i);
                itemSprintService.update(sub);
            }
        }
    }
    
    /**
     * Obtém SUBs ordenadas por prioridade
     * 
     * @param itemPaiId ID do item pai
     * @return Lista de SUBs ordenadas por prioridade
     */
    public List<ItemSprintDTO> getOrderedSubs(Long itemPaiId) {
        return itemSprintService.findByItemPaiId(itemPaiId)
            .stream()
            .filter(item -> item.getTipo() == TipoItem.SUB)
            .sorted((a, b) -> {
                Integer prioA = a.getPrioridade() != null ? a.getPrioridade() : Integer.MAX_VALUE;
                Integer prioB = b.getPrioridade() != null ? b.getPrioridade() : Integer.MAX_VALUE;
                int cmp = prioA.compareTo(prioB);
                if (cmp == 0) {
                    return a.getId().compareTo(b.getId());
                }
                return cmp;
            })
            .collect(Collectors.toList());
    }
}
