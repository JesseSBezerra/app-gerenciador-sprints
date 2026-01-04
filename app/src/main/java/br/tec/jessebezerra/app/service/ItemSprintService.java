package br.tec.jessebezerra.app.service;

import br.tec.jessebezerra.app.dto.ItemSprintDTO;
import br.tec.jessebezerra.app.dto.MembroDTO;
import br.tec.jessebezerra.app.dto.SprintDTO;
import br.tec.jessebezerra.app.entity.ItemSprint;
import br.tec.jessebezerra.app.repository.AplicacaoRepository;
import br.tec.jessebezerra.app.repository.ItemSprintRepository;
import br.tec.jessebezerra.app.repository.MembroRepository;
import br.tec.jessebezerra.app.repository.ProjetoRepository;
import br.tec.jessebezerra.app.repository.SprintRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemSprintService {
    private final ItemSprintRepository repository;
    private final SprintRepository sprintRepository;
    private final MembroRepository membroRepository;
    private final ProjetoRepository projetoRepository;
    private final AplicacaoRepository aplicacaoRepository;

    public ItemSprintService() {
        this.repository = new ItemSprintRepository();
        this.sprintRepository = new SprintRepository();
        this.membroRepository = new MembroRepository();
        this.projetoRepository = new ProjetoRepository();
        this.aplicacaoRepository = new AplicacaoRepository();
    }

    public ItemSprintDTO create(ItemSprintDTO dto) {
        validarItem(dto);
        
        ItemSprint item = toEntity(dto);
        ItemSprint saved = repository.save(item);
        return toDTO(saved);
    }

    public ItemSprintDTO update(ItemSprintDTO dto) {
        validarItem(dto);
        
        ItemSprint item = toEntity(dto);
        ItemSprint updated = repository.update(item);
        return toDTO(updated);
    }

    public void delete(Long id) {
        repository.delete(id);
    }

    public Optional<ItemSprintDTO> findById(Long id) {
        return repository.findById(id).map(this::toDTO);
    }

    public List<ItemSprintDTO> findAll() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ItemSprintDTO> findBySprintId(Long sprintId) {
        return repository.findBySprintId(sprintId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ItemSprintDTO> findByItemPaiId(Long itemPaiId) {
        return repository.findByItemPaiId(itemPaiId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private void validarItem(ItemSprintDTO dto) {
        if (dto.getTitulo() == null || dto.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("O título é obrigatório.");
        }
        if (dto.getSprintId() == null) {
            throw new IllegalArgumentException("A sprint é obrigatória.");
        }
        if (dto.getStatus() == null) {
            throw new IllegalArgumentException("O status é obrigatório.");
        }
        
        validarRegrasNegocio(dto);
    }

    private void validarRegrasNegocio(ItemSprintDTO dto) {
        // Validar duração não ultrapassa a sprint
        validarDuracaoSprint(dto);
        
        // Validar hierarquia de tipos
        if (dto.getItemPaiId() != null) {
            validarHierarquiaTipos(dto);
        }
        
        // Validar alocação de dias por membro (apenas para SUBs)
        validarAlocacaoMembroSprint(dto);
    }

    private void validarDuracaoSprint(ItemSprintDTO dto) {
        Optional<br.tec.jessebezerra.app.entity.Sprint> sprintOpt = sprintRepository.findById(dto.getSprintId());
        
        if (sprintOpt.isPresent()) {
            br.tec.jessebezerra.app.entity.Sprint sprint = sprintOpt.get();
            
            if (sprint.getDataInicio() != null && sprint.getDataFim() != null) {
                long diasUteis = contarDiasUteis(sprint.getDataInicio(), sprint.getDataFim());
                
                // Validar baseado no tipo de duração
                if (dto.getDuracaoSemanas() != null) {
                    // Para Feature, História, Tarefa (duração em semanas)
                    long semanasDisponiveis = diasUteis / 5; // 5 dias úteis por semana
                    if (dto.getDuracaoSemanas() > semanasDisponiveis) {
                        throw new IllegalArgumentException(
                            String.format("A duração do item (%d semanas) não pode ultrapassar a duração da sprint (%d semanas).",
                                dto.getDuracaoSemanas(), semanasDisponiveis)
                        );
                    }
                } else if (dto.getDuracaoDias() != null) {
                    // Para SUB (duração em dias)
                    if (dto.getDuracaoDias() > diasUteis) {
                        throw new IllegalArgumentException(
                            String.format("A duração do item (%d dias) não pode ultrapassar a duração da sprint (%d dias úteis).",
                                dto.getDuracaoDias(), diasUteis)
                        );
                    }
                }
            }
        }
    }

    private void validarDuracaoItemPai(ItemSprintDTO dto) {
        Optional<ItemSprint> itemPaiOpt = repository.findById(dto.getItemPaiId());
        
        if (itemPaiOpt.isPresent()) {
            ItemSprint itemPai = itemPaiOpt.get();
            
            // Validar baseado no tipo (semanas ou dias)
            if (dto.getDuracaoSemanas() != null && itemPai.getDuracaoSemanas() != null) {
                // Validação em semanas (História/Tarefa filhas de Feature)
                // Primeiro: verificar se o item sozinho não ultrapassa o pai
                if (dto.getDuracaoSemanas() > itemPai.getDuracaoSemanas()) {
                    throw new IllegalArgumentException(
                        String.format("A duração do item (%d semanas) não pode ultrapassar a duração do item pai (%d semanas).",
                            dto.getDuracaoSemanas(), itemPai.getDuracaoSemanas())
                    );
                }
                
                // Segundo: verificar se a soma de todos os filhos não ultrapassa o pai
                Integer somaDuracaoFilhos = repository.getSomaDuracaoFilhosSemanas(dto.getItemPaiId());
                
                if (dto.getId() != null) {
                    // Se for update, subtrair a duração antiga
                    Optional<ItemSprint> itemAtualOpt = repository.findById(dto.getId());
                    if (itemAtualOpt.isPresent() && itemAtualOpt.get().getDuracaoSemanas() != null) {
                        somaDuracaoFilhos -= itemAtualOpt.get().getDuracaoSemanas();
                    }
                }
                
                int novaDuracaoTotal = somaDuracaoFilhos + dto.getDuracaoSemanas();
                
                if (novaDuracaoTotal > itemPai.getDuracaoSemanas()) {
                    throw new IllegalArgumentException(
                        String.format("A duração total dos subitens (%d semanas) não pode ultrapassar a duração do item pai (%d semanas).",
                            novaDuracaoTotal, itemPai.getDuracaoSemanas())
                    );
                }
            } else if (dto.getDuracaoDias() != null) {
                // Validação em dias (SUB filha de História/Tarefa)
                // Converter semanas do pai para dias (5 dias úteis por semana)
                int duracaoPaiEmDias = itemPai.getDuracaoSemanas() != null ? itemPai.getDuracaoSemanas() * 5 : 0;
                
                // Primeiro: verificar se o item sozinho não ultrapassa o pai
                if (dto.getDuracaoDias() > duracaoPaiEmDias) {
                    throw new IllegalArgumentException(
                        String.format("A duração do item (%d dias) não pode ultrapassar a duração do item pai (%d dias).",
                            dto.getDuracaoDias(), duracaoPaiEmDias)
                    );
                }
                
                // Segundo: verificar se a soma de todos os filhos não ultrapassa o pai
                Integer somaDuracaoFilhos = repository.getSomaDuracaoFilhosDias(dto.getItemPaiId());
                
                if (dto.getId() != null) {
                    // Se for update, subtrair a duração antiga
                    Optional<ItemSprint> itemAtualOpt = repository.findById(dto.getId());
                    if (itemAtualOpt.isPresent() && itemAtualOpt.get().getDuracaoDias() != null) {
                        somaDuracaoFilhos -= itemAtualOpt.get().getDuracaoDias();
                    }
                }
                
                int novaDuracaoTotal = somaDuracaoFilhos + dto.getDuracaoDias();
                
                if (novaDuracaoTotal > duracaoPaiEmDias) {
                    throw new IllegalArgumentException(
                        String.format("A duração total dos subitens (%d dias) não pode ultrapassar a duração do item pai (%d dias).",
                            novaDuracaoTotal, duracaoPaiEmDias)
                    );
                }
            }
        }
    }

    private void validarHierarquiaTipos(ItemSprintDTO dto) {
        Optional<ItemSprint> itemPaiOpt = repository.findById(dto.getItemPaiId());
        
        if (itemPaiOpt.isPresent()) {
            ItemSprint itemPai = itemPaiOpt.get();
            
            if (!dto.getTipo().podeSerFilhoDe(itemPai.getTipo())) {
                throw new IllegalArgumentException(
                    String.format("Um item do tipo '%s' não pode ser filho de um item do tipo '%s'.",
                        dto.getTipo().getDescricao(), itemPai.getTipo().getDescricao())
                );
            }
        }
    }
    
    private void validarAlocacaoMembroSprint(ItemSprintDTO dto) {
        // Validar apenas para SUBs com membro atribuído
        if (dto.getTipo() != br.tec.jessebezerra.app.entity.TipoItem.SUB || dto.getMembroId() == null) {
            return;
        }
        
        // Calcular dias já alocados para o membro na sprint
        TimelineService timelineService = new TimelineService();
        int diasAlocados = timelineService.calculateMemberAllocatedDays(dto.getSprintId(), dto.getMembroId());
        
        // Subtrair dias do item atual se for uma atualização
        if (dto.getId() != null) {
            Optional<ItemSprint> itemAtualOpt = repository.findById(dto.getId());
            if (itemAtualOpt.isPresent()) {
                ItemSprint itemAtual = itemAtualOpt.get();
                if (itemAtual.getDuracaoDias() != null) {
                    diasAlocados -= itemAtual.getDuracaoDias();
                }
            }
        }
        
        // Adicionar dias do novo item
        int novosDias = dto.getDuracaoDias() != null ? dto.getDuracaoDias() : 0;
        int totalDias = diasAlocados + novosDias;
        
        // Obter total de dias úteis da sprint
        Optional<br.tec.jessebezerra.app.entity.Sprint> sprintOpt = sprintRepository.findById(dto.getSprintId());
        if (sprintOpt.isPresent()) {
            br.tec.jessebezerra.app.entity.Sprint sprint = sprintOpt.get();
            if (sprint.getDataInicio() != null && sprint.getDataFim() != null) {
                long diasUteisSprint = contarDiasUteis(sprint.getDataInicio(), sprint.getDataFim());
                
                if (totalDias > diasUteisSprint) {
                    // Buscar nome do membro
                    String nomeMembro = "Membro";
                    if (dto.getMembroId() != null) {
                        Optional<br.tec.jessebezerra.app.entity.Membro> membroOpt = membroRepository.findById(dto.getMembroId());
                        if (membroOpt.isPresent()) {
                            nomeMembro = membroOpt.get().getNome();
                        }
                    }
                    
                    throw new IllegalArgumentException(
                        String.format("O membro '%s' já possui %d dias alocados. " +
                            "Adicionar mais %d dias ultrapassaria o limite de %d dias úteis da sprint.",
                            nomeMembro, diasAlocados, novosDias, diasUteisSprint)
                    );
                }
            }
        }
    }

    private long contarDiasUteis(LocalDate inicio, LocalDate fim) {
        long diasUteis = 0;
        LocalDate data = inicio;
        
        while (!data.isAfter(fim)) {
            if (data.getDayOfWeek().getValue() <= 5) { // 1=Monday, 5=Friday
                diasUteis++;
            }
            data = data.plusDays(1);
        }
        
        return diasUteis;
    }

    private ItemSprint toEntity(ItemSprintDTO dto) {
        ItemSprint item = new ItemSprint();
        item.setId(dto.getId());
        item.setTipo(dto.getTipo());
        item.setTitulo(dto.getTitulo());
        item.setDescricao(dto.getDescricao());
        item.setDuracaoSemanas(dto.getDuracaoSemanas());
        item.setDuracaoDias(dto.getDuracaoDias());
        item.setStatus(dto.getStatus());
        item.setSprintId(dto.getSprintId());
        item.setMembroId(dto.getMembroId());
        item.setItemPaiId(dto.getItemPaiId());
        item.setProjetoId(dto.getProjetoId());
        item.setAplicacaoId(dto.getAplicacaoId());
        return item;
    }

    private ItemSprintDTO toDTO(ItemSprint item) {
        ItemSprintDTO dto = new ItemSprintDTO();
        dto.setId(item.getId());
        dto.setTipo(item.getTipo());
        dto.setTitulo(item.getTitulo());
        dto.setDescricao(item.getDescricao());
        dto.setDuracaoSemanas(item.getDuracaoSemanas());
        dto.setDuracaoDias(item.getDuracaoDias());
        dto.setStatus(item.getStatus());
        dto.setSprintId(item.getSprintId());
        dto.setMembroId(item.getMembroId());
        dto.setItemPaiId(item.getItemPaiId());
        dto.setProjetoId(item.getProjetoId());
        dto.setAplicacaoId(item.getAplicacaoId());
        
        // Buscar nomes para exibição
        if (item.getSprintId() != null) {
            sprintRepository.findById(item.getSprintId())
                .ifPresent(sprint -> dto.setSprintNome(sprint.getNome()));
        }
        
        if (item.getMembroId() != null) {
            membroRepository.findById(item.getMembroId())
                .ifPresent(membro -> dto.setMembroNome(membro.getNome()));
        }
        
        if (item.getItemPaiId() != null) {
            repository.findById(item.getItemPaiId())
                .ifPresent(itemPai -> dto.setItemPaiTitulo(itemPai.getTitulo()));
        }
        
        if (item.getProjetoId() != null) {
            projetoRepository.findById(item.getProjetoId())
                .ifPresent(projeto -> dto.setProjetoNome(projeto.getNome()));
        }
        
        if (item.getAplicacaoId() != null) {
            aplicacaoRepository.findById(item.getAplicacaoId())
                .ifPresent(aplicacao -> dto.setAplicacaoNome(aplicacao.getNome()));
        }
        
        return dto;
    }
}
