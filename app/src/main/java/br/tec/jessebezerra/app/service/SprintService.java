package br.tec.jessebezerra.app.service;

import br.tec.jessebezerra.app.dto.SprintDTO;
import br.tec.jessebezerra.app.entity.Sprint;
import br.tec.jessebezerra.app.repository.SprintRepository;
import br.tec.jessebezerra.app.util.DateUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SprintService {
    private final SprintRepository repository;

    public SprintService() {
        this.repository = new SprintRepository();
    }

    public SprintDTO create(SprintDTO dto) {
        Sprint sprint = toEntity(dto);
        
        // Validar sobreposição de períodos
        if (sprint.getDataFim() != null && 
            repository.existsOverlappingSprint(sprint.getDataInicio(), sprint.getDataFim(), null)) {
            throw new IllegalArgumentException("Já existe uma sprint cadastrada neste período. " +
                    "Não é possível cadastrar sprints com períodos sobrepostos.");
        }
        
        Sprint saved = repository.save(sprint);
        return toDTO(saved);
    }

    public SprintDTO update(SprintDTO dto) {
        Sprint sprint = toEntity(dto);
        
        // Validar sobreposição de períodos (excluindo a própria sprint)
        if (sprint.getDataFim() != null && 
            repository.existsOverlappingSprint(sprint.getDataInicio(), sprint.getDataFim(), sprint.getId())) {
            throw new IllegalArgumentException("Já existe uma sprint cadastrada neste período. " +
                    "Não é possível cadastrar sprints com períodos sobrepostos.");
        }
        
        Sprint updated = repository.update(sprint);
        return toDTO(updated);
    }

    public void delete(Long id) {
        repository.delete(id);
    }

    public Optional<SprintDTO> findById(Long id) {
        return repository.findById(id).map(this::toDTO);
    }

    public List<SprintDTO> findAll() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private Sprint toEntity(SprintDTO dto) {
        Sprint sprint = new Sprint();
        sprint.setId(dto.getId());
        sprint.setNome(dto.getNome());
        sprint.setDataInicio(dto.getDataInicio());
        sprint.setDuracaoSemanas(dto.getDuracaoSemanas());
        sprint.setDataFim(DateUtils.calcularDataFim(dto.getDataInicio(), dto.getDuracaoSemanas()));
        return sprint;
    }

    private SprintDTO toDTO(Sprint sprint) {
        SprintDTO dto = new SprintDTO();
        dto.setId(sprint.getId());
        dto.setNome(sprint.getNome());
        dto.setDataInicio(sprint.getDataInicio());
        dto.setDuracaoSemanas(sprint.getDuracaoSemanas());
        dto.setDataFim(sprint.getDataFim());
        return dto;
    }
}
