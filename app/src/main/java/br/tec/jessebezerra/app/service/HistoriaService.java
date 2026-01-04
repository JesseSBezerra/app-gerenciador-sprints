package br.tec.jessebezerra.app.service;

import br.tec.jessebezerra.app.dto.ItemSprintDTO;
import br.tec.jessebezerra.app.entity.TipoItem;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço especializado para gerenciar Histórias e suas SUBs.
 * Encapsula regras de negócio específicas de Histórias.
 */
public class HistoriaService {
    
    private final ItemSprintService itemSprintService;
    
    public HistoriaService() {
        this.itemSprintService = new ItemSprintService();
    }
    
    /**
     * Cria uma nova História
     */
    public ItemSprintDTO createHistoria(ItemSprintDTO dto) {
        dto.setTipo(TipoItem.HISTORIA);
        return itemSprintService.create(dto);
    }
    
    /**
     * Atualiza uma História existente
     */
    public ItemSprintDTO updateHistoria(ItemSprintDTO dto) {
        dto.setTipo(TipoItem.HISTORIA);
        return itemSprintService.update(dto);
    }
    
    /**
     * Exclui uma História
     */
    public void deleteHistoria(Long id) {
        itemSprintService.delete(id);
    }
    
    /**
     * Busca uma História por ID
     */
    public ItemSprintDTO findHistoriaById(Long id) {
        return itemSprintService.findById(id)
            .filter(item -> item.getTipo() == TipoItem.HISTORIA)
            .orElse(null);
    }
    
    /**
     * Lista todas as Histórias
     */
    public List<ItemSprintDTO> findAllHistorias() {
        return itemSprintService.findAll().stream()
            .filter(item -> item.getTipo() == TipoItem.HISTORIA)
            .collect(Collectors.toList());
    }
    
    /**
     * Lista Histórias de uma Sprint específica
     */
    public List<ItemSprintDTO> findHistoriasBySprint(Long sprintId) {
        return itemSprintService.findBySprintId(sprintId).stream()
            .filter(item -> item.getTipo() == TipoItem.HISTORIA)
            .collect(Collectors.toList());
    }
    
    /**
     * Lista Histórias de uma Feature específica
     */
    public List<ItemSprintDTO> findHistoriasByFeature(Long featureId) {
        return itemSprintService.findByItemPaiId(featureId).stream()
            .filter(item -> item.getTipo() == TipoItem.HISTORIA)
            .collect(Collectors.toList());
    }
    
    /**
     * Lista todas as SUBs de uma História
     */
    public List<ItemSprintDTO> findSubsByHistoria(Long historiaId) {
        return itemSprintService.findByItemPaiId(historiaId).stream()
            .filter(item -> item.getTipo() == TipoItem.SUB)
            .collect(Collectors.toList());
    }
    
    /**
     * Cria uma SUB vinculada a uma História
     */
    public ItemSprintDTO createSub(ItemSprintDTO dto, Long historiaId, Long sprintId) {
        dto.setTipo(TipoItem.SUB);
        dto.setItemPaiId(historiaId);
        dto.setSprintId(sprintId);
        
        // Validar que a SUB não ultrapassa a duração da História
        ItemSprintDTO historia = findHistoriaById(historiaId);
        if (historia != null && historia.getDuracaoSemanas() != null) {
            int duracaoHistoriaEmDias = historia.getDuracaoSemanas() * 5;
            if (dto.getDuracaoDias() != null && dto.getDuracaoDias() > duracaoHistoriaEmDias) {
                throw new IllegalArgumentException(
                    String.format("A duração da SUB (%d dias) não pode ultrapassar a duração da História (%d dias).",
                        dto.getDuracaoDias(), duracaoHistoriaEmDias));
            }
        }
        
        return itemSprintService.create(dto);
    }
    
    /**
     * Atualiza uma SUB
     */
    public ItemSprintDTO updateSub(ItemSprintDTO dto) {
        dto.setTipo(TipoItem.SUB);
        
        // Validar que a SUB não ultrapassa a duração da História
        if (dto.getItemPaiId() != null) {
            ItemSprintDTO historia = findHistoriaById(dto.getItemPaiId());
            if (historia != null && historia.getDuracaoSemanas() != null) {
                int duracaoHistoriaEmDias = historia.getDuracaoSemanas() * 5;
                if (dto.getDuracaoDias() != null && dto.getDuracaoDias() > duracaoHistoriaEmDias) {
                    throw new IllegalArgumentException(
                        String.format("A duração da SUB (%d dias) não pode ultrapassar a duração da História (%d dias).",
                            dto.getDuracaoDias(), duracaoHistoriaEmDias));
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
     * Valida se uma História pode ser criada/atualizada
     */
    public void validateHistoria(ItemSprintDTO dto) {
        if (dto.getTitulo() == null || dto.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("O título da História é obrigatório.");
        }
        
        if (dto.getStatus() == null) {
            throw new IllegalArgumentException("O status da História é obrigatório.");
        }
        
        if (dto.getSprintId() == null) {
            throw new IllegalArgumentException("A História deve estar vinculada a uma Sprint.");
        }
        
        if (dto.getDuracaoSemanas() == null || dto.getDuracaoSemanas() <= 0) {
            throw new IllegalArgumentException("A duração da História deve ser maior que zero.");
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
     * Calcula o total de dias alocados em SUBs de uma História
     */
    public int calculateTotalSubsDuration(Long historiaId) {
        return findSubsByHistoria(historiaId).stream()
            .mapToInt(sub -> sub.getDuracaoDias() != null ? sub.getDuracaoDias() : 0)
            .sum();
    }
    
    /**
     * Verifica se uma História tem SUBs vinculadas
     */
    public boolean hasSubTasks(Long historiaId) {
        return !findSubsByHistoria(historiaId).isEmpty();
    }
    
    /**
     * Calcula a duração disponível para novas SUBs
     */
    public int calculateAvailableDurationForSubs(Long historiaId) {
        ItemSprintDTO historia = findHistoriaById(historiaId);
        if (historia == null || historia.getDuracaoSemanas() == null) {
            return 0;
        }
        
        int duracaoTotalDias = historia.getDuracaoSemanas() * 5;
        int duracaoAlocadaDias = calculateTotalSubsDuration(historiaId);
        
        return Math.max(0, duracaoTotalDias - duracaoAlocadaDias);
    }
}
