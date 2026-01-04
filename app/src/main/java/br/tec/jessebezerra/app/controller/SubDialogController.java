package br.tec.jessebezerra.app.controller;

import br.tec.jessebezerra.app.dto.ItemSprintDTO;
import br.tec.jessebezerra.app.dto.MembroDTO;
import br.tec.jessebezerra.app.entity.StatusItem;
import br.tec.jessebezerra.app.entity.TipoItem;
import br.tec.jessebezerra.app.service.ItemSprintService;
import br.tec.jessebezerra.app.service.MembroService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.Optional;

public class SubDialogController {
    
    @FXML private Label historiaTitleLabel;
    @FXML private TextField tituloField;
    @FXML private TextArea descricaoTextArea;
    @FXML private Spinner<Integer> duracaoSpinner;
    @FXML private ComboBox<StatusItem> statusComboBox;
    @FXML private ComboBox<MembroDTO> membroComboBox;
    
    @FXML private TableView<SubTableModel> subTable;
    @FXML private TableColumn<SubTableModel, Long> idColumn;
    @FXML private TableColumn<SubTableModel, String> tituloColumn;
    @FXML private TableColumn<SubTableModel, Integer> duracaoColumn;
    @FXML private TableColumn<SubTableModel, StatusItem> statusColumn;
    @FXML private TableColumn<SubTableModel, String> membroColumn;
    
    @FXML private Button salvarButton;
    @FXML private Button editarButton;
    @FXML private Button excluirButton;
    
    private final ItemSprintService service;
    private final MembroService membroService;
    private final ObservableList<SubTableModel> subList;
    private Long historiaId;
    private String historiaTitulo;
    private Integer historiaDuracaoSemanas;
    private Long sprintId;
    private Long editingId;

    public SubDialogController() {
        this.service = new ItemSprintService();
        this.membroService = new MembroService();
        this.subList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        statusComboBox.setItems(FXCollections.observableArrayList(StatusItem.values()));
        statusComboBox.setValue(StatusItem.CRIADO);
        
        SpinnerValueFactory<Integer> valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 25, 1);
        duracaoSpinner.setValueFactory(valueFactory);
        
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        tituloColumn.setCellValueFactory(cellData -> cellData.getValue().tituloProperty());
        duracaoColumn.setCellValueFactory(cellData -> cellData.getValue().duracaoDiasProperty().asObject());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        membroColumn.setCellValueFactory(cellData -> cellData.getValue().membroNomeProperty());
        
        subTable.setItems(subList);
        
        subTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                editarButton.setDisable(newSelection == null);
                excluirButton.setDisable(newSelection == null);
            }
        );
        
        loadMembros();
    }
    
    public void setHistoria(Long historiaId, String historiaTitulo, Integer historiaDuracaoSemanas, Long sprintId) {
        this.historiaId = historiaId;
        this.historiaTitulo = historiaTitulo;
        this.historiaDuracaoSemanas = historiaDuracaoSemanas;
        this.sprintId = sprintId;
        
        historiaTitleLabel.setText("SUBs da História: " + historiaTitulo + 
            " (Duração: " + historiaDuracaoSemanas + " semanas = " + (historiaDuracaoSemanas * 5) + " dias)");
        
        loadSubs();
    }

    @FXML
    protected void onSalvarClick() {
        if (!validateFields()) {
            showErrorAlert("Erro de Validação", "Por favor, preencha todos os campos obrigatórios.");
            return;
        }
        
        // Validar que a SUB não ultrapassa a duração da História
        int duracaoHistoriaEmDias = historiaDuracaoSemanas * 5;
        if (duracaoSpinner.getValue() > duracaoHistoriaEmDias) {
            showErrorAlert("Validação", 
                String.format("A duração da SUB (%d dias) não pode ultrapassar a duração da História (%d dias).",
                    duracaoSpinner.getValue(), duracaoHistoriaEmDias));
            return;
        }
        
        ItemSprintDTO dto = new ItemSprintDTO();
        dto.setId(editingId);
        dto.setTipo(TipoItem.SUB);
        dto.setTitulo(tituloField.getText());
        dto.setDescricao(descricaoTextArea.getText());
        dto.setDuracaoDias(duracaoSpinner.getValue());
        dto.setStatus(statusComboBox.getValue());
        dto.setSprintId(sprintId);
        dto.setItemPaiId(historiaId);
        
        if (membroComboBox.getValue() != null) {
            dto.setMembroId(membroComboBox.getValue().getId());
        }
        
        try {
            if (editingId == null) {
                service.create(dto);
                showAlert("Sucesso", "SUB criada com sucesso!");
            } else {
                service.update(dto);
                showAlert("Sucesso", "SUB atualizada com sucesso!");
            }
            clearForm();
            loadSubs();
        } catch (IllegalArgumentException e) {
            showErrorAlert("Validação", e.getMessage());
        } catch (Exception e) {
            showErrorAlert("Erro", "Erro ao salvar SUB: " + e.getMessage());
        }
    }

    @FXML
    protected void onLimparClick() {
        clearForm();
    }

    @FXML
    protected void onEditarClick() {
        SubTableModel selected = subTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            editingId = selected.getId();
            tituloField.setText(selected.getTitulo());
            descricaoTextArea.setText(selected.getDescricao());
            duracaoSpinner.getValueFactory().setValue(selected.getDuracaoDias());
            statusComboBox.setValue(selected.getStatus());
            
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
        SubTableModel selected = subTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmar Exclusão");
            confirmAlert.setHeaderText("Deseja realmente excluir esta SUB?");
            confirmAlert.setContentText("SUB: " + selected.getTitulo());
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    service.delete(selected.getId());
                    showAlert("Sucesso", "SUB excluída com sucesso!");
                    loadSubs();
                } catch (Exception e) {
                    showErrorAlert("Erro", "Erro ao excluir SUB: " + e.getMessage());
                }
            }
        }
    }
    
    @FXML
    protected void onFecharClick() {
        ((javafx.stage.Stage) tituloField.getScene().getWindow()).close();
    }

    private void loadMembros() {
        List<MembroDTO> membros = membroService.findAll();
        membroComboBox.setItems(FXCollections.observableArrayList(membros));
    }

    private void loadSubs() {
        List<ItemSprintDTO> subs = service.findByItemPaiId(historiaId).stream()
            .filter(item -> item.getTipo() == TipoItem.SUB)
            .toList();
        
        subList.clear();
        subs.forEach(dto -> {
            SubTableModel model = new SubTableModel(
                dto.getId(),
                dto.getTitulo(),
                dto.getDescricao(),
                dto.getDuracaoDias(),
                dto.getStatus(),
                dto.getMembroId(),
                dto.getMembroNome()
            );
            subList.add(model);
        });
        
        subTable.refresh();
    }

    private void clearForm() {
        editingId = null;
        tituloField.clear();
        descricaoTextArea.clear();
        duracaoSpinner.getValueFactory().setValue(1);
        statusComboBox.setValue(StatusItem.CRIADO);
        membroComboBox.setValue(null);
        salvarButton.setText("Salvar SUB");
        subTable.getSelectionModel().clearSelection();
    }

    private boolean validateFields() {
        return tituloField.getText() != null && !tituloField.getText().trim().isEmpty()
            && statusComboBox.getValue() != null
            && duracaoSpinner.getValue() != null && duracaoSpinner.getValue() > 0;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class SubTableModel {
        private final SimpleLongProperty id;
        private final SimpleStringProperty titulo;
        private final SimpleStringProperty descricao;
        private final SimpleIntegerProperty duracaoDias;
        private final SimpleObjectProperty<StatusItem> status;
        private final SimpleObjectProperty<Long> membroId;
        private final SimpleStringProperty membroNome;

        public SubTableModel(Long id, String titulo, String descricao, Integer duracaoDias,
                            StatusItem status, Long membroId, String membroNome) {
            this.id = new SimpleLongProperty(id);
            this.titulo = new SimpleStringProperty(titulo);
            this.descricao = new SimpleStringProperty(descricao);
            this.duracaoDias = new SimpleIntegerProperty(duracaoDias != null ? duracaoDias : 0);
            this.status = new SimpleObjectProperty<>(status);
            this.membroId = new SimpleObjectProperty<>(membroId);
            this.membroNome = new SimpleStringProperty(membroNome);
        }

        public long getId() { return id.get(); }
        public SimpleLongProperty idProperty() { return id; }
        
        public String getTitulo() { return titulo.get(); }
        public SimpleStringProperty tituloProperty() { return titulo; }
        
        public String getDescricao() { return descricao.get(); }
        
        public Integer getDuracaoDias() { return duracaoDias.get(); }
        public SimpleIntegerProperty duracaoDiasProperty() { return duracaoDias; }
        
        public StatusItem getStatus() { return status.get(); }
        public SimpleObjectProperty<StatusItem> statusProperty() { return status; }
        
        public Long getMembroId() { return membroId.get(); }
        
        public String getMembroNome() { return membroNome.get(); }
        public SimpleStringProperty membroNomeProperty() { return membroNome; }
    }
}
