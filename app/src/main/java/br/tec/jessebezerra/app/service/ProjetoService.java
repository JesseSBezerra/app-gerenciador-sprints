package br.tec.jessebezerra.app.service;

import br.tec.jessebezerra.app.dto.ProjetoDTO;
import br.tec.jessebezerra.app.entity.Projeto;
import br.tec.jessebezerra.app.repository.ProjetoRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProjetoService {
    private final ProjetoRepository repository;

    public ProjetoService() {
        this.repository = new ProjetoRepository();
    }

    public ProjetoDTO create(ProjetoDTO dto) {
        validarProjeto(dto);
        
        Projeto projeto = new Projeto();
        projeto.setNome(dto.getNome());
        projeto.setDescricao(dto.getDescricao());
        
        Projeto saved = repository.save(projeto);
        return toDTO(saved);
    }

    public ProjetoDTO update(ProjetoDTO dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("ID do projeto não pode ser nulo");
        }
        
        validarProjeto(dto);
        
        Projeto projeto = new Projeto();
        projeto.setId(dto.getId());
        projeto.setNome(dto.getNome());
        projeto.setDescricao(dto.getDescricao());
        
        Projeto updated = repository.update(projeto);
        return toDTO(updated);
    }

    public void delete(Long id) {
        repository.delete(id);
    }

    public Optional<ProjetoDTO> findById(Long id) {
        return repository.findById(id).map(this::toDTO);
    }

    public List<ProjetoDTO> findAll() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private void validarProjeto(ProjetoDTO dto) {
        if (dto.getNome() == null || dto.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do projeto é obrigatório");
        }
    }

    private ProjetoDTO toDTO(Projeto projeto) {
        ProjetoDTO dto = new ProjetoDTO();
        dto.setId(projeto.getId());
        dto.setNome(projeto.getNome());
        dto.setDescricao(projeto.getDescricao());
        return dto;
    }
}
