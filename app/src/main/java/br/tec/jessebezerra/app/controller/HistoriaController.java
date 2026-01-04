package br.tec.jessebezerra.app.controller;

import br.tec.jessebezerra.app.dto.AplicacaoDTO;
import br.tec.jessebezerra.app.dto.ItemSprintDTO;
import br.tec.jessebezerra.app.dto.MembroDTO;
import br.tec.jessebezerra.app.dto.ProjetoDTO;
import br.tec.jessebezerra.app.dto.SprintDTO;
import br.tec.jessebezerra.app.entity.StatusItem;
import br.tec.jessebezerra.app.entity.TipoItem;
import br.tec.jessebezerra.app.service.AplicacaoService;
import br.tec.jessebezerra.app.service.ItemSprintService;
import br.tec.jessebezerra.app.service.MembroService;
import br.tec.jessebezerra.app.service.ProjetoService;
import br.tec.jessebezerra.app.service.SprintService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class HistoriaController extends BaseController {
    
    @FXML private TextField tituloField;
    @FXML private TextArea descricaoTextArea;
    @FXML private Spinner<Integer> duracaoSpinner;
    @FXML private ComboBox<StatusItem> statusComboBox;
    @FXML private ComboBox<SprintDTO> sprintComboBox;
    @FXML private ComboBox<MembroDTO> membroComboBox;
    @FXML private ComboBox<ItemSprintDTO> featureComboBox;
    @FXML private ComboBox<ProjetoDTO> projetoComboBox;
    @FXML private ComboBox<AplicacaoDTO> aplicacaoComboBox;
    
    @FXML private TableView<HistoriaTableModel> historiaTable;
    @FXML private TableColumn<HistoriaTableModel, Long> idColumn;
    @FXML private TableColumn<HistoriaTableModel, String> tituloColumn;
    @FXML private TableColumn<HistoriaTableModel, Integer> duracaoColumn;
    @FXML private TableColumn<HistoriaTableModel, StatusItem> statusColumn;
    @FXML private TableColumn<HistoriaTableModel, String> sprintColumn;
    @FXML private TableColumn<HistoriaTableModel, String> featureColumn;
    @FXML private TableColumn<HistoriaTableModel, String> membroColumn;
    
    @FXML private Button salvarButton;
    @FXML private Button editarButton;
    @FXML private Button excluirButton;
    @FXML private Button gerenciarSubsButton;
    @FXML private Button menuToggleButton;
    @FXML private VBox sideMenu;
    
    private final ItemSprintService service;
    private final SprintService sprintService;
    private final MembroService membroService;
    private final ProjetoService projetoService;
    private final AplicacaoService aplicacaoService;
    private boolean menuExpanded = true;
    private final ObservableList<HistoriaTableModel> historiaList;
    private Long editingId;

    public HistoriaController() {
        this.service = new ItemSprintService();
        this.sprintService = new SprintService();
        this.membroService = new MembroService();
        this.projetoService = new ProjetoService();
        this.aplicacaoService = new AplicacaoService();
        this.historiaList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        statusComboBox.setItems(FXCollections.observableArrayList(StatusItem.values()));
        statusComboBox.setValue(StatusItem.CRIADO);
        
        SpinnerValueFactory<Integer> valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 52, 1);
        duracaoSpinner.setValueFactory(valueFactory);
        
        loadSprints();
        loadMembros();
        loadFeatures();
        loadProjetos();
        loadAplicacoes();
        
        sprintComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadFeaturesBySprint(newVal.getId());
            }
        });
        
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        tituloColumn.setCellValueFactory(cellData -> cellData.getValue().tituloProperty());
        duracaoColumn.setCellValueFactory(cellData -> cellData.getValue().duracaoSemanasProperty().asObject());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        sprintColumn.setCellValueFactory(cellData -> cellData.getValue().sprintNomeProperty());
        featureColumn.setCellValueFactory(cellData -> cellData.getValue().featureTituloProperty());
        membroColumn.setCellValueFactory(cellData -> cellData.getValue().membroNomeProperty());
        
        historiaTable.setItems(historiaList);
        
        historiaTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                boolean historiaSelected = newSelection != null;
                editarButton.setDisable(!historiaSelected);
                excluirButton.setDisable(!historiaSelected);
                gerenciarSubsButton.setDisable(!historiaSelected);
            }
        );
        
        loadHistorias();
    }

    @FXML
    protected void onSalvarClick() {
        if (!validateFields()) {
            showErrorAlert("Erro de Validação", "Por favor, preencha todos os campos obrigatórios.");
            return;
        }
        
        ItemSprintDTO dto = new ItemSprintDTO();
        dto.setId(editingId);
        dto.setTipo(TipoItem.HISTORIA);
        dto.setTitulo(tituloField.getText());
        dto.setDescricao(descricaoTextArea.getText());
        dto.setDuracaoSemanas(duracaoSpinner.getValue());
        dto.setStatus(statusComboBox.getValue());
        dto.setSprintId(sprintComboBox.getValue().getId());
        
        if (membroComboBox.getValue() != null) {
            dto.setMembroId(membroComboBox.getValue().getId());
        }
        
        if (featureComboBox.getValue() != null) {
            dto.setItemPaiId(featureComboBox.getValue().getId());
        }
        
        if (projetoComboBox.getValue() != null) {
            dto.setProjetoId(projetoComboBox.getValue().getId());
        }
        
        if (aplicacaoComboBox.getValue() != null) {
            dto.setAplicacaoId(aplicacaoComboBox.getValue().getId());
        }
        
        try {
            if (editingId == null) {
                service.create(dto);
                showAlert("Sucesso", "História criada com sucesso!");
            } else {
                service.update(dto);
                showAlert("Sucesso", "História atualizada com sucesso!");
            }
            clearForm();
            loadHistorias();
        } catch (IllegalArgumentException e) {
            showErrorAlert("Validação", e.getMessage());
        } catch (Exception e) {
            showErrorAlert("Erro", "Erro ao salvar história: " + e.getMessage());
        }
    }

    @FXML
    protected void onLimparClick() {
        clearForm();
    }

    @FXML
    protected void onEditarClick() {
        HistoriaTableModel selected = historiaTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            editingId = selected.getId();
            tituloField.setText(selected.getTitulo());
            descricaoTextArea.setText(selected.getDescricao());
            duracaoSpinner.getValueFactory().setValue(selected.getDuracaoSemanas());
            statusComboBox.setValue(selected.getStatus());
            
            sprintComboBox.getItems().stream()
                .filter(s -> s.getId().equals(selected.getSprintId()))
                .findFirst()
                .ifPresent(sprintComboBox::setValue);
            
            if (selected.getFeatureId() != null) {
                featureComboBox.getItems().stream()
                    .filter(f -> f.getId().equals(selected.getFeatureId()))
                    .findFirst()
                    .ifPresent(featureComboBox::setValue);
            }
            
            if (selected.getMembroId() != null) {
                membroComboBox.getItems().stream()
                    .filter(m -> m.getId().equals(selected.getMembroId()))
                    .findFirst()
                    .ifPresent(membroComboBox::setValue);
            }
            
            if (selected.getProjetoId() != null) {
                projetoComboBox.getItems().stream()
                    .filter(p -> p.getId().equals(selected.getProjetoId()))
                    .findFirst()
                    .ifPresent(projetoComboBox::setValue);
            }
            
            if (selected.getAplicacaoId() != null) {
                aplicacaoComboBox.getItems().stream()
                    .filter(a -> a.getId().equals(selected.getAplicacaoId()))
                    .findFirst()
                    .ifPresent(aplicacaoComboBox::setValue);
            }
            
            salvarButton.setText("Atualizar");
        }
    }

    @FXML
    protected void onExcluirClick() {
        HistoriaTableModel selected = historiaTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmar Exclusão");
            confirmAlert.setHeaderText("Deseja realmente excluir esta história?");
            confirmAlert.setContentText("História: " + selected.getTitulo());
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    service.delete(selected.getId());
                    showAlert("Sucesso", "História excluída com sucesso!");
                    loadHistorias();
                } catch (Exception e) {
                    showErrorAlert("Erro", "Erro ao excluir história: " + e.getMessage());
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
    protected void onNavigateToFeatures() {
        navigateTo("/br/tec/jessebezerra/app/feature-view.fxml", "Gerenciamento de Features");
    }
    
    @FXML
    protected void onNavigateToHistorias() {
        // Já estamos na tela de histórias
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
        featureComboBox.setItems(FXCollections.observableArrayList(features));
    }
    
    private void loadFeaturesBySprint(Long sprintId) {
        List<ItemSprintDTO> features = service.findBySprintId(sprintId).stream()
            .filter(item -> item.getTipo() == TipoItem.FEATURE)
            .toList();
        featureComboBox.setItems(FXCollections.observableArrayList(features));
    }
    
    private void loadProjetos() {
        List<ProjetoDTO> projetos = projetoService.findAll();
        projetoComboBox.setItems(FXCollections.observableArrayList(projetos));
    }
    
    private void loadAplicacoes() {
        List<AplicacaoDTO> aplicacoes = aplicacaoService.findAll();
        aplicacaoComboBox.setItems(FXCollections.observableArrayList(aplicacoes));
    }

    private void loadHistorias() {
        List<ItemSprintDTO> historias = service.findAll().stream()
            .filter(item -> item.getTipo() == TipoItem.HISTORIA)
            .toList();
        
        historiaList.clear();
        historias.forEach(dto -> {
            HistoriaTableModel model = new HistoriaTableModel(
                dto.getId(),
                dto.getTitulo(),
                dto.getDescricao(),
                dto.getDuracaoSemanas(),
                dto.getStatus(),
                dto.getSprintId(),
                dto.getSprintNome(),
                dto.getItemPaiId(),
                dto.getItemPaiTitulo(),
                dto.getMembroId(),
                dto.getMembroNome(),
                dto.getProjetoId(),
                dto.getProjetoNome(),
                dto.getAplicacaoId(),
                dto.getAplicacaoNome()
            );
            historiaList.add(model);
        });
        
        historiaTable.refresh();
    }

    private void clearForm() {
        editingId = null;
        tituloField.clear();
        descricaoTextArea.clear();
        duracaoSpinner.getValueFactory().setValue(1);
        statusComboBox.setValue(StatusItem.CRIADO);
        sprintComboBox.setValue(null);
        featureComboBox.setValue(null);
        membroComboBox.setValue(null);
        projetoComboBox.setValue(null);
        aplicacaoComboBox.setValue(null);
        salvarButton.setText("Salvar");
        historiaTable.getSelectionModel().clearSelection();
    }

    private boolean validateFields() {
        return tituloField.getText() != null && !tituloField.getText().trim().isEmpty()
            && statusComboBox.getValue() != null
            && sprintComboBox.getValue() != null
            && featureComboBox.getValue() != null
            && duracaoSpinner.getValue() != null && duracaoSpinner.getValue() > 0;
    }
    
    @FXML
    protected void onGerenciarSubsClick() {
        HistoriaTableModel selected = historiaTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorAlert("Erro", "Selecione uma história primeiro.");
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

    public static class HistoriaTableModel {
        private final SimpleLongProperty id;
        private final SimpleStringProperty titulo;
        private final SimpleStringProperty descricao;
        private final SimpleIntegerProperty duracaoSemanas;
        private final SimpleObjectProperty<StatusItem> status;
        private final SimpleLongProperty sprintId;
        private final SimpleStringProperty sprintNome;
        private final SimpleObjectProperty<Long> featureId;
        private final SimpleStringProperty featureTitulo;
        private final SimpleObjectProperty<Long> membroId;
        private final SimpleStringProperty membroNome;
        private final SimpleObjectProperty<Long> projetoId;
        private final SimpleStringProperty projetoNome;
        private final SimpleObjectProperty<Long> aplicacaoId;
        private final SimpleStringProperty aplicacaoNome;

        public HistoriaTableModel(Long id, String titulo, String descricao, Integer duracaoSemanas,
                                 StatusItem status, Long sprintId, String sprintNome,
                                 Long featureId, String featureTitulo,
                                 Long membroId, String membroNome,
                                 Long projetoId, String projetoNome,
                                 Long aplicacaoId, String aplicacaoNome) {
            this.id = new SimpleLongProperty(id);
            this.titulo = new SimpleStringProperty(titulo);
            this.descricao = new SimpleStringProperty(descricao);
            this.duracaoSemanas = new SimpleIntegerProperty(duracaoSemanas != null ? duracaoSemanas : 0);
            this.status = new SimpleObjectProperty<>(status);
            this.sprintId = new SimpleLongProperty(sprintId);
            this.sprintNome = new SimpleStringProperty(sprintNome);
            this.featureId = new SimpleObjectProperty<>(featureId);
            this.featureTitulo = new SimpleStringProperty(featureTitulo);
            this.membroId = new SimpleObjectProperty<>(membroId);
            this.membroNome = new SimpleStringProperty(membroNome);
            this.projetoId = new SimpleObjectProperty<>(projetoId);
            this.projetoNome = new SimpleStringProperty(projetoNome);
            this.aplicacaoId = new SimpleObjectProperty<>(aplicacaoId);
            this.aplicacaoNome = new SimpleStringProperty(aplicacaoNome);
        }

        public long getId() { return id.get(); }
        public SimpleLongProperty idProperty() { return id; }
        
        public String getTitulo() { return titulo.get(); }
        public SimpleStringProperty tituloProperty() { return titulo; }
        
        public String getDescricao() { return descricao.get(); }
        
        public Integer getDuracaoSemanas() { return duracaoSemanas.get(); }
        public SimpleIntegerProperty duracaoSemanasProperty() { return duracaoSemanas; }
        
        public StatusItem getStatus() { return status.get(); }
        public SimpleObjectProperty<StatusItem> statusProperty() { return status; }
        
        public Long getSprintId() { return sprintId.get(); }
        
        public String getSprintNome() { return sprintNome.get(); }
        public SimpleStringProperty sprintNomeProperty() { return sprintNome; }
        
        public Long getFeatureId() { return featureId.get(); }
        
        public String getFeatureTitulo() { return featureTitulo.get(); }
        public SimpleStringProperty featureTituloProperty() { return featureTitulo; }
        
        public Long getMembroId() { return membroId.get(); }
        
        public String getMembroNome() { return membroNome.get(); }
        public SimpleStringProperty membroNomeProperty() { return membroNome; }
        
        public Long getProjetoId() { return projetoId.get(); }
        
        public String getProjetoNome() { return projetoNome.get(); }
        public SimpleStringProperty projetoNomeProperty() { return projetoNome; }
        
        public Long getAplicacaoId() { return aplicacaoId.get(); }
        
        public String getAplicacaoNome() { return aplicacaoNome.get(); }
        public SimpleStringProperty aplicacaoNomeProperty() { return aplicacaoNome; }
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
        duracaoSpinner.getValueFactory().setValue(item.getDuracaoSemanas() != null ? item.getDuracaoSemanas() : 1);
        statusComboBox.setValue(item.getStatus());
        
        // Selecionar Sprint
        if (item.getSprintId() != null) {
            sprintComboBox.getItems().stream()
                .filter(s -> s.getId().equals(item.getSprintId()))
                .findFirst()
                .ifPresent(sprintComboBox::setValue);
        }
        
        // Selecionar Feature
        if (item.getItemPaiId() != null) {
            featureComboBox.getItems().stream()
                .filter(f -> f.getId().equals(item.getItemPaiId()))
                .findFirst()
                .ifPresent(featureComboBox::setValue);
        }
        
        // Selecionar Membro
        if (item.getMembroId() != null) {
            membroComboBox.getItems().stream()
                .filter(m -> m.getId().equals(item.getMembroId()))
                .findFirst()
                .ifPresent(membroComboBox::setValue);
        }
        
        // Selecionar Projeto
        if (item.getProjetoId() != null) {
            projetoComboBox.getItems().stream()
                .filter(p -> p.getId().equals(item.getProjetoId()))
                .findFirst()
                .ifPresent(projetoComboBox::setValue);
        }
        
        // Selecionar Aplicação
        if (item.getAplicacaoId() != null) {
            aplicacaoComboBox.getItems().stream()
                .filter(a -> a.getId().equals(item.getAplicacaoId()))
                .findFirst()
                .ifPresent(aplicacaoComboBox::setValue);
        }
    }
    
    @Override
    protected javafx.stage.Stage getCurrentStage() {
        return (javafx.stage.Stage) historiaTable.getScene().getWindow();
    }
}
