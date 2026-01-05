package br.tec.jessebezerra.app.controller;

import br.tec.jessebezerra.app.dto.ItemSprintDTO;
import br.tec.jessebezerra.app.dto.MembroDTO;
import br.tec.jessebezerra.app.dto.SprintDTO;
import br.tec.jessebezerra.app.entity.StatusItem;
import br.tec.jessebezerra.app.entity.TipoItem;
import br.tec.jessebezerra.app.service.ItemSprintService;
import br.tec.jessebezerra.app.service.MembroService;
import br.tec.jessebezerra.app.service.SprintService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class FeatureController extends BaseController {
    
    @FXML private TextField tituloField;
    @FXML private TextField codigoExternoField;
    @FXML private TextArea descricaoTextArea;
    @FXML private Spinner<Integer> duracaoSpinner;
    @FXML private ComboBox<StatusItem> statusComboBox;
    @FXML private ComboBox<SprintDTO> sprintComboBox;
    @FXML private ComboBox<MembroDTO> membroComboBox;
    
    @FXML private TableView<FeatureTableModel> featureTable;
    @FXML private TableColumn<FeatureTableModel, Long> idColumn;
    @FXML private TableColumn<FeatureTableModel, String> tituloColumn;
    @FXML private TableColumn<FeatureTableModel, Integer> duracaoColumn;
    @FXML private TableColumn<FeatureTableModel, StatusItem> statusColumn;
    @FXML private TableColumn<FeatureTableModel, String> sprintColumn;
    @FXML private TableColumn<FeatureTableModel, String> membroColumn;
    
    @FXML private Button salvarButton;
    @FXML private Button editarButton;
    @FXML private Button excluirButton;
    @FXML private Button gerenciarSubsButton;
    @FXML private Button menuToggleButton;
    @FXML private VBox sideMenu;
    
    private final ItemSprintService service;
    private final SprintService sprintService;
    private final MembroService membroService;
    private boolean menuExpanded = true;
    private final ObservableList<FeatureTableModel> featureList;
    private Long editingId;

    public FeatureController() {
        this.service = new ItemSprintService();
        this.sprintService = new SprintService();
        this.membroService = new MembroService();
        this.featureList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        statusComboBox.setItems(FXCollections.observableArrayList(StatusItem.values()));
        statusComboBox.setValue(StatusItem.CRIADO);
        
        SpinnerValueFactory<Integer> valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 52, 2);
        duracaoSpinner.setValueFactory(valueFactory);
        
        loadSprints();
        loadMembros();
        
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        tituloColumn.setCellValueFactory(cellData -> cellData.getValue().tituloProperty());
        duracaoColumn.setCellValueFactory(cellData -> cellData.getValue().duracaoSemanasProperty().asObject());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        sprintColumn.setCellValueFactory(cellData -> cellData.getValue().sprintNomeProperty());
        membroColumn.setCellValueFactory(cellData -> cellData.getValue().membroNomeProperty());
        
        featureTable.setItems(featureList);
        
        featureTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                boolean featureSelected = newSelection != null;
                editarButton.setDisable(!featureSelected);
                excluirButton.setDisable(!featureSelected);
                gerenciarSubsButton.setDisable(!featureSelected);
            }
        );
        
        loadFeatures();
    }

    @FXML
    protected void onSalvarClick() {
        if (!validateFields()) {
            showErrorAlert("Erro de Validação", "Por favor, preencha todos os campos obrigatórios.");
            return;
        }
        
        ItemSprintDTO dto = new ItemSprintDTO();
        dto.setId(editingId);
        dto.setTipo(TipoItem.FEATURE);
        dto.setTitulo(tituloField.getText());
        dto.setCodigoExterno(codigoExternoField.getText().trim().isEmpty() ? null : codigoExternoField.getText().trim());
        dto.setDescricao(descricaoTextArea.getText());
        dto.setDuracaoSemanas(duracaoSpinner.getValue());
        dto.setStatus(statusComboBox.getValue());
        dto.setSprintId(sprintComboBox.getValue().getId());
        
        if (membroComboBox.getValue() != null) {
            dto.setMembroId(membroComboBox.getValue().getId());
        }
        
        try {
            if (editingId == null) {
                service.create(dto);
                showAlert("Sucesso", "Feature criada com sucesso!");
            } else {
                service.update(dto);
                showAlert("Sucesso", "Feature atualizada com sucesso!");
            }
            clearForm();
            loadFeatures();
        } catch (IllegalArgumentException e) {
            showErrorAlert("Validação", e.getMessage());
        } catch (Exception e) {
            showErrorAlert("Erro", "Erro ao salvar feature: " + e.getMessage());
        }
    }

    @FXML
    protected void onLimparClick() {
        clearForm();
    }

    @FXML
    protected void onEditarClick() {
        FeatureTableModel selected = featureTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            editingId = selected.getId();
            tituloField.setText(selected.getTitulo());
            codigoExternoField.setText(selected.getCodigoExterno() != null ? selected.getCodigoExterno() : "");
            descricaoTextArea.setText(selected.getDescricao());
            duracaoSpinner.getValueFactory().setValue(selected.getDuracaoSemanas());
            statusComboBox.setValue(selected.getStatus());
            
            sprintComboBox.getItems().stream()
                .filter(s -> s.getId().equals(selected.getSprintId()))
                .findFirst()
                .ifPresent(sprintComboBox::setValue);
            
            if (selected.getMembroId() != null) {
                membroComboBox.getItems().stream()
                    .filter(m -> m.getId().equals(selected.getMembroId()))
                    .findFirst()
                    .ifPresent(membroComboBox::setValue);
            }
            
            salvarButton.setText("Atualizar");
        }
    }

    @FXML
    protected void onExcluirClick() {
        FeatureTableModel selected = featureTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmar Exclusão");
            confirmAlert.setHeaderText("Deseja realmente excluir esta feature?");
            confirmAlert.setContentText("Feature: " + selected.getTitulo());
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    service.delete(selected.getId());
                    showAlert("Sucesso", "Feature excluída com sucesso!");
                    loadFeatures();
                } catch (Exception e) {
                    showErrorAlert("Erro", "Erro ao excluir feature: " + e.getMessage());
                }
            }
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
        navigateTo("/br/tec/jessebezerra/app/sprint-view.fxml", "Gerenciamento de Sprints");
    }
    
    @FXML
    protected void onNavigateToEquipe() {
        navigateTo("/br/tec/jessebezerra/app/membro-view.fxml", "Gerenciamento de Membros");
    }
    
    @FXML
    protected void onNavigateToHistorias() {
        navigateTo("/br/tec/jessebezerra/app/historia-view.fxml", "Gerenciamento de Histórias");
    }
    
    @FXML
    protected void onNavigateToFeatures() {
        // Já estamos na tela de features
    }
    
    @FXML
    protected void onNavigateToTarefas() {
        navigateTo("/br/tec/jessebezerra/app/tarefa-view.fxml", "Gerenciamento de Tarefas");
    }

    private void loadSprints() {
        List<SprintDTO> sprints = sprintService.findAll();
        sprintComboBox.setItems(FXCollections.observableArrayList(sprints));
    }

    private void loadMembros() {
        List<MembroDTO> membros = membroService.findAll();
        membroComboBox.setItems(FXCollections.observableArrayList(membros));
    }

    private void loadFeatures() {
        List<ItemSprintDTO> features = service.findAll().stream()
            .filter(item -> item.getTipo() == TipoItem.FEATURE)
            .toList();
        
        featureList.clear();
        features.forEach(dto -> {
            FeatureTableModel model = new FeatureTableModel(
                dto.getId(),
                dto.getTitulo(),
                dto.getCodigoExterno(),
                dto.getDescricao(),
                dto.getDuracaoSemanas(),
                dto.getStatus(),
                dto.getSprintId(),
                dto.getSprintNome(),
                dto.getMembroId(),
                dto.getMembroNome()
            );
            featureList.add(model);
        });
        
        featureTable.refresh();
    }

    private void clearForm() {
        editingId = null;
        tituloField.clear();
        codigoExternoField.clear();
        descricaoTextArea.clear();
        duracaoSpinner.getValueFactory().setValue(2);
        statusComboBox.setValue(StatusItem.CRIADO);
        sprintComboBox.setValue(null);
        membroComboBox.setValue(null);
        salvarButton.setText("Salvar");
        featureTable.getSelectionModel().clearSelection();
    }

    private boolean validateFields() {
        return tituloField.getText() != null && !tituloField.getText().trim().isEmpty()
            && statusComboBox.getValue() != null
            && sprintComboBox.getValue() != null
            && duracaoSpinner.getValue() != null && duracaoSpinner.getValue() > 0;
    }
    
    @FXML
    protected void onGerenciarSubsClick() {
        FeatureTableModel selected = featureTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorAlert("Erro", "Selecione uma feature primeiro.");
            return;
        }
        
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/br/tec/jessebezerra/app/sub-dialog.fxml")
            );
            javafx.scene.Parent root = loader.load();
            
            SubDialogController controller = loader.getController();
            controller.setHistoria(selected.getId(), selected.getTitulo(), selected.getDuracaoSemanas(), selected.getSprintId());
            
            // Capturar dimensões da tela
            javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
            javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
            
            // Calcular tamanho do dialog (60% da largura, 70% da altura)
            double dialogWidth = bounds.getWidth() * 0.6;
            double dialogHeight = bounds.getHeight() * 0.7;
            
            // Garantir tamanhos mínimos
            dialogWidth = Math.max(dialogWidth, 600);
            dialogHeight = Math.max(dialogHeight, 450);
            
            javafx.stage.Stage dialogStage = new javafx.stage.Stage();
            dialogStage.setTitle("Gerenciar SUBs - " + selected.getTitulo());
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialogStage.initOwner(tituloField.getScene().getWindow());
            dialogStage.setScene(new javafx.scene.Scene(root, dialogWidth, dialogHeight));
            dialogStage.setResizable(true);
            
            // Centralizar na tela
            dialogStage.setX((bounds.getWidth() - dialogWidth) / 2 + bounds.getMinX());
            dialogStage.setY((bounds.getHeight() - dialogHeight) / 2 + bounds.getMinY());
            
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erro", "Erro ao abrir gerenciador de SUBs: " + e.getMessage());
        }
    }

    public static class FeatureTableModel {
        private final SimpleLongProperty id;
        private final SimpleStringProperty titulo;
        private final SimpleStringProperty codigoExterno;
        private final SimpleStringProperty descricao;
        private final SimpleIntegerProperty duracaoSemanas;
        private final SimpleObjectProperty<StatusItem> status;
        private final SimpleLongProperty sprintId;
        private final SimpleStringProperty sprintNome;
        private final SimpleObjectProperty<Long> membroId;
        private final SimpleStringProperty membroNome;

        public FeatureTableModel(Long id, String titulo, String codigoExterno, String descricao, Integer duracaoSemanas,
                                StatusItem status, Long sprintId, String sprintNome,
                                Long membroId, String membroNome) {
            this.id = new SimpleLongProperty(id);
            this.titulo = new SimpleStringProperty(titulo);
            this.codigoExterno = new SimpleStringProperty(codigoExterno);
            this.descricao = new SimpleStringProperty(descricao);
            this.duracaoSemanas = new SimpleIntegerProperty(duracaoSemanas != null ? duracaoSemanas : 0);
            this.status = new SimpleObjectProperty<>(status);
            this.sprintId = new SimpleLongProperty(sprintId);
            this.sprintNome = new SimpleStringProperty(sprintNome);
            this.membroId = new SimpleObjectProperty<>(membroId);
            this.membroNome = new SimpleStringProperty(membroNome);
        }

        public long getId() { return id.get(); }
        public SimpleLongProperty idProperty() { return id; }
        
        public String getTitulo() { return titulo.get(); }
        public SimpleStringProperty tituloProperty() { return titulo; }
        
        public String getCodigoExterno() { return codigoExterno.get(); }
        public SimpleStringProperty codigoExternoProperty() { return codigoExterno; }
        
        public String getDescricao() { return descricao.get(); }
        
        public Integer getDuracaoSemanas() { return duracaoSemanas.get(); }
        public SimpleIntegerProperty duracaoSemanasProperty() { return duracaoSemanas; }
        
        public StatusItem getStatus() { return status.get(); }
        public SimpleObjectProperty<StatusItem> statusProperty() { return status; }
        
        public Long getSprintId() { return sprintId.get(); }
        
        public String getSprintNome() { return sprintNome.get(); }
        public SimpleStringProperty sprintNomeProperty() { return sprintNome; }
        
        public Long getMembroId() { return membroId.get(); }
        
        public String getMembroNome() { return membroNome.get(); }
        public SimpleStringProperty membroNomeProperty() { return membroNome; }
    }
    
    /**
     * Método público para editar um item a partir de um ItemSprintDTO
     * Usado pela Timeline para abrir o formulário de edição
     */
    public void editItem(ItemSprintDTO item) {
        if (item == null || item.getId() == null) {
            return;
        }
        
        // Armazenar ID para edição
        editingId = item.getId();
        
        // Preencher formulário com dados do item
        tituloField.setText(item.getTitulo());
        descricaoTextArea.setText(item.getDescricao() != null ? item.getDescricao() : "");
        duracaoSpinner.getValueFactory().setValue(item.getDuracaoSemanas() != null ? item.getDuracaoSemanas() : 2);
        statusComboBox.setValue(item.getStatus());
        
        // Selecionar Sprint
        if (item.getSprintId() != null) {
            sprintComboBox.getItems().stream()
                .filter(s -> s.getId().equals(item.getSprintId()))
                .findFirst()
                .ifPresent(sprintComboBox::setValue);
        }
        
        // Selecionar Membro
        if (item.getMembroId() != null) {
            membroComboBox.getItems().stream()
                .filter(m -> m.getId().equals(item.getMembroId()))
                .findFirst()
                .ifPresent(membroComboBox::setValue);
        }
        
        salvarButton.setText("Atualizar");
    }
    
    @Override
    protected javafx.stage.Stage getCurrentStage() {
        return (javafx.stage.Stage) featureTable.getScene().getWindow();
    }
}
