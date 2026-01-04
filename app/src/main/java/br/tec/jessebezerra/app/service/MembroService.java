package br.tec.jessebezerra.app.service;

import br.tec.jessebezerra.app.dto.MembroDTO;
import br.tec.jessebezerra.app.entity.Membro;
import br.tec.jessebezerra.app.repository.MembroRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MembroService {
    private final MembroRepository repository;

    public MembroService() {
        this.repository = new MembroRepository();
    }

    public MembroDTO create(MembroDTO dto) {
        Membro membro = toEntity(dto);
        Membro saved = repository.save(membro);
        return toDTO(saved);
    }

    public MembroDTO update(MembroDTO dto) {
        Membro membro = toEntity(dto);
        Membro updated = repository.update(membro);
        return toDTO(updated);
    }

    public void delete(Long id) {
        repository.delete(id);
    }

    public Optional<MembroDTO> findById(Long id) {
        return repository.findById(id).map(this::toDTO);
    }

    public List<MembroDTO> findAll() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<MembroDTO> findByAtivo(boolean ativo) {
        return repository.findByAtivo(ativo).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private Membro toEntity(MembroDTO dto) {
        Membro membro = new Membro();
        membro.setId(dto.getId());
        membro.setNome(dto.getNome());
        membro.setFuncao(dto.getFuncao());
        membro.setAtivo(dto.getAtivo());
        membro.setEspecialidades(dto.getEspecialidades());
        return membro;
    }

    private MembroDTO toDTO(Membro membro) {
        MembroDTO dto = new MembroDTO();
        dto.setId(membro.getId());
        dto.setNome(membro.getNome());
        dto.setFuncao(membro.getFuncao());
        dto.setAtivo(membro.getAtivo());
        dto.setEspecialidades(membro.getEspecialidades());
        return dto;
    }
}
