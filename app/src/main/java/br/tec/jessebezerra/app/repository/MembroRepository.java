package br.tec.jessebezerra.app.repository;

import br.tec.jessebezerra.app.config.DatabaseConfig;
import br.tec.jessebezerra.app.entity.Funcao;
import br.tec.jessebezerra.app.entity.Membro;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MembroRepository {

    public Membro save(Membro membro) {
        String sql = "INSERT INTO membro (nome, funcao, ativo, especialidades) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, membro.getNome());
            pstmt.setString(2, membro.getFuncao().name());
            pstmt.setInt(3, membro.getAtivo() ? 1 : 0);
            pstmt.setString(4, membro.getEspecialidades());
            
            pstmt.executeUpdate();
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    membro.setId(rs.getLong(1));
                }
            }
            
            return membro;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar membro", e);
        }
    }

    public Membro update(Membro membro) {
        String sql = "UPDATE membro SET nome = ?, funcao = ?, ativo = ?, especialidades = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, membro.getNome());
            pstmt.setString(2, membro.getFuncao().name());
            pstmt.setInt(3, membro.getAtivo() ? 1 : 0);
            pstmt.setString(4, membro.getEspecialidades());
            pstmt.setLong(5, membro.getId());
            
            pstmt.executeUpdate();
            return membro;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar membro", e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM membro WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir membro", e);
        }
    }

    public Optional<Membro> findById(Long id) {
        String sql = "SELECT * FROM membro WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMembro(rs));
                }
            }
            
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar membro", e);
        }
    }

    public List<Membro> findAll() {
        String sql = "SELECT * FROM membro ORDER BY nome ASC";
        List<Membro> membros = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                membros.add(mapResultSetToMembro(rs));
            }
            
            return membros;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar membros", e);
        }
    }

    public List<Membro> findByAtivo(boolean ativo) {
        String sql = "SELECT * FROM membro WHERE ativo = ? ORDER BY nome ASC";
        List<Membro> membros = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, ativo ? 1 : 0);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    membros.add(mapResultSetToMembro(rs));
                }
            }
            
            return membros;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar membros ativos", e);
        }
    }

    private Membro mapResultSetToMembro(ResultSet rs) throws SQLException {
        Membro membro = new Membro();
        membro.setId(rs.getLong("id"));
        membro.setNome(rs.getString("nome"));
        membro.setFuncao(Funcao.valueOf(rs.getString("funcao")));
        membro.setAtivo(rs.getInt("ativo") == 1);
        membro.setEspecialidades(rs.getString("especialidades"));
        return membro;
    }
}
