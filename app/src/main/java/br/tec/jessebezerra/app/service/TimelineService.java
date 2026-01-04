package br.tec.jessebezerra.app.service;

import br.tec.jessebezerra.app.dto.ItemSprintDTO;
import br.tec.jessebezerra.app.dto.SprintDTO;
import br.tec.jessebezerra.app.entity.TipoItem;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço especializado para gerenciar a Timeline da Sprint.
 * Encapsula regras de negócio para cálculo de dias úteis, organização hierárquica e visualização.
 */
public class TimelineService {
    
    private final ItemSprintService itemSprintService;
    
    public TimelineService() {
        this.itemSprintService = new ItemSprintService();
    }
    
    /**
     * Calcula os dias úteis (segunda a sexta) de uma sprint
     */
    public List<LocalDate> calculateWorkingDays(SprintDTO sprint) {
        List<LocalDate> workingDays = new ArrayList<>();
        LocalDate current = sprint.getDataInicio();
        LocalDate end = sprint.getDataFim();
        
        while (!current.isAfter(end)) {
            if (current.getDayOfWeek() != DayOfWeek.SATURDAY && 
                current.getDayOfWeek() != DayOfWeek.SUNDAY) {
                workingDays.add(current);
            }
            current = current.plusDays(1);
        }
        
        return workingDays;
    }
    
    /**
     * Organiza os itens da sprint em estrutura hierárquica
     */
    public List<TimelineItem> buildHierarchicalTimeline(Long sprintId) {
        List<TimelineItem> timelineItems = new ArrayList<>();
        List<ItemSprintDTO> allItems = itemSprintService.findBySprintId(sprintId);
        
        // Buscar Features (nível 0)
        List<ItemSprintDTO> features = allItems.stream()
            .filter(item -> item.getTipo() == TipoItem.FEATURE)
            .sorted(Comparator.comparing(ItemSprintDTO::getId))
            .collect(Collectors.toList());
        
        for (ItemSprintDTO feature : features) {
            timelineItems.add(new TimelineItem(feature, 0, "FEATURE"));
            
            // Buscar Histórias e Tarefas filhas (nível 1)
            List<ItemSprintDTO> children = allItems.stream()
                .filter(item -> feature.getId().equals(item.getItemPaiId()))
                .filter(item -> item.getTipo() == TipoItem.HISTORIA || item.getTipo() == TipoItem.TAREFA)
                .sorted(Comparator.comparing(ItemSprintDTO::getId))
                .collect(Collectors.toList());
            
            for (ItemSprintDTO child : children) {
                timelineItems.add(new TimelineItem(child, 1, child.getTipo().name()));
                
                // Buscar SUBs (nível 2) - ordenar por prioridade
                List<ItemSprintDTO> subs = allItems.stream()
                    .filter(item -> child.getId().equals(item.getItemPaiId()))
                    .filter(item -> item.getTipo() == TipoItem.SUB)
                    .sorted((a, b) -> {
                        // Ordenar por prioridade, se não houver prioridade usar ID
                        Integer prioA = a.getPrioridade() != null ? a.getPrioridade() : Integer.MAX_VALUE;
                        Integer prioB = b.getPrioridade() != null ? b.getPrioridade() : Integer.MAX_VALUE;
                        int cmp = prioA.compareTo(prioB);
                        if (cmp == 0) {
                            return a.getId().compareTo(b.getId());
                        }
                        return cmp;
                    })
                    .collect(Collectors.toList());
                
                for (ItemSprintDTO sub : subs) {
                    timelineItems.add(new TimelineItem(sub, 2, "SUB"));
                }
            }
        }
        
        // Adicionar itens órfãos (sem pai)
        List<ItemSprintDTO> orphans = allItems.stream()
            .filter(item -> item.getItemPaiId() == null && item.getTipo() != TipoItem.FEATURE)
            .sorted(Comparator.comparing(ItemSprintDTO::getId))
            .collect(Collectors.toList());
        
        for (ItemSprintDTO orphan : orphans) {
            timelineItems.add(new TimelineItem(orphan, 0, orphan.getTipo().name()));
        }
        
        return timelineItems;
    }
    
    /**
     * Calcula a duração em dias de um item
     */
    public int calculateDurationInDays(ItemSprintDTO item) {
        if (item.getDuracaoDias() != null && item.getDuracaoDias() > 0) {
            return item.getDuracaoDias();
        } else if (item.getDuracaoSemanas() != null && item.getDuracaoSemanas() > 0) {
            return item.getDuracaoSemanas() * 5; // 5 dias úteis por semana
        }
        return 0;
    }
    
