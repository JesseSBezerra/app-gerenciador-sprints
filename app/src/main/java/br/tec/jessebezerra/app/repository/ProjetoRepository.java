package br.tec.jessebezerra.app.repository;

import br.tec.jessebezerra.app.config.DatabaseConfig;
import br.tec.jessebezerra.app.entity.Projeto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjetoRepository {

    public Projeto save(Projeto projeto) {
        String sql = "INSERT INTO projeto (nome, descricao) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, projeto.getNome());
            pstmt.setString(2, projeto.getDescricao());
            
            pstmt.executeUpdate();
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    projeto.setId(rs.getLong(1));
                }
            }
            
            return projeto;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar projeto", e);
        }
    }

    public Projeto update(Projeto projeto) {
        String sql = "UPDATE projeto SET nome = ?, descricao = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, projeto.getNome());
            pstmt.setString(2, projeto.getDescricao());
            pstmt.setLong(3, projeto.getId());
            
            pstmt.executeUpdate();
            return projeto;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar projeto", e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM projeto WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir projeto", e);
        }
    }

    public Optional<Projeto> findById(Long id) {
        String sql = "SELECT * FROM projeto WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProjeto(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar projeto", e);
        }
        
        return Optional.empty();
    }

    public List<Projeto> findAll() {
        String sql = "SELECT * FROM projeto ORDER BY nome";
        List<Projeto> projetos = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                projetos.add(mapResultSetToProjeto(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar projetos", e);
        }
        
        return projetos;
    }

    private Projeto mapResultSetToProjeto(ResultSet rs) throws SQLException {
        Projeto projeto = new Projeto();
        projeto.setId(rs.getLong("id"));
        projeto.setNome(rs.getString("nome"));
        projeto.setDescricao(rs.getString("descricao"));
        return projeto;
    }
}
