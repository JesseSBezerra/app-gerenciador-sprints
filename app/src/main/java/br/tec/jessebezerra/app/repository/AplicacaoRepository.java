package br.tec.jessebezerra.app.repository;

import br.tec.jessebezerra.app.config.DatabaseConfig;
import br.tec.jessebezerra.app.entity.Aplicacao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AplicacaoRepository {

    public Aplicacao save(Aplicacao aplicacao) {
        String sql = "INSERT INTO aplicacao (nome, descricao) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, aplicacao.getNome());
            pstmt.setString(2, aplicacao.getDescricao());
            
            pstmt.executeUpdate();
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    aplicacao.setId(rs.getLong(1));
                }
            }
            
            return aplicacao;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar aplicação", e);
        }
    }

    public Aplicacao update(Aplicacao aplicacao) {
        String sql = "UPDATE aplicacao SET nome = ?, descricao = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, aplicacao.getNome());
            pstmt.setString(2, aplicacao.getDescricao());
            pstmt.setLong(3, aplicacao.getId());
            
            pstmt.executeUpdate();
            return aplicacao;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar aplicação", e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM aplicacao WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir aplicação", e);
        }
    }

    public Optional<Aplicacao> findById(Long id) {
        String sql = "SELECT * FROM aplicacao WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAplicacao(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar aplicação", e);
        }
        
        return Optional.empty();
    }

    public List<Aplicacao> findAll() {
        String sql = "SELECT * FROM aplicacao ORDER BY nome";
        List<Aplicacao> aplicacoes = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                aplicacoes.add(mapResultSetToAplicacao(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar aplicações", e);
        }
        
        return aplicacoes;
    }

    private Aplicacao mapResultSetToAplicacao(ResultSet rs) throws SQLException {
        Aplicacao aplicacao = new Aplicacao();
        aplicacao.setId(rs.getLong("id"));
        aplicacao.setNome(rs.getString("nome"));
        aplicacao.setDescricao(rs.getString("descricao"));
        return aplicacao;
    }
}
