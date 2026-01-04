package br.tec.jessebezerra.app.service;

import br.tec.jessebezerra.app.dto.AplicacaoDTO;
import br.tec.jessebezerra.app.entity.Aplicacao;
import br.tec.jessebezerra.app.repository.AplicacaoRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AplicacaoService {
    private final AplicacaoRepository repository;

    public AplicacaoService() {
        this.repository = new AplicacaoRepository();
    }

    public AplicacaoDTO create(AplicacaoDTO dto) {
        validarAplicacao(dto);
        
        Aplicacao aplicacao = new Aplicacao();
        aplicacao.setNome(dto.getNome());
        aplicacao.setDescricao(dto.getDescricao());
        
        Aplicacao saved = repository.save(aplicacao);
        return toDTO(saved);
    }

    public AplicacaoDTO update(AplicacaoDTO dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("ID da aplicação não pode ser nulo");
        }
        
        validarAplicacao(dto);
        
        Aplicacao aplicacao = new Aplicacao();
        aplicacao.setId(dto.getId());
        aplicacao.setNome(dto.getNome());
        aplicacao.setDescricao(dto.getDescricao());
        
        Aplicacao updated = repository.update(aplicacao);
        return toDTO(updated);
    }

    public void delete(Long id) {
        repository.delete(id);
    }

    public Optional<AplicacaoDTO> findById(Long id) {
        return repository.findById(id).map(this::toDTO);
    }

    public List<AplicacaoDTO> findAll() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private void validarAplicacao(AplicacaoDTO dto) {
        if (dto.getNome() == null || dto.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da aplicação é obrigatório");
        }
    }

    private AplicacaoDTO toDTO(Aplicacao aplicacao) {
        AplicacaoDTO dto = new AplicacaoDTO();
        dto.setId(aplicacao.getId());
        dto.setNome(aplicacao.getNome());
        dto.setDescricao(aplicacao.getDescricao());
        return dto;
    }
}
