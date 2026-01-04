package br.tec.jessebezerra.app.repository;

import br.tec.jessebezerra.app.config.DatabaseConfig;
import br.tec.jessebezerra.app.entity.ItemSprint;
import br.tec.jessebezerra.app.entity.StatusItem;
import br.tec.jessebezerra.app.entity.TipoItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemSprintRepository {

    public ItemSprint save(ItemSprint item) {
        String sql = "INSERT INTO item_sprint (tipo, titulo, descricao, duracao_semanas, duracao_dias, status, sprint_id, membro_id, item_pai_id, projeto_id, aplicacao_id, prioridade) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, item.getTipo().name());
            pstmt.setString(2, item.getTitulo());
            pstmt.setString(3, item.getDescricao());
            
            if (item.getDuracaoSemanas() != null) {
                pstmt.setInt(4, item.getDuracaoSemanas());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            
            if (item.getDuracaoDias() != null) {
                pstmt.setInt(5, item.getDuracaoDias());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }
            
            pstmt.setString(6, item.getStatus().name());
            pstmt.setLong(7, item.getSprintId());
            
            if (item.getMembroId() != null) {
                pstmt.setLong(8, item.getMembroId());
            } else {
                pstmt.setNull(8, Types.INTEGER);
            }
            
            if (item.getItemPaiId() != null) {
                pstmt.setLong(9, item.getItemPaiId());
            } else {
                pstmt.setNull(9, Types.INTEGER);
            }
            
            if (item.getProjetoId() != null) {
                pstmt.setLong(10, item.getProjetoId());
            } else {
                pstmt.setNull(10, Types.INTEGER);
            }
            
            if (item.getAplicacaoId() != null) {
                pstmt.setLong(11, item.getAplicacaoId());
            } else {
                pstmt.setNull(11, Types.INTEGER);
            }
            
            if (item.getPrioridade() != null) {
                pstmt.setInt(12, item.getPrioridade());
            } else {
                pstmt.setNull(12, Types.INTEGER);
            }
            
            pstmt.executeUpdate();
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    item.setId(rs.getLong(1));
                }
            }
            
            return item;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar item da sprint", e);
        }
    }

    public ItemSprint update(ItemSprint item) {
        String sql = "UPDATE item_sprint SET tipo = ?, titulo = ?, descricao = ?, duracao_semanas = ?, duracao_dias = ?, " +
                     "status = ?, sprint_id = ?, membro_id = ?, item_pai_id = ?, projeto_id = ?, aplicacao_id = ?, prioridade = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, item.getTipo().name());
            pstmt.setString(2, item.getTitulo());
            pstmt.setString(3, item.getDescricao());
            
            if (item.getDuracaoSemanas() != null) {
                pstmt.setInt(4, item.getDuracaoSemanas());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            
            if (item.getDuracaoDias() != null) {
                pstmt.setInt(5, item.getDuracaoDias());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }
            
            pstmt.setString(6, item.getStatus().name());
            pstmt.setLong(7, item.getSprintId());
            
            if (item.getMembroId() != null) {
                pstmt.setLong(8, item.getMembroId());
            } else {
                pstmt.setNull(8, Types.INTEGER);
            }
            
            if (item.getItemPaiId() != null) {
                pstmt.setLong(9, item.getItemPaiId());
            } else {
                pstmt.setNull(9, Types.INTEGER);
            }
            
            if (item.getProjetoId() != null) {
                pstmt.setLong(10, item.getProjetoId());
            } else {
                pstmt.setNull(10, Types.INTEGER);
            }
            
            if (item.getAplicacaoId() != null) {
                pstmt.setLong(11, item.getAplicacaoId());
            } else {
                pstmt.setNull(11, Types.INTEGER);
            }
            
            if (item.getPrioridade() != null) {
                pstmt.setInt(12, item.getPrioridade());
            } else {
                pstmt.setNull(12, Types.INTEGER);
            }
            
            pstmt.setLong(13, item.getId());
            
            pstmt.executeUpdate();
            return item;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar item da sprint", e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM item_sprint WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir item da sprint", e);
        }
    }

    public Optional<ItemSprint> findById(Long id) {
        String sql = "SELECT * FROM item_sprint WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToItemSprint(rs));
                }
            }
            
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar item da sprint", e);
        }
    }

    public List<ItemSprint> findAll() {
        String sql = "SELECT * FROM item_sprint ORDER BY id ASC";
        List<ItemSprint> itens = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                itens.add(mapResultSetToItemSprint(rs));
            }
            
            return itens;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar itens da sprint", e);
        }
    }

    public List<ItemSprint> findBySprintId(Long sprintId) {
        String sql = "SELECT * FROM item_sprint WHERE sprint_id = ? ORDER BY id ASC";
        List<ItemSprint> itens = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, sprintId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    itens.add(mapResultSetToItemSprint(rs));
                }
            }
            
            return itens;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar itens da sprint", e);
        }
    }

    public List<ItemSprint> findByItemPaiId(Long itemPaiId) {
        String sql = "SELECT * FROM item_sprint WHERE item_pai_id = ? ORDER BY id ASC";
        List<ItemSprint> itens = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, itemPaiId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    itens.add(mapResultSetToItemSprint(rs));
                }
            }
            
            return itens;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar subitens", e);
        }
    }

    public Integer getSomaDuracaoFilhosSemanas(Long itemPaiId) {
        String sql = "SELECT SUM(duracao_semanas) FROM item_sprint WHERE item_pai_id = ? AND duracao_semanas IS NOT NULL";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, itemPaiId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao calcular duração dos filhos em semanas", e);
        }
    }
    
    public Integer getSomaDuracaoFilhosDias(Long itemPaiId) {
        String sql = "SELECT SUM(duracao_dias) FROM item_sprint WHERE item_pai_id = ? AND duracao_dias IS NOT NULL";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, itemPaiId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao calcular duração dos filhos em dias", e);
        }
    }

    private ItemSprint mapResultSetToItemSprint(ResultSet rs) throws SQLException {
        ItemSprint item = new ItemSprint();
        item.setId(rs.getLong("id"));
        item.setTipo(TipoItem.valueOf(rs.getString("tipo")));
        item.setTitulo(rs.getString("titulo"));
        item.setDescricao(rs.getString("descricao"));
        
        int duracaoSemanas = rs.getInt("duracao_semanas");
        if (!rs.wasNull()) {
            item.setDuracaoSemanas(duracaoSemanas);
        }
        
        int duracaoDias = rs.getInt("duracao_dias");
        if (!rs.wasNull()) {
            item.setDuracaoDias(duracaoDias);
        }
        
        item.setStatus(StatusItem.valueOf(rs.getString("status")));
        item.setSprintId(rs.getLong("sprint_id"));
        
        long membroId = rs.getLong("membro_id");
        if (!rs.wasNull()) {
            item.setMembroId(membroId);
        }
        
        long itemPaiId = rs.getLong("item_pai_id");
        if (!rs.wasNull()) {
            item.setItemPaiId(itemPaiId);
        }
        
        long projetoId = rs.getLong("projeto_id");
        if (!rs.wasNull()) {
            item.setProjetoId(projetoId);
        }
        
        long aplicacaoId = rs.getLong("aplicacao_id");
        if (!rs.wasNull()) {
            item.setAplicacaoId(aplicacaoId);
        }
        
        int prioridade = rs.getInt("prioridade");
        if (!rs.wasNull()) {
            item.setPrioridade(prioridade);
        }
        
        return item;
    }
}
