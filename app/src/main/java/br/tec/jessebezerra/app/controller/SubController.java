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
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

public class SubController extends BaseController {
    
    @FXML private TextField tituloField;
    @FXML private TextField codigoExternoField;
    @FXML private TextArea descricaoTextArea;
    @FXML private Spinner<Integer> duracaoSpinner;
    @FXML private Spinner<Integer> prioridadeSpinner;
    @FXML private ComboBox<StatusItem> statusComboBox;
    @FXML private ComboBox<SprintDTO> sprintComboBox;
    @FXML private ComboBox<MembroDTO> membroComboBox;
    @FXML private ComboBox<ItemSprintDTO> itemPaiComboBox;
    @FXML private ComboBox<ProjetoDTO> projetoComboBox;
    @FXML private ComboBox<AplicacaoDTO> aplicacaoComboBox;
    @FXML private Button salvarButton;
    
    private ItemSprintService service;
    private SprintService sprintService;
    private MembroService membroService;
    private ProjetoService projetoService;
    private AplicacaoService aplicacaoService;
    
    private Long editingId;
    
    @FXML
    public void initialize() {
        service = new ItemSprintService();
        sprintService = new SprintService();
        membroService = new MembroService();
        projetoService = new ProjetoService();
        aplicacaoService = new AplicacaoService();
        
        setupSpinners();
        loadComboBoxes();
    }
    
    private void setupSpinners() {
        SpinnerValueFactory<Integer> duracaoValueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        duracaoSpinner.setValueFactory(duracaoValueFactory);
        
        SpinnerValueFactory<Integer> prioridadeValueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 1);
        prioridadeSpinner.setValueFactory(prioridadeValueFactory);
    }
    
    private void loadComboBoxes() {
        // Carregar status
        statusComboBox.setItems(FXCollections.observableArrayList(StatusItem.values()));
        statusComboBox.setValue(StatusItem.CRIADO);
        
        // Carregar sprints
        List<SprintDTO> sprints = sprintService.findAll();
        sprintComboBox.setItems(FXCollections.observableArrayList(sprints));
        
        // Carregar membros
        List<MembroDTO> membros = membroService.findAll();
        membroComboBox.setItems(FXCollections.observableArrayList(membros));
        
        // Carregar projetos
        List<ProjetoDTO> projetos = projetoService.findAll();
        projetoComboBox.setItems(FXCollections.observableArrayList(projetos));
        
        // Carregar aplicações
        List<AplicacaoDTO> aplicacoes = aplicacaoService.findAll();
        aplicacaoComboBox.setItems(FXCollections.observableArrayList(aplicacoes));
        
        // Carregar itens pai (Histórias e Tarefas)
        List<ItemSprintDTO> itensPai = service.findAll().stream()
            .filter(item -> item.getTipo() == TipoItem.HISTORIA || item.getTipo() == TipoItem.TAREFA)
            .toList();
        itemPaiComboBox.setItems(FXCollections.observableArrayList(itensPai));
    }
    
    @FXML
    protected void onSalvarClick() {
        if (!validateFields()) {
            showErrorAlert("Erro de Validação", "Por favor, preencha todos os campos obrigatórios.");
            return;
        }
        
        ItemSprintDTO dto = new ItemSprintDTO();
        dto.setId(editingId);
        dto.setTipo(TipoItem.SUB);
        dto.setTitulo(tituloField.getText());
        dto.setDescricao(descricaoTextArea.getText());
        dto.setDuracaoDias(duracaoSpinner.getValue());
        dto.setStatus(statusComboBox.getValue());
        dto.setSprintId(sprintComboBox.getValue().getId());
        dto.setCodigoExterno(codigoExternoField.getText().trim().isEmpty() ? null : codigoExternoField.getText().trim());
        dto.setPrioridade(prioridadeSpinner.getValue());
        
        if (membroComboBox.getValue() != null) {
            dto.setMembroId(membroComboBox.getValue().getId());
        }
        
        if (itemPaiComboBox.getValue() != null) {
            dto.setItemPaiId(itemPaiComboBox.getValue().getId());
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
                showAlert("Sucesso", "SUB criada com sucesso!");
            } else {
                service.update(dto);
                showAlert("Sucesso", "SUB atualizada com sucesso!");
            }
            onFecharClick();
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
    protected void onFecharClick() {
        Stage stage = (Stage) salvarButton.getScene().getWindow();
        stage.close();
    }
    
    private void clearForm() {
        editingId = null;
        tituloField.clear();
        codigoExternoField.clear();
        descricaoTextArea.clear();
        duracaoSpinner.getValueFactory().setValue(1);
        prioridadeSpinner.getValueFactory().setValue(1);
        statusComboBox.setValue(StatusItem.CRIADO);
        sprintComboBox.setValue(null);
        itemPaiComboBox.setValue(null);
        membroComboBox.setValue(null);
        projetoComboBox.setValue(null);
        aplicacaoComboBox.setValue(null);
        salvarButton.setText("Salvar");
    }
    
    private boolean validateFields() {
        return tituloField.getText() != null && !tituloField.getText().trim().isEmpty()
            && statusComboBox.getValue() != null
            && sprintComboBox.getValue() != null
            && itemPaiComboBox.getValue() != null;
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
        codigoExternoField.setText(item.getCodigoExterno() != null ? item.getCodigoExterno() : "");
        descricaoTextArea.setText(item.getDescricao() != null ? item.getDescricao() : "");
        duracaoSpinner.getValueFactory().setValue(item.getDuracaoDias() != null ? item.getDuracaoDias() : 1);
        prioridadeSpinner.getValueFactory().setValue(item.getPrioridade() != null ? item.getPrioridade() : 1);
        statusComboBox.setValue(item.getStatus());
        
        // Selecionar Sprint
        if (item.getSprintId() != null) {
            sprintComboBox.getItems().stream()
                .filter(s -> s.getId().equals(item.getSprintId()))
                .findFirst()
                .ifPresent(sprintComboBox::setValue);
        }
        
        // Selecionar Item Pai
        if (item.getItemPaiId() != null) {
            itemPaiComboBox.getItems().stream()
                .filter(i -> i.getId().equals(item.getItemPaiId()))
                .findFirst()
                .ifPresent(itemPaiComboBox::setValue);
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
        
        salvarButton.setText("Atualizar");
    }
    
    @Override
    protected Stage getCurrentStage() {
        return (Stage) salvarButton.getScene().getWindow();
    }
}
