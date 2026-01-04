package br.tec.jessebezerra.app.repository;

import br.tec.jessebezerra.app.config.DatabaseConfig;
import br.tec.jessebezerra.app.entity.Sprint;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SprintRepository {

    public Sprint save(Sprint sprint) {
        String sql = "INSERT INTO sprint (nome, data_inicio, duracao_semanas, data_fim) VALUES (?, ?, ?, ?)";
        
        System.out.println("Repository.save() - Salvando: " + sprint);
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, sprint.getNome());
            pstmt.setString(2, sprint.getDataInicio().toString());
            pstmt.setInt(3, sprint.getDuracaoSemanas());
            pstmt.setString(4, sprint.getDataFim() != null ? sprint.getDataFim().toString() : null);
            
            System.out.println("Executando INSERT...");
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Linhas afetadas: " + rowsAffected);
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    sprint.setId(rs.getLong(1));
                    System.out.println("ID gerado: " + sprint.getId());
                }
            }
            
            return sprint;
        } catch (SQLException e) {
            System.err.println("ERRO SQL ao salvar sprint: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao salvar sprint", e);
        }
    }

    public Sprint update(Sprint sprint) {
        String sql = "UPDATE sprint SET nome = ?, data_inicio = ?, duracao_semanas = ?, data_fim = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, sprint.getNome());
            pstmt.setString(2, sprint.getDataInicio().toString());
            pstmt.setInt(3, sprint.getDuracaoSemanas());
            pstmt.setString(4, sprint.getDataFim() != null ? sprint.getDataFim().toString() : null);
            pstmt.setLong(5, sprint.getId());
            
            pstmt.executeUpdate();
            return sprint;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar sprint", e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM sprint WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar sprint", e);
        }
    }

    public Optional<Sprint> findById(Long id) {
        String sql = "SELECT * FROM sprint WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToSprint(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar sprint", e);
        }
    }

    public List<Sprint> findAll() {
        String sql = "SELECT * FROM sprint ORDER BY data_inicio DESC";
        List<Sprint> sprints = new ArrayList<>();
        
        System.out.println("Repository.findAll() - Buscando todas as sprints...");
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Sprint sprint = mapResultSetToSprint(rs);
                System.out.println("Sprint encontrada: " + sprint);
                sprints.add(sprint);
            }
            
            System.out.println("Total de sprints no banco: " + sprints.size());
            return sprints;
        } catch (SQLException e) {
            System.err.println("ERRO ao buscar sprints: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao buscar sprints", e);
        }
    }

    public boolean existsOverlappingSprint(LocalDate dataInicio, LocalDate dataFim, Long excludeId) {
        String sql = "SELECT COUNT(*) FROM sprint WHERE " +
                     "((data_inicio <= ? AND data_fim >= ?) OR " +
                     "(data_inicio <= ? AND data_fim >= ?) OR " +
                     "(data_inicio >= ? AND data_fim <= ?))";
        
        if (excludeId != null) {
            sql += " AND id != ?";
        }
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, dataInicio.toString());
            pstmt.setString(2, dataInicio.toString());
            pstmt.setString(3, dataFim.toString());
            pstmt.setString(4, dataFim.toString());
            pstmt.setString(5, dataInicio.toString());
            pstmt.setString(6, dataFim.toString());
            
            if (excludeId != null) {
                pstmt.setLong(7, excludeId);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar sobreposição de sprints", e);
        }
    }
    
    private Sprint mapResultSetToSprint(ResultSet rs) throws SQLException {
        Sprint sprint = new Sprint();
        sprint.setId(rs.getLong("id"));
        sprint.setNome(rs.getString("nome"));
        sprint.setDataInicio(LocalDate.parse(rs.getString("data_inicio")));
        sprint.setDuracaoSemanas(rs.getInt("duracao_semanas"));
        
        String dataFimStr = rs.getString("data_fim");
        if (dataFimStr != null && !dataFimStr.isEmpty()) {
            sprint.setDataFim(LocalDate.parse(dataFimStr));
        }
        
        return sprint;
    }
}
