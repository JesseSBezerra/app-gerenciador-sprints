package br.tec.jessebezerra.app.controller;

import br.tec.jessebezerra.app.dto.AplicacaoDTO;
import br.tec.jessebezerra.app.service.AplicacaoService;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.Optional;

public class AplicacaoController extends BaseController {
    
    @FXML
    private TextField nomeField;
    
    @FXML
    private TextArea descricaoField;
    
    @FXML
    private TableView<AplicacaoTableModel> aplicacaoTable;
    
    @FXML
    private TableColumn<AplicacaoTableModel, Long> idColumn;
    
    @FXML
    private TableColumn<AplicacaoTableModel, String> nomeColumn;
    
    @FXML
    private TableColumn<AplicacaoTableModel, String> descricaoColumn;
    
    @FXML
    private Button salvarButton;
    
    @FXML
    private Button limparButton;
    
    @FXML
    private Button editarButton;
    
    @FXML
    private Button excluirButton;
    
    private final AplicacaoService service;
    private final ObservableList<AplicacaoTableModel> aplicacaoList;
    private Long editingId;

    public AplicacaoController() {
        this.service = new AplicacaoService();
        this.aplicacaoList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        configurarTabela();
        carregarAplicacoes();
        configurarBotoes();
    }

    private void configurarTabela() {
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        nomeColumn.setCellValueFactory(cellData -> cellData.getValue().nomeProperty());
        descricaoColumn.setCellValueFactory(cellData -> cellData.getValue().descricaoProperty());
        
        aplicacaoTable.setItems(aplicacaoList);
    }

    private void carregarAplicacoes() {
        aplicacaoList.clear();
        List<AplicacaoDTO> aplicacoes = service.findAll();
        
        for (AplicacaoDTO dto : aplicacoes) {
            aplicacaoList.add(new AplicacaoTableModel(
                dto.getId(),
                dto.getNome(),
                dto.getDescricao()
            ));
        }
    }

    private void configurarBotoes() {
        editarButton.setDisable(true);
        excluirButton.setDisable(true);
        
        aplicacaoTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            editarButton.setDisable(!hasSelection);
            excluirButton.setDisable(!hasSelection);
        });
    }

    @FXML
    private void handleSalvar() {
        try {
            String nome = nomeField.getText();
            String descricao = descricaoField.getText();
            
            if (nome == null || nome.trim().isEmpty()) {
                mostrarErro("Nome da aplicação é obrigatório");
                return;
            }
            
            AplicacaoDTO dto = new AplicacaoDTO();
            dto.setNome(nome.trim());
            dto.setDescricao(descricao != null ? descricao.trim() : null);
            
            if (editingId != null) {
                dto.setId(editingId);
                service.update(dto);
                mostrarSucesso("Aplicação atualizada com sucesso!");
            } else {
                service.create(dto);
                mostrarSucesso("Aplicação criada com sucesso!");
            }
            
            limparFormulario();
            carregarAplicacoes();
            
        } catch (Exception e) {
            mostrarErro("Erro ao salvar aplicação: " + e.getMessage());
        }
    }

    @FXML
    private void handleLimpar() {
        limparFormulario();
    }

    @FXML
    private void handleEditar() {
        AplicacaoTableModel selected = aplicacaoTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            editingId = selected.getId();
            nomeField.setText(selected.getNome());
            descricaoField.setText(selected.getDescricao());
            salvarButton.setText("Atualizar");
        }
    }

    @FXML
    private void handleExcluir() {
        AplicacaoTableModel selected = aplicacaoTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Exclusão");
            alert.setHeaderText("Deseja realmente excluir esta aplicação?");
            alert.setContentText(selected.getNome());
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    service.delete(selected.getId());
                    mostrarSucesso("Aplicação excluída com sucesso!");
                    carregarAplicacoes();
                    limparFormulario();
                } catch (Exception e) {
                    mostrarErro("Erro ao excluir aplicação: " + e.getMessage());
                }
            }
        }
    }

    private void limparFormulario() {
        editingId = null;
        nomeField.clear();
        descricaoField.clear();
        salvarButton.setText("Salvar");
        aplicacaoTable.getSelectionModel().clearSelection();
    }

    private void mostrarSucesso(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    @Override
    protected javafx.stage.Stage getCurrentStage() {
        return (javafx.stage.Stage) aplicacaoTable.getScene().getWindow();
    }

    @FXML
    protected void onNavigateToMembros() {
        navigateTo("/br/tec/jessebezerra/app/membro-view.fxml", "Gerenciamento de Membros");
    }

    @FXML
    protected void onNavigateToProjetos() {
        navigateTo("/br/tec/jessebezerra/app/projeto-view.fxml", "Gerenciamento de Projetos");
    }

    public static class AplicacaoTableModel {
        private final SimpleLongProperty id;
        private final SimpleStringProperty nome;
        private final SimpleStringProperty descricao;

        public AplicacaoTableModel(Long id, String nome, String descricao) {
            this.id = new SimpleLongProperty(id);
            this.nome = new SimpleStringProperty(nome);
            this.descricao = new SimpleStringProperty(descricao != null ? descricao : "");
        }

        public Long getId() { return id.get(); }
        public SimpleLongProperty idProperty() { return id; }

        public String getNome() { return nome.get(); }
        public SimpleStringProperty nomeProperty() { return nome; }

        public String getDescricao() { return descricao.get(); }
        public SimpleStringProperty descricaoProperty() { return descricao; }
    }
}
