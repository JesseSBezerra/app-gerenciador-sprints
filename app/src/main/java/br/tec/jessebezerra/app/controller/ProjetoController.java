package br.tec.jessebezerra.app.controller;

import br.tec.jessebezerra.app.dto.ProjetoDTO;
import br.tec.jessebezerra.app.service.ProjetoService;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.Optional;

public class ProjetoController extends BaseController {
    
    @FXML
    private TextField nomeField;
    
    @FXML
    private TextArea descricaoField;
    
    @FXML
    private TableView<ProjetoTableModel> projetoTable;
    
    @FXML
    private TableColumn<ProjetoTableModel, Long> idColumn;
    
    @FXML
    private TableColumn<ProjetoTableModel, String> nomeColumn;
    
    @FXML
    private TableColumn<ProjetoTableModel, String> descricaoColumn;
    
    @FXML
    private Button salvarButton;
    
    @FXML
    private Button limparButton;
    
    @FXML
    private Button editarButton;
    
    @FXML
    private Button excluirButton;
    
    private final ProjetoService service;
    private final ObservableList<ProjetoTableModel> projetoList;
    private Long editingId;

    public ProjetoController() {
        this.service = new ProjetoService();
        this.projetoList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        configurarTabela();
        carregarProjetos();
        configurarBotoes();
    }

    private void configurarTabela() {
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        nomeColumn.setCellValueFactory(cellData -> cellData.getValue().nomeProperty());
        descricaoColumn.setCellValueFactory(cellData -> cellData.getValue().descricaoProperty());
        
        projetoTable.setItems(projetoList);
    }

    private void carregarProjetos() {
        projetoList.clear();
        List<ProjetoDTO> projetos = service.findAll();
        
        for (ProjetoDTO dto : projetos) {
            projetoList.add(new ProjetoTableModel(
                dto.getId(),
                dto.getNome(),
                dto.getDescricao()
            ));
        }
    }

    private void configurarBotoes() {
        editarButton.setDisable(true);
        excluirButton.setDisable(true);
        
        projetoTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
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
                mostrarErro("Nome do projeto é obrigatório");
                return;
            }
            
            ProjetoDTO dto = new ProjetoDTO();
            dto.setNome(nome.trim());
            dto.setDescricao(descricao != null ? descricao.trim() : null);
            
            if (editingId != null) {
                dto.setId(editingId);
                service.update(dto);
                mostrarSucesso("Projeto atualizado com sucesso!");
            } else {
                service.create(dto);
                mostrarSucesso("Projeto criado com sucesso!");
            }
            
            limparFormulario();
            carregarProjetos();
            
        } catch (Exception e) {
            mostrarErro("Erro ao salvar projeto: " + e.getMessage());
        }
    }

    @FXML
    private void handleLimpar() {
        limparFormulario();
    }

    @FXML
    private void handleEditar() {
        ProjetoTableModel selected = projetoTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            editingId = selected.getId();
            nomeField.setText(selected.getNome());
            descricaoField.setText(selected.getDescricao());
            salvarButton.setText("Atualizar");
        }
    }

    @FXML
    private void handleExcluir() {
        ProjetoTableModel selected = projetoTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Exclusão");
            alert.setHeaderText("Deseja realmente excluir este projeto?");
            alert.setContentText(selected.getNome());
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    service.delete(selected.getId());
                    mostrarSucesso("Projeto excluído com sucesso!");
                    carregarProjetos();
                    limparFormulario();
                } catch (Exception e) {
                    mostrarErro("Erro ao excluir projeto: " + e.getMessage());
                }
            }
        }
    }

    private void limparFormulario() {
        editingId = null;
        nomeField.clear();
        descricaoField.clear();
        salvarButton.setText("Salvar");
        projetoTable.getSelectionModel().clearSelection();
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
        return (javafx.stage.Stage) projetoTable.getScene().getWindow();
    }

    @FXML
    protected void onNavigateToMembros() {
        navigateTo("/br/tec/jessebezerra/app/membro-view.fxml", "Gerenciamento de Membros");
    }

    @FXML
    protected void onNavigateToAplicacoes() {
        navigateTo("/br/tec/jessebezerra/app/aplicacao-view.fxml", "Gerenciamento de Aplicações");
    }

    public static class ProjetoTableModel {
        private final SimpleLongProperty id;
        private final SimpleStringProperty nome;
        private final SimpleStringProperty descricao;

        public ProjetoTableModel(Long id, String nome, String descricao) {
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
