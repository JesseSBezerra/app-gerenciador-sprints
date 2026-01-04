package br.tec.jessebezerra.app.controller;

import br.tec.jessebezerra.app.dto.MembroDTO;
import br.tec.jessebezerra.app.entity.Funcao;
import br.tec.jessebezerra.app.service.MembroService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class MembroController extends BaseController {
    
    @FXML
    private TextField nomeField;
    
    @FXML
    private ComboBox<Funcao> funcaoComboBox;
    
    @FXML
    private CheckBox ativoCheckBox;
    
    @FXML
    private TextArea especialidadesTextArea;
    
    @FXML
    private TableView<MembroTableModel> membroTable;
    
    @FXML
    private TableColumn<MembroTableModel, Long> idColumn;
    
    @FXML
    private TableColumn<MembroTableModel, String> nomeColumn;
    
    @FXML
    private TableColumn<MembroTableModel, Funcao> funcaoColumn;
    
    @FXML
    private TableColumn<MembroTableModel, Boolean> ativoColumn;
    
    @FXML
    private TableColumn<MembroTableModel, String> especialidadesColumn;
    
    @FXML
    private Button salvarButton;
    
    @FXML
    private Button limparButton;
    
    @FXML
    private Button editarButton;
    
    @FXML
    private Button excluirButton;
    
    @FXML
    private Button menuToggleButton;
    
    @FXML
    private VBox sideMenu;
    
    private final MembroService service;
    private boolean menuExpanded = true;
    private final ObservableList<MembroTableModel> membroList;
    private Long editingId;

    public MembroController() {
        this.service = new MembroService();
        this.membroList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        // Configurar ComboBox de funções
        funcaoComboBox.setItems(FXCollections.observableArrayList(Funcao.values()));
        
        // Marcar como ativo por padrão
        ativoCheckBox.setSelected(true);
        
        // Configurar colunas da tabela
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        nomeColumn.setCellValueFactory(cellData -> cellData.getValue().nomeProperty());
        funcaoColumn.setCellValueFactory(cellData -> cellData.getValue().funcaoProperty());
        ativoColumn.setCellValueFactory(cellData -> cellData.getValue().ativoProperty().asObject());
        especialidadesColumn.setCellValueFactory(cellData -> cellData.getValue().especialidadesProperty());
        
        // Customizar coluna ativo para exibir Sim/Não
        ativoColumn.setCellFactory(column -> new TableCell<MembroTableModel, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Sim" : "Não");
                    setStyle(item ? "-fx-text-fill: #0047AB;" : "-fx-text-fill: #6A737D;");
                }
            }
        });
        
        membroTable.setItems(membroList);
        
        membroTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                editarButton.setDisable(newSelection == null);
                excluirButton.setDisable(newSelection == null);
            }
        );
        
        loadMembros();
    }

    @FXML
    protected void onSalvarClick() {
        if (!validateFields()) {
            showAlert("Erro de Validação", "Por favor, preencha todos os campos corretamente.");
            return;
        }
        
        MembroDTO dto = new MembroDTO();
        dto.setId(editingId);
        dto.setNome(nomeField.getText());
        dto.setFuncao(funcaoComboBox.getValue());
        dto.setAtivo(ativoCheckBox.isSelected());
        dto.setEspecialidades(especialidadesTextArea.getText());
        
        try {
            if (editingId == null) {
                service.create(dto);
                showAlert("Sucesso", "Membro cadastrado com sucesso!");
            } else {
                service.update(dto);
                showAlert("Sucesso", "Membro atualizado com sucesso!");
            }
            clearForm();
            loadMembros();
        } catch (Exception e) {
            showAlert("Erro", "Erro ao salvar membro: " + e.getMessage());
        }
    }

    @FXML
    protected void onLimparClick() {
        clearForm();
    }

    @FXML
    protected void onEditarClick() {
        MembroTableModel selected = membroTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            editingId = selected.getId();
            nomeField.setText(selected.getNome());
            funcaoComboBox.setValue(selected.getFuncao());
            ativoCheckBox.setSelected(selected.getAtivo());
            especialidadesTextArea.setText(selected.getEspecialidades());
            salvarButton.setText("Atualizar");
        }
    }

    @FXML
    protected void onMenuToggle() {
        if (menuExpanded) {
            sideMenu.setPrefWidth(60);
            sideMenu.setMinWidth(60);
            sideMenu.setMaxWidth(60);
        } else {
            sideMenu.setPrefWidth(220);
            sideMenu.setMinWidth(220);
            sideMenu.setMaxWidth(220);
        }
        menuExpanded = !menuExpanded;
    }
    
    @FXML
    protected void onNavigateToSprints() {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) nomeField.getScene().getWindow();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/br/tec/jessebezerra/app/sprint-view.fxml")
            );
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load(), currentWidth, currentHeight);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erro", "Erro ao navegar para tela de Sprints: " + e.getMessage());
        }
    }
    
    @FXML
    protected void onNavigateToFeatures() {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) nomeField.getScene().getWindow();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/br/tec/jessebezerra/app/feature-view.fxml")
            );
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load(), currentWidth, currentHeight);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erro", "Erro ao navegar para tela de Features: " + e.getMessage());
        }
    }
    
    @FXML
    protected void onNavigateToHistorias() {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) nomeField.getScene().getWindow();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/br/tec/jessebezerra/app/historia-view.fxml")
            );
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load(), currentWidth, currentHeight);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erro", "Erro ao navegar para tela de Histórias: " + e.getMessage());
        }
    }
    
    @FXML
    protected void onNavigateToTarefas() {
        navigateTo("/br/tec/jessebezerra/app/tarefa-view.fxml", "Gerenciamento de Tarefas");
    }
    
    @FXML
    protected void onExcluirClick() {
        MembroTableModel selected = membroTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmar Exclusão");
            confirmAlert.setHeaderText("Deseja realmente excluir este membro?");
            confirmAlert.setContentText("Membro: " + selected.getNome());
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    service.delete(selected.getId());
                    showAlert("Sucesso", "Membro excluído com sucesso!");
                    loadMembros();
                } catch (Exception e) {
                    showAlert("Erro", "Erro ao excluir membro: " + e.getMessage());
                }
            }
        }
    }

    private void loadMembros() {
        List<MembroDTO> membros = service.findAll();
        
        ObservableList<MembroTableModel> tempList = FXCollections.observableArrayList();
        
        membros.forEach(dto -> {
            MembroTableModel model = new MembroTableModel(
                dto.getId(),
                dto.getNome(),
                dto.getFuncao(),
                dto.getAtivo(),
                dto.getEspecialidades()
            );
            tempList.add(model);
        });
        
        membroList.clear();
        membroList.addAll(tempList);
        
        membroTable.setItems(null);
        membroTable.setItems(membroList);
        membroTable.refresh();
    }

    private void clearForm() {
        editingId = null;
        nomeField.clear();
        funcaoComboBox.setValue(null);
        ativoCheckBox.setSelected(true);
        especialidadesTextArea.clear();
        salvarButton.setText("Salvar");
        membroTable.getSelectionModel().clearSelection();
    }

    private boolean validateFields() {
        return nomeField.getText() != null && !nomeField.getText().trim().isEmpty()
            && funcaoComboBox.getValue() != null;
    }

    public static class MembroTableModel {
        private final SimpleLongProperty id;
        private final SimpleStringProperty nome;
        private final SimpleObjectProperty<Funcao> funcao;
        private final SimpleBooleanProperty ativo;
        private final SimpleStringProperty especialidades;

        public MembroTableModel(Long id, String nome, Funcao funcao, Boolean ativo, String especialidades) {
            this.id = new SimpleLongProperty(id);
            this.nome = new SimpleStringProperty(nome);
            this.funcao = new SimpleObjectProperty<>(funcao);
            this.ativo = new SimpleBooleanProperty(ativo);
            this.especialidades = new SimpleStringProperty(especialidades);
        }

        public long getId() { return id.get(); }
        public SimpleLongProperty idProperty() { return id; }
        
        public String getNome() { return nome.get(); }
        public SimpleStringProperty nomeProperty() { return nome; }
        
        public Funcao getFuncao() { return funcao.get(); }
        public SimpleObjectProperty<Funcao> funcaoProperty() { return funcao; }
        
        public Boolean getAtivo() { return ativo.get(); }
        public SimpleBooleanProperty ativoProperty() { return ativo; }
        
        public String getEspecialidades() { return especialidades.get(); }
        public SimpleStringProperty especialidadesProperty() { return especialidades; }
    }
    
    @Override
    protected javafx.stage.Stage getCurrentStage() {
        return (javafx.stage.Stage) membroTable.getScene().getWindow();
    }
}
