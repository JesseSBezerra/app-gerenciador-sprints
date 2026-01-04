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

public class ItemSprintController extends BaseController {
    
    @FXML private ComboBox<TipoItem> tipoComboBox;
    @FXML private TextField tituloField;
    @FXML private TextArea descricaoTextArea;
    @FXML private Spinner<Integer> duracaoSpinner;
    @FXML private ComboBox<StatusItem> statusComboBox;
    @FXML private ComboBox<SprintDTO> sprintComboBox;
    @FXML private ComboBox<MembroDTO> membroComboBox;
    @FXML private ComboBox<ItemSprintDTO> itemPaiComboBox;
    
    @FXML private TableView<ItemSprintTableModel> itemTable;
    @FXML private TableColumn<ItemSprintTableModel, Long> idColumn;
    @FXML private TableColumn<ItemSprintTableModel, TipoItem> tipoColumn;
    @FXML private TableColumn<ItemSprintTableModel, String> tituloColumn;
    @FXML private TableColumn<ItemSprintTableModel, Integer> duracaoColumn;
    @FXML private TableColumn<ItemSprintTableModel, StatusItem> statusColumn;
    @FXML private TableColumn<ItemSprintTableModel, String> sprintColumn;
    @FXML private TableColumn<ItemSprintTableModel, String> membroColumn;
    
    @FXML private Button salvarButton;
    @FXML private Button limparButton;
    @FXML private Button editarButton;
    @FXML private Button excluirButton;
    @FXML private Button menuToggleButton;
    @FXML private VBox sideMenu;
    
    private final ItemSprintService service;
    private final SprintService sprintService;
    private final MembroService membroService;
    private boolean menuExpanded = true;
    private final ObservableList<ItemSprintTableModel> itemList;
    private Long editingId;

