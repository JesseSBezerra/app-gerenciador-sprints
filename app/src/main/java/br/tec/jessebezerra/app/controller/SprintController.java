package br.tec.jessebezerra.app.controller;

import br.tec.jessebezerra.app.dto.SprintDTO;
import br.tec.jessebezerra.app.service.SprintService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class SprintController extends BaseController {
    
    @FXML
    private TextField nomeField;
    
    @FXML
    private DatePicker dataInicioField;
    
    @FXML
    private Spinner<Integer> duracaoSpinner;
    
    @FXML
    private TableView<SprintTableModel> sprintTable;
    
    @FXML
    private TableColumn<SprintTableModel, Long> idColumn;
    
    @FXML
    private TableColumn<SprintTableModel, String> nomeColumn;
    
    @FXML
    private TableColumn<SprintTableModel, LocalDate> dataInicioColumn;
    
    @FXML
    private TableColumn<SprintTableModel, Integer> duracaoColumn;
    
    @FXML
    private TableColumn<SprintTableModel, LocalDate> dataFimColumn;
    
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
    
    private final SprintService service;
    private boolean menuExpanded = true;
    private final ObservableList<SprintTableModel> sprintList;
    private Long editingId;

    public SprintController() {
        this.service = new SprintService();
        this.sprintList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        System.out.println("=== INITIALIZE CONTROLLER ===");
        System.out.println("sprintTable: " + sprintTable);
        System.out.println("idColumn: " + idColumn);
        System.out.println("nomeColumn: " + nomeColumn);
        System.out.println("dataInicioColumn: " + dataInicioColumn);
        System.out.println("duracaoColumn: " + duracaoColumn);
        
        SpinnerValueFactory<Integer> valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 52, 2);
        duracaoSpinner.setValueFactory(valueFactory);
        
        System.out.println("Configurando CellValueFactory...");
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        nomeColumn.setCellValueFactory(cellData -> cellData.getValue().nomeProperty());
        dataInicioColumn.setCellValueFactory(cellData -> cellData.getValue().dataInicioProperty());
        duracaoColumn.setCellValueFactory(cellData -> cellData.getValue().duracaoSemanasProperty().asObject());
        dataFimColumn.setCellValueFactory(cellData -> cellData.getValue().dataFimProperty());
        
        System.out.println("Vinculando ObservableList à tabela...");
        sprintTable.setItems(sprintList);
        System.out.println("sprintList size: " + sprintList.size());
        
        sprintTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                editarButton.setDisable(newSelection == null);
                excluirButton.setDisable(newSelection == null);
            }
        );
        
        System.out.println("Carregando sprints...");
        loadSprints();
        System.out.println("sprintList size após load: " + sprintList.size());
        
        // Verificar propriedades da tabela
        System.out.println("Tabela visível: " + sprintTable.isVisible());
        System.out.println("Tabela largura: " + sprintTable.getWidth());
        System.out.println("Tabela altura: " + sprintTable.getHeight());
        System.out.println("Colunas na tabela: " + sprintTable.getColumns().size());
        
        System.out.println("=== FIM INITIALIZE ===");
    }

    @FXML
    protected void onSalvarClick() {
        System.out.println("=== INICIANDO SALVAMENTO ===");
        System.out.println("Nome: " + nomeField.getText());
        System.out.println("Data Início: " + dataInicioField.getValue());
        System.out.println("Duração: " + duracaoSpinner.getValue());
        
        if (!validateFields()) {
            System.out.println("ERRO: Validação falhou");
            showAlert("Erro de Validação", "Por favor, preencha todos os campos corretamente.");
            return;
        }
        
        SprintDTO dto = new SprintDTO();
        dto.setId(editingId);
        dto.setNome(nomeField.getText());
        dto.setDataInicio(dataInicioField.getValue());
        dto.setDuracaoSemanas(duracaoSpinner.getValue());
        
        System.out.println("DTO criado: " + dto);
        
        try {
            if (editingId == null) {
                System.out.println("Criando nova sprint...");
                SprintDTO saved = service.create(dto);
                System.out.println("Sprint criada com ID: " + saved.getId());
                showAlert("Sucesso", "Sprint criada com sucesso!");
            } else {
                System.out.println("Atualizando sprint ID: " + editingId);
                service.update(dto);
                showAlert("Sucesso", "Sprint atualizada com sucesso!");
            }
            clearForm();
            loadSprints();
            System.out.println("=== SALVAMENTO CONCLUÍDO ===");
        } catch (IllegalArgumentException e) {
            System.err.println("ERRO de validação: " + e.getMessage());
            showErrorAlert("Período Inválido", e.getMessage());
        } catch (Exception e) {
            System.err.println("ERRO ao salvar sprint: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erro", "Erro ao salvar sprint: " + e.getMessage());
        }
    }

    @FXML
    protected void onLimparClick() {
        clearForm();
    }

    @FXML
    protected void onEditarClick() {
        SprintTableModel selected = sprintTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            editingId = selected.getId();
            nomeField.setText(selected.getNome());
            dataInicioField.setValue(selected.getDataInicio());
            duracaoSpinner.getValueFactory().setValue(selected.getDuracaoSemanas());
            salvarButton.setText("Atualizar");
        }
    }

    @FXML
    protected void onMenuToggle() {
        if (menuExpanded) {
            sideMenu.setPrefWidth(60);
            sideMenu.setMinWidth(60);
            sideMenu.setMaxWidth(60);
            menuToggleButton.setText("☰");
        } else {
            sideMenu.setPrefWidth(220);
            sideMenu.setMinWidth(220);
            sideMenu.setMaxWidth(220);
            menuToggleButton.setText("☰");
        }
        menuExpanded = !menuExpanded;
    }
    
    @FXML
    protected void onNavigateToEquipe() {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) nomeField.getScene().getWindow();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/br/tec/jessebezerra/app/membro-view.fxml")
            );
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load(), currentWidth, currentHeight);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erro", "Erro ao navegar para tela de Equipe: " + e.getMessage());
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
        SprintTableModel selected = sprintTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmar Exclusão");
            confirmAlert.setHeaderText("Deseja realmente excluir esta sprint?");
            confirmAlert.setContentText("Sprint: " + selected.getNome());
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    service.delete(selected.getId());
                    showAlert("Sucesso", "Sprint excluída com sucesso!");
                    clearForm();
                    loadSprints();
                } catch (Exception e) {
                    showAlert("Erro", "Erro ao excluir sprint: " + e.getMessage());
                }
            }
        }
    }

    private void loadSprints() {
        System.out.println("=== CARREGANDO SPRINTS ===");
        
        List<SprintDTO> sprints = service.findAll();
        System.out.println("Total de sprints encontradas: " + sprints.size());
        
        // Criar nova lista temporária
        ObservableList<SprintTableModel> tempList = FXCollections.observableArrayList();
        
        sprints.forEach(dto -> {
            System.out.println("Adicionando sprint: " + dto);
            SprintTableModel model = new SprintTableModel(
                dto.getId(),
                dto.getNome(),
                dto.getDataInicio(),
                dto.getDuracaoSemanas(),
                dto.getDataFim()
            );
            tempList.add(model);
            System.out.println("Modelo criado - ID: " + model.getId() + ", Nome: " + model.getNome() + ", Data Fim: " + model.getDataFim());
        });
        
        System.out.println("Total na lista temporária: " + tempList.size());
        
        // Limpar e adicionar tudo de uma vez
        sprintList.clear();
        sprintList.addAll(tempList);
        
        System.out.println("Total na sprintList: " + sprintList.size());
        
        // Reatribuir à tabela
        sprintTable.setItems(null);
        sprintTable.setItems(sprintList);
        
        // Forçar refresh da tabela
        sprintTable.refresh();
        System.out.println("Tabela atualizada (refresh)");
        
        // Verificar se os itens estão realmente na tabela
        System.out.println("Itens na tabela após refresh: " + sprintTable.getItems().size());
        
        System.out.println("=== FIM CARREGAMENTO ===");
    }

    private void clearForm() {
        editingId = null;
        nomeField.clear();
        dataInicioField.setValue(null);
        duracaoSpinner.getValueFactory().setValue(2);
        salvarButton.setText("Salvar");
        sprintTable.getSelectionModel().clearSelection();
    }

    private boolean validateFields() {
        return nomeField.getText() != null && !nomeField.getText().trim().isEmpty()
            && dataInicioField.getValue() != null
            && duracaoSpinner.getValue() != null && duracaoSpinner.getValue() > 0;
    }

    public static class SprintTableModel {
        private final SimpleLongProperty id;
        private final SimpleStringProperty nome;
        private final SimpleObjectProperty<LocalDate> dataInicio;
        private final SimpleIntegerProperty duracaoSemanas;
        private final SimpleObjectProperty<LocalDate> dataFim;

        public SprintTableModel(Long id, String nome, LocalDate dataInicio, Integer duracaoSemanas, LocalDate dataFim) {
            this.id = new SimpleLongProperty(id);
            this.nome = new SimpleStringProperty(nome);
            this.dataInicio = new SimpleObjectProperty<>(dataInicio);
            this.duracaoSemanas = new SimpleIntegerProperty(duracaoSemanas);
            this.dataFim = new SimpleObjectProperty<>(dataFim);
        }

        public long getId() { return id.get(); }
        public SimpleLongProperty idProperty() { return id; }
        
        public String getNome() { return nome.get(); }
        public SimpleStringProperty nomeProperty() { return nome; }
        
        public LocalDate getDataInicio() { return dataInicio.get(); }
        public SimpleObjectProperty<LocalDate> dataInicioProperty() { return dataInicio; }
        
        public int getDuracaoSemanas() { return duracaoSemanas.get(); }
        public SimpleIntegerProperty duracaoSemanasProperty() { return duracaoSemanas; }
        
        public LocalDate getDataFim() { return dataFim.get(); }
        public SimpleObjectProperty<LocalDate> dataFimProperty() { return dataFim; }
    }
    
    @Override
    protected javafx.stage.Stage getCurrentStage() {
        return (javafx.stage.Stage) sprintTable.getScene().getWindow();
    }
}