    /**
     * Valida se um item pode ser alocado na timeline
     */
    public void validateItemAllocation(ItemSprintDTO item, SprintDTO sprint) {
        int itemDuration = calculateDurationInDays(item);
        List<LocalDate> workingDays = calculateWorkingDays(sprint);
        
        if (itemDuration > workingDays.size()) {
            throw new IllegalArgumentException(
                String.format("A duração do item (%d dias) excede os dias úteis da sprint (%d dias).",
                    itemDuration, workingDays.size()));
        }
    }
    
    /**
     * Calcula o total de dias alocados na sprint
     */
    public int calculateTotalAllocatedDays(Long sprintId) {
        List<ItemSprintDTO> items = itemSprintService.findBySprintId(sprintId);
        return items.stream()
            .filter(item -> item.getItemPaiId() == null) // Apenas itens raiz
            .mapToInt(this::calculateDurationInDays)
            .sum();
    }
    
    /**
     * Calcula o total de dias alocados para um membro específico em uma sprint
     * Considera apenas SUBs (tarefas atômicas)
     */
    public int calculateMemberAllocatedDays(Long sprintId, Long membroId) {
        if (membroId == null) {
            return 0;
        }
        
        List<ItemSprintDTO> items = itemSprintService.findBySprintId(sprintId);
        
        // Debug: listar todas as SUBs do membro
        List<ItemSprintDTO> subsDoMembro = items.stream()
            .filter(item -> item.getTipo() == TipoItem.SUB)
            .filter(item -> membroId.equals(item.getMembroId()))
            .collect(Collectors.toList());
        
        System.out.println("=== DEBUG: Calculando dias para membro ID " + membroId + " ===");
        int total = 0;
        for (ItemSprintDTO sub : subsDoMembro) {
            int dias = calculateDurationInDays(sub);
            System.out.println("SUB: " + sub.getTitulo() + " - Dias: " + dias + 
                " (duracaoDias=" + sub.getDuracaoDias() + ", duracaoSemanas=" + sub.getDuracaoSemanas() + ")");
            total += dias;
        }
        System.out.println("TOTAL: " + total + " dias");
        System.out.println("==============================================");
        
        return total;
    }
    
    /**
     * Calcula dias disponíveis na sprint
     */
    public int calculateAvailableDays(SprintDTO sprint) {
        List<LocalDate> workingDays = calculateWorkingDays(sprint);
        int totalDays = workingDays.size();
        int allocatedDays = calculateTotalAllocatedDays(sprint.getId());
        return Math.max(0, totalDays - allocatedDays);
    }
    
    /**
     * Verifica se a sprint está com sobrecarga
     */
    public boolean isSprintOverloaded(SprintDTO sprint) {
        return calculateAvailableDays(sprint) < 0;
    }
    
    /**
     * Calcula a posição inicial (dia) para um item baseado em alocações anteriores do mesmo membro
     */
    public int calculateStartDay(Long sprintId, Long membroId, Long currentItemId) {
        if (membroId == null) {
            return 0; // Sem membro, começa do dia 0
        }
        
        List<ItemSprintDTO> allItems = itemSprintService.findBySprintId(sprintId);
        
        // Filtrar itens do mesmo membro que vêm antes do item atual
        int lastEndDay = 0;
        
        for (ItemSprintDTO item : allItems) {
            // Pular o item atual
            if (item.getId().equals(currentItemId)) {
                continue;
            }
            
            // Verificar se é do mesmo membro
            if (item.getMembroId() != null && item.getMembroId().equals(membroId)) {
                int itemDuration = calculateDurationInDays(item);
                // Assumir que itens anteriores já foram alocados sequencialmente
                lastEndDay += itemDuration;
            }
        }
        
        return lastEndDay;
    }
    