    public ItemSprintController() {
        this.service = new ItemSprintService();
        this.sprintService = new SprintService();
        this.membroService = new MembroService();
        this.itemList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        // Configurar ComboBoxes
        tipoComboBox.setItems(FXCollections.observableArrayList(TipoItem.values()));
        statusComboBox.setItems(FXCollections.observableArrayList(StatusItem.values()));
        statusComboBox.setValue(StatusItem.CRIADO);
        
        // Configurar Spinner
        SpinnerValueFactory<Integer> valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        duracaoSpinner.setValueFactory(valueFactory);
        
        // Carregar dados nos ComboBoxes
        loadSprints();
        loadMembros();
        
        // Configurar colunas da tabela
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        tipoColumn.setCellValueFactory(cellData -> cellData.getValue().tipoProperty());
        tituloColumn.setCellValueFactory(cellData -> cellData.getValue().tituloProperty());
        duracaoColumn.setCellValueFactory(cellData -> cellData.getValue().duracaoDiasProperty().asObject());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        sprintColumn.setCellValueFactory(cellData -> cellData.getValue().sprintNomeProperty());
        membroColumn.setCellValueFactory(cellData -> cellData.getValue().membroNomeProperty());
        
        itemTable.setItems(itemList);
        
        itemTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                editarButton.setDisable(newSelection == null);
                excluirButton.setDisable(newSelection == null);
            }
        );
        
        // Listener para atualizar itens pai quando tipo ou sprint mudar
        tipoComboBox.valueProperty().addListener((obs, oldVal, newVal) -> loadItensPai());
        sprintComboBox.valueProperty().addListener((obs, oldVal, newVal) -> loadItensPai());
        
        loadItens();
    }

    @FXML
    protected void onSalvarClick() {
        if (!validateFields()) {
            showErrorAlert("Erro de Validação", "Por favor, preencha todos os campos obrigatórios.");
            return;
        }
        
        ItemSprintDTO dto = new ItemSprintDTO();
        dto.setId(editingId);
        dto.setTipo(tipoComboBox.getValue());
        dto.setTitulo(tituloField.getText());
        dto.setDescricao(descricaoTextArea.getText());
        dto.setDuracaoDias(duracaoSpinner.getValue());
        dto.setStatus(statusComboBox.getValue());
        dto.setSprintId(sprintComboBox.getValue().getId());
        
        if (membroComboBox.getValue() != null) {
            dto.setMembroId(membroComboBox.getValue().getId());
        }
        
        if (itemPaiComboBox.getValue() != null) {
            dto.setItemPaiId(itemPaiComboBox.getValue().getId());
        }
        
        try {
            if (editingId == null) {
                service.create(dto);
                showAlert("Sucesso", "Item criado com sucesso!");
            } else {
                service.update(dto);
                showAlert("Sucesso", "Item atualizado com sucesso!");
            }
            clearForm();
            loadItens();
        } catch (IllegalArgumentException e) {
            showErrorAlert("Validação", e.getMessage());
        } catch (Exception e) {
            showErrorAlert("Erro", "Erro ao salvar item: " + e.getMessage());
        }
    }

    @FXML
    protected void onLimparClick() {
        clearForm();
    }

    @FXML
    protected void onEditarClick() {
        ItemSprintTableModel selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            editingId = selected.getId();
            tipoComboBox.setValue(selected.getTipo());
            tituloField.setText(selected.getTitulo());
            descricaoTextArea.setText(selected.getDescricao());
            duracaoSpinner.getValueFactory().setValue(selected.getDuracaoDias());
            statusComboBox.setValue(selected.getStatus());
            
            // Selecionar sprint
            sprintComboBox.getItems().stream()
                .filter(s -> s.getId().equals(selected.getSprintId()))
                .findFirst()
                .ifPresent(sprintComboBox::setValue);
            
            // Selecionar membro
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
        ItemSprintTableModel selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmar Exclusão");
            confirmAlert.setHeaderText("Deseja realmente excluir este item?");
            confirmAlert.setContentText("Item: " + selected.getTitulo());
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    service.delete(selected.getId());
                    showAlert("Sucesso", "Item excluído com sucesso!");
                    loadItens();
                } catch (Exception e) {
                    showErrorAlert("Erro", "Erro ao excluir item: " + e.getMessage());
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
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) tituloField.getScene().getWindow();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/br/tec/jessebezerra/app/sprint-view.fxml")
            );
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load(), currentWidth, currentHeight);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erro", "Erro ao navegar para tela de Sprints: " + e.getMessage());
        }
    }
    
    @FXML
    protected void onNavigateToEquipe() {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) tituloField.getScene().getWindow();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/br/tec/jessebezerra/app/membro-view.fxml")
            );
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load(), currentWidth, currentHeight);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erro", "Erro ao navegar para tela de Equipe: " + e.getMessage());
        }
    }

    private void loadSprints() {
        List<SprintDTO> sprints = sprintService.findAll();
        sprintComboBox.setItems(FXCollections.observableArrayList(sprints));
    }

    private void loadMembros() {
        List<MembroDTO> membros = membroService.findAll();
        membroComboBox.setItems(FXCollections.observableArrayList(membros));
    }

    private void loadItensPai() {
        if (tipoComboBox.getValue() != null && sprintComboBox.getValue() != null) {
            List<ItemSprintDTO> itensPossiveis = service.findBySprintId(sprintComboBox.getValue().getId())
                .stream()
                .filter(item -> tipoComboBox.getValue().podeSerFilhoDe(item.getTipo()))
                .toList();
            
            itemPaiComboBox.setItems(FXCollections.observableArrayList(itensPossiveis));
        }
    }

    private void loadItens() {
        List<ItemSprintDTO> itens = service.findAll();
        
        itemList.clear();
        itens.forEach(dto -> {
            ItemSprintTableModel model = new ItemSprintTableModel(
                dto.getId(),
                dto.getTipo(),
                dto.getTitulo(),
                dto.getDescricao(),
                dto.getDuracaoDias(),
                dto.getStatus(),
                dto.getSprintId(),
                dto.getSprintNome(),
                dto.getMembroId(),
                dto.getMembroNome()
            );
            itemList.add(model);
        });
        
        itemTable.refresh();
    }

    private void clearForm() {
        editingId = null;
        tipoComboBox.setValue(null);
        tituloField.clear();
        descricaoTextArea.clear();
        duracaoSpinner.getValueFactory().setValue(1);
        statusComboBox.setValue(StatusItem.CRIADO);
        sprintComboBox.setValue(null);
        membroComboBox.setValue(null);
        itemPaiComboBox.setValue(null);
        salvarButton.setText("Salvar");
        itemTable.getSelectionModel().clearSelection();
    }

    private boolean validateFields() {
        return tituloField.getText() != null && !tituloField.getText().trim().isEmpty()
            && tipoComboBox.getValue() != null
            && statusComboBox.getValue() != null
            && sprintComboBox.getValue() != null
            && duracaoSpinner.getValue() != null && duracaoSpinner.getValue() > 0;
    }

    public static class ItemSprintTableModel {
        private final SimpleLongProperty id;
        private final SimpleObjectProperty<TipoItem> tipo;
        private final SimpleStringProperty titulo;
        private final SimpleStringProperty descricao;
        private final SimpleIntegerProperty duracaoDias;
        private final SimpleObjectProperty<StatusItem> status;
        private final SimpleLongProperty sprintId;
        private final SimpleStringProperty sprintNome;
        private final SimpleObjectProperty<Long> membroId;
        private final SimpleStringProperty membroNome;

        public ItemSprintTableModel(Long id, TipoItem tipo, String titulo, String descricao,
                                   Integer duracaoDias, StatusItem status, Long sprintId, String sprintNome,
                                   Long membroId, String membroNome) {
            this.id = new SimpleLongProperty(id);
            this.tipo = new SimpleObjectProperty<>(tipo);
            this.titulo = new SimpleStringProperty(titulo);
            this.descricao = new SimpleStringProperty(descricao);
            this.duracaoDias = new SimpleIntegerProperty(duracaoDias);
            this.status = new SimpleObjectProperty<>(status);
            this.sprintId = new SimpleLongProperty(sprintId);
            this.sprintNome = new SimpleStringProperty(sprintNome);
            this.membroId = new SimpleObjectProperty<>(membroId);
            this.membroNome = new SimpleStringProperty(membroNome);
        }

        public long getId() { return id.get(); }
        public SimpleLongProperty idProperty() { return id; }
        
        public TipoItem getTipo() { return tipo.get(); }
        public SimpleObjectProperty<TipoItem> tipoProperty() { return tipo; }
        
        public String getTitulo() { return titulo.get(); }
        public SimpleStringProperty tituloProperty() { return titulo; }
        
        public String getDescricao() { return descricao.get(); }
        public SimpleStringProperty descricaoProperty() { return descricao; }
        
        public Integer getDuracaoDias() { return duracaoDias.get(); }
        public SimpleIntegerProperty duracaoDiasProperty() { return duracaoDias; }
        
        public StatusItem getStatus() { return status.get(); }
        public SimpleObjectProperty<StatusItem> statusProperty() { return status; }
        
        public Long getSprintId() { return sprintId.get(); }
        public SimpleLongProperty sprintIdProperty() { return sprintId; }
        
        public String getSprintNome() { return sprintNome.get(); }
        public SimpleStringProperty sprintNomeProperty() { return sprintNome; }
        
        public Long getMembroId() { return membroId.get(); }
        public SimpleObjectProperty<Long> membroIdProperty() { return membroId; }
        
        public String getMembroNome() { return membroNome.get(); }
        public SimpleStringProperty membroNomeProperty() { return membroNome; }
    }
    
    @Override
    protected javafx.stage.Stage getCurrentStage() {
        return (javafx.stage.Stage) itemTable.getScene().getWindow();
    }
}
