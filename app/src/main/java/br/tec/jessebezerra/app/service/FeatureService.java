package br.tec.jessebezerra.app.service;

import br.tec.jessebezerra.app.dto.ItemSprintDTO;
import br.tec.jessebezerra.app.entity.TipoItem;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço especializado para gerenciar Features e suas SUBs.
 * Encapsula regras de negócio específicas de Features.
 */
public class FeatureService {
    
    private final ItemSprintService itemSprintService;
    
    public FeatureService() {
        this.itemSprintService = new ItemSprintService();
    }
    
    /**
     * Cria uma nova Feature
     */
    public ItemSprintDTO createFeature(ItemSprintDTO dto) {
        dto.setTipo(TipoItem.FEATURE);
        return itemSprintService.create(dto);
    }
    
    /**
     * Atualiza uma Feature existente
     */
    public ItemSprintDTO updateFeature(ItemSprintDTO dto) {
        dto.setTipo(TipoItem.FEATURE);
        return itemSprintService.update(dto);
    }
    
    /**
     * Exclui uma Feature
     */
    public void deleteFeature(Long id) {
        itemSprintService.delete(id);
    }
    
    /**
     * Busca uma Feature por ID
     */
    public ItemSprintDTO findFeatureById(Long id) {
        return itemSprintService.findById(id)
            .filter(item -> item.getTipo() == TipoItem.FEATURE)
            .orElse(null);
    }
    
    /**
     * Lista todas as Features
     */
    public List<ItemSprintDTO> findAllFeatures() {
        return itemSprintService.findAll().stream()
            .filter(item -> item.getTipo() == TipoItem.FEATURE)
            .collect(Collectors.toList());
    }
    
    /**
     * Lista Features de uma Sprint específica
     */
    public List<ItemSprintDTO> findFeaturesBySprint(Long sprintId) {
        return itemSprintService.findBySprintId(sprintId).stream()
            .filter(item -> item.getTipo() == TipoItem.FEATURE)
            .collect(Collectors.toList());
    }
    
    /**
     * Lista todas as Histórias de uma Feature
     */
    public List<ItemSprintDTO> findHistoriasByFeature(Long featureId) {
        return itemSprintService.findByItemPaiId(featureId).stream()
            .filter(item -> item.getTipo() == TipoItem.HISTORIA)
            .collect(Collectors.toList());
    }
    
    /**
     * Lista todas as Tarefas de uma Feature
     */
    public List<ItemSprintDTO> findTarefasByFeature(Long featureId) {
        return itemSprintService.findByItemPaiId(featureId).stream()
            .filter(item -> item.getTipo() == TipoItem.TAREFA)
            .collect(Collectors.toList());
    }
    
    /**
     * Lista todas as SUBs de uma Feature
     */
    public List<ItemSprintDTO> findSubsByFeature(Long featureId) {
        return itemSprintService.findByItemPaiId(featureId).stream()
            .filter(item -> item.getTipo() == TipoItem.SUB)
            .collect(Collectors.toList());
    }
    
    /**
     * Lista todos os itens filhos de uma Feature (Histórias, Tarefas e SUBs)
     */
    public List<ItemSprintDTO> findAllChildrenByFeature(Long featureId) {
        return itemSprintService.findByItemPaiId(featureId);
    }
    
    /**
     * Cria uma SUB vinculada a uma Feature
     */
    public ItemSprintDTO createSub(ItemSprintDTO dto, Long featureId, Long sprintId) {
        dto.setTipo(TipoItem.SUB);
        dto.setItemPaiId(featureId);
        dto.setSprintId(sprintId);
        
        // Validar que a SUB não ultrapassa a duração da Feature
        ItemSprintDTO feature = findFeatureById(featureId);
        if (feature != null && feature.getDuracaoSemanas() != null) {
            int duracaoFeatureEmDias = feature.getDuracaoSemanas() * 5;
            if (dto.getDuracaoDias() != null && dto.getDuracaoDias() > duracaoFeatureEmDias) {
                throw new IllegalArgumentException(
                    String.format("A duração da SUB (%d dias) não pode ultrapassar a duração da Feature (%d dias).",
                        dto.getDuracaoDias(), duracaoFeatureEmDias));
            }
        }
        
        return itemSprintService.create(dto);
    }
    
    /**
     * Atualiza uma SUB
     */
    public ItemSprintDTO updateSub(ItemSprintDTO dto) {
        dto.setTipo(TipoItem.SUB);
        
        // Validar que a SUB não ultrapassa a duração da Feature
        if (dto.getItemPaiId() != null) {
            ItemSprintDTO feature = findFeatureById(dto.getItemPaiId());
            if (feature != null && feature.getDuracaoSemanas() != null) {
                int duracaoFeatureEmDias = feature.getDuracaoSemanas() * 5;
                if (dto.getDuracaoDias() != null && dto.getDuracaoDias() > duracaoFeatureEmDias) {
                    throw new IllegalArgumentException(
                        String.format("A duração da SUB (%d dias) não pode ultrapassar a duração da Feature (%d dias).",
                            dto.getDuracaoDias(), duracaoFeatureEmDias));
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
     * Valida se uma Feature pode ser criada/atualizada
     */
    public void validateFeature(ItemSprintDTO dto) {
        if (dto.getTitulo() == null || dto.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("O título da Feature é obrigatório.");
        }
        
        if (dto.getStatus() == null) {
            throw new IllegalArgumentException("O status da Feature é obrigatório.");
        }
        
        if (dto.getSprintId() == null) {
            throw new IllegalArgumentException("A Feature deve estar vinculada a uma Sprint.");
        }
        
        if (dto.getDuracaoSemanas() == null || dto.getDuracaoSemanas() <= 0) {
            throw new IllegalArgumentException("A duração da Feature deve ser maior que zero.");
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
     * Calcula o total de dias alocados em SUBs de uma Feature
     */
    public int calculateTotalSubsDuration(Long featureId) {
        return findSubsByFeature(featureId).stream()
            .mapToInt(sub -> sub.getDuracaoDias() != null ? sub.getDuracaoDias() : 0)
            .sum();
    }
    
    /**
     * Verifica se uma Feature tem itens filhos vinculados
     */
    public boolean hasChildren(Long featureId) {
        return !findAllChildrenByFeature(featureId).isEmpty();
    }
    
    /**
     * Calcula a duração disponível para novas SUBs
     */
    public int calculateAvailableDurationForSubs(Long featureId) {
        ItemSprintDTO feature = findFeatureById(featureId);
        if (feature == null || feature.getDuracaoSemanas() == null) {
            return 0;
        }
        
        int duracaoTotalDias = feature.getDuracaoSemanas() * 5;
        int duracaoAlocadaDias = calculateTotalSubsDuration(featureId);
        
        return Math.max(0, duracaoTotalDias - duracaoAlocadaDias);
    }
    
    /**
     * Calcula o total de semanas alocadas em Histórias e Tarefas de uma Feature
     */
    public int calculateTotalChildrenDuration(Long featureId) {
        List<ItemSprintDTO> historias = findHistoriasByFeature(featureId);
        List<ItemSprintDTO> tarefas = findTarefasByFeature(featureId);
        
        int totalSemanas = 0;
        
        for (ItemSprintDTO historia : historias) {
            if (historia.getDuracaoSemanas() != null) {
                totalSemanas += historia.getDuracaoSemanas();
            }
        }
        
        for (ItemSprintDTO tarefa : tarefas) {
            if (tarefa.getDuracaoSemanas() != null) {
                totalSemanas += tarefa.getDuracaoSemanas();
            }
        }
        
        return totalSemanas;
    }
}