    /**
     * Calcula o dia inicial de uma História ou Tarefa baseado na primeira SUB filha
     * Se não houver SUBs, retorna 0
     */
    public int calculateParentStartDay(Long parentId, List<ItemSprintDTO> allItems, Map<Long, Integer> memberAllocations) {
        // Buscar SUBs filhas ordenadas por ID
        List<ItemSprintDTO> childSubs = allItems.stream()
            .filter(item -> item.getTipo() == TipoItem.SUB)
            .filter(item -> parentId.equals(item.getItemPaiId()))
            .sorted(Comparator.comparing(ItemSprintDTO::getId))
            .collect(Collectors.toList());
        
        if (childSubs.isEmpty()) {
            return 0; // Sem SUBs, começa do dia 0
        }
        
        // Simular alocação das SUBs para encontrar o menor dia inicial
        Map<Long, Integer> tempAllocations = new HashMap<>(memberAllocations);
        int minStartDay = Integer.MAX_VALUE;
        
        for (ItemSprintDTO sub : childSubs) {
            if (sub.getMembroId() != null) {
                int subStartDay = tempAllocations.getOrDefault(sub.getMembroId(), 0);
                int subDuration = calculateDurationInDays(sub);
                
                // Atualizar alocação temporária
                tempAllocations.put(sub.getMembroId(), subStartDay + subDuration);
                
                // Rastrear menor dia inicial
                if (subStartDay < minStartDay) {
                    minStartDay = subStartDay;
                }
            }
        }
        
        return minStartDay == Integer.MAX_VALUE ? 0 : minStartDay;
    }
    
    /**
     * Calcula o dia final de uma História ou Tarefa baseado na última SUB filha
     * Retorna o número de dias que a História/Tarefa deve ocupar na timeline
     */
    public int calculateParentDuration(Long parentId, List<ItemSprintDTO> allItems, Map<Long, Integer> memberAllocations) {
        // Buscar SUBs filhas ordenadas por ID
        List<ItemSprintDTO> childSubs = allItems.stream()
            .filter(item -> item.getTipo() == TipoItem.SUB)
            .filter(item -> parentId.equals(item.getItemPaiId()))
            .sorted(Comparator.comparing(ItemSprintDTO::getId))
            .collect(Collectors.toList());
        
        if (childSubs.isEmpty()) {
            return 0; // Sem SUBs, duração 0
        }
        
        System.out.println("=== DEBUG calculateParentDuration para parent ID " + parentId + " ===");
        System.out.println("Total de SUBs filhas: " + childSubs.size());
        
        // Simular alocação das SUBs para calcular dia inicial e final
        Map<Long, Integer> tempAllocations = new HashMap<>(memberAllocations);
        
        int minStartDay = Integer.MAX_VALUE;
        int maxEndDay = 0;
        
        for (ItemSprintDTO sub : childSubs) {
            if (sub.getMembroId() != null) {
                // Calcular dia inicial desta SUB
                int subStartDay = tempAllocations.getOrDefault(sub.getMembroId(), 0);
                int subDuration = calculateDurationInDays(sub);
                int subEndDay = subStartDay + subDuration;
                
                System.out.println("  SUB: " + sub.getTitulo() + 
                    " | Membro: " + sub.getMembroNome() + 
                    " | StartDay: " + subStartDay + 
                    " | Duration: " + subDuration + 
                    " | EndDay: " + subEndDay);
                
                // Atualizar alocação temporária
                tempAllocations.put(sub.getMembroId(), subEndDay);
                
                // Rastrear menor dia inicial e maior dia final
                if (subStartDay < minStartDay) {
                    minStartDay = subStartDay;
                }
                if (subEndDay > maxEndDay) {
                    maxEndDay = subEndDay;
                }
            }
        }
        
        int duration = minStartDay == Integer.MAX_VALUE ? 0 : Math.max(0, maxEndDay - minStartDay);
        System.out.println("RESULTADO: minStartDay=" + minStartDay + ", maxEndDay=" + maxEndDay + ", duration=" + duration);
        System.out.println("========================================");
        
        return duration;
    }
    
    /**
     * Classe interna para representar um item na timeline com metadados
     */
    public static class TimelineItem {
        private final ItemSprintDTO item;
        private final int indentLevel;
        private final String displayType;
        private final int startDay;
        
        public TimelineItem(ItemSprintDTO item, int indentLevel, String displayType) {
            this(item, indentLevel, displayType, 0);
        }
        
        public TimelineItem(ItemSprintDTO item, int indentLevel, String displayType, int startDay) {
            this.item = item;
            this.indentLevel = indentLevel;
            this.displayType = displayType;
            this.startDay = startDay;
        }
        
        public ItemSprintDTO getItem() {
            return item;
        }
        
        public int getIndentLevel() {
            return indentLevel;
        }
        
        public String getDisplayType() {
            return displayType;
        }
        
        public int getStartDay() {
            return startDay;
        }
    }
}
