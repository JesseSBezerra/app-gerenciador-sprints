package br.tec.jessebezerra.app.controller;

import br.tec.jessebezerra.app.dto.ItemSprintDTO;
import br.tec.jessebezerra.app.dto.MembroDTO;
import br.tec.jessebezerra.app.dto.SprintDTO;
import br.tec.jessebezerra.app.entity.TipoItem;
import br.tec.jessebezerra.app.service.ItemSprintService;
import br.tec.jessebezerra.app.service.MembroService;
import br.tec.jessebezerra.app.service.SprintService;
import br.tec.jessebezerra.app.service.TimelineService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class TimelineController extends BaseController {
    
    @FXML
    private VBox sideMenu;
    
    @FXML
    private Button menuToggleButton;
    
    @FXML
    private VBox timelineContainer;
    
    @FXML
    private CheckBox showFeaturesCheckBox;
    
    @FXML
    private CheckBox showHistoriasCheckBox;
    
    @FXML
    private CheckBox showTarefasCheckBox;
    
    @FXML
    private ComboBox<MembroDTO> membroFilterComboBox;
    
    private SprintService sprintService;
    private TimelineService timelineService;
    private MembroService membroService;
    
    private boolean menuExpanded = true;
    private SprintDTO selectedSprint;
    private List<LocalDate> workingDays;
    
    private static final String[] COLORS = {
        "#FFB6C1", "#FFD700", "#98FB98", "#87CEEB", "#DDA0DD",
        "#F0E68C", "#E0BBE4", "#FFDAB9", "#B0E0E6", "#FFE4B5"
    };

    public TimelineController() {
        this.sprintService = new SprintService();
        this.timelineService = new TimelineService();
        this.membroService = new MembroService();
        this.workingDays = new ArrayList<>();
    }

    @FXML
    public void initialize() {
        loadMembros();
        loadAndBuildTimeline();
    }
    
    private void loadMembros() {
        List<MembroDTO> membros = membroService.findAll();
        membroFilterComboBox.setItems(FXCollections.observableArrayList(membros));
    }
    
    @FXML
    protected void onFilterChanged() {
        loadAndBuildTimeline();
    }
    
    @FXML
    protected void onClearMembroFilter() {
        membroFilterComboBox.setValue(null);
        loadAndBuildTimeline();
    }
    
    @FXML
    protected void onRefreshTimeline() {
        loadAndBuildTimeline();
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


    private void loadAndBuildTimeline() {
        List<SprintDTO> sprints = sprintService.findAll();
        if (!sprints.isEmpty()) {
            selectedSprint = sprints.get(0); // Carregar primeira sprint automaticamente
            calculateWorkingDays();
            buildTimeline();
        }
    }

    private void calculateWorkingDays() {
        workingDays = timelineService.calculateWorkingDays(selectedSprint);
    }

    private void buildTimeline() {
        timelineContainer.getChildren().clear();
        
        // Criar cabeçalho
        GridPane header = createHeader();
        timelineContainer.getChildren().add(header);
        
        // Buscar itens organizados hierarquicamente
        List<TimelineService.TimelineItem> timelineItems = timelineService.buildHierarchicalTimeline(selectedSprint.getId());
        
        // Aplicar filtros
        timelineItems = applyFilters(timelineItems);
        
        // Buscar todos os itens para cálculos
        List<ItemSprintDTO> allItems = new ArrayList<>();
        for (TimelineService.TimelineItem ti : timelineItems) {
            allItems.add(ti.getItem());
        }
        
        // Rastrear dias alocados por membro
        Map<Long, Integer> memberAllocations = new HashMap<>();
        
        int colorIndex = 0;
        String currentFeatureColor = COLORS[0]; // Cor padrão inicial
        
        for (TimelineService.TimelineItem timelineItem : timelineItems) {
            ItemSprintDTO item = timelineItem.getItem();
            int indentLevel = timelineItem.getIndentLevel();
            String displayType = timelineItem.getDisplayType();
            
            // Definir cor baseada no nível
            String rowColor;
            if (indentLevel == 0) {
                // Feature - atribuir nova cor
                currentFeatureColor = COLORS[colorIndex % COLORS.length];
                colorIndex++;
                rowColor = currentFeatureColor;
            } else if (indentLevel == 1) {
                // História - usar cor da Feature pai (ou cor padrão se Feature foi filtrada)
                rowColor = adjustBrightness(currentFeatureColor, 0.9);
            } else {
                // Tarefa/SUB - usar cor da Feature pai (ou cor padrão se Feature foi filtrada)
                rowColor = adjustBrightness(currentFeatureColor, 0.8);
            }
            
            // Calcular dia inicial e duração
            int startDay = 0;
            int customDuration = -1; // -1 significa usar duração padrão do item
            
            if (item.getTipo() == TipoItem.SUB) {
                // SUBs: alocação sequencial por membro
                if (item.getMembroId() != null) {
                    startDay = memberAllocations.getOrDefault(item.getMembroId(), 0);
                    int duration = timelineService.calculateDurationInDays(item);
                    memberAllocations.put(item.getMembroId(), startDay + duration);
                }
            } else if (item.getTipo() == TipoItem.HISTORIA || item.getTipo() == TipoItem.TAREFA) {
                // Histórias e Tarefas: começam no dia da primeira SUB e terminam no dia da última SUB
                startDay = timelineService.calculateParentStartDay(item.getId(), allItems, memberAllocations);
                customDuration = timelineService.calculateParentDuration(item.getId(), allItems, memberAllocations);
            }
            // Features sempre começam do dia 0
            
            GridPane row = createItemRow(item, indentLevel, rowColor, displayType, startDay, customDuration);
            timelineContainer.getChildren().add(row);
        }
    }

    private GridPane createHeader() {
        GridPane header = new GridPane();
        header.setStyle("-fx-background-color: #E8F4F8; -fx-padding: 0;");
        header.setHgap(0);
        header.setVgap(0);
        
        int totalDays = workingDays.size();
        
        // LINHA 0: "ROADMAP" mesclado até PESSOAS (rosa) + Nome da Sprint mesclado nas semanas (laranja)
        Label roadmapLabel = new Label("ROADMAP");
        roadmapLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold; -fx-font-size: 13px; " +
            "-fx-background-color: #FFB6D9; -fx-padding: 8; -fx-alignment: center; " +
            "-fx-border-color: transparent; -fx-border-width: 0;");
        roadmapLabel.setMinWidth(580);
        roadmapLabel.setMaxWidth(580);
        roadmapLabel.setMinHeight(35);
        roadmapLabel.setMaxHeight(35);
        roadmapLabel.setAlignment(Pos.CENTER);
        header.add(roadmapLabel, 0, 0, 3, 1);
        
        // Nome da Sprint mesclado sobre todas as semanas (laranja)
        Label sprintNameLabel = new Label(selectedSprint.getNome().toUpperCase());
        sprintNameLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold; -fx-font-size: 13px; " +
            "-fx-background-color: #FFD4A3; -fx-padding: 8; -fx-alignment: center; " +
            "-fx-border-color: transparent; -fx-border-width: 0;");
        sprintNameLabel.setMinWidth(totalDays * 32);
        sprintNameLabel.setMaxWidth(totalDays * 32);
        sprintNameLabel.setMinHeight(35);
        sprintNameLabel.setMaxHeight(35);
        sprintNameLabel.setAlignment(Pos.CENTER);
        header.add(sprintNameLabel, 3, 0, totalDays, 1);
        
        // LINHA 1: "ITENS DA SPRINT" mesclado (laranja claro) + Semanas
        Label itensSprintLabel = new Label("ITENS DA SPRINT");
        itensSprintLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold; -fx-font-size: 12px; " +
            "-fx-background-color: #FFD4A3; -fx-padding: 5; -fx-alignment: center;");
        itensSprintLabel.setMinWidth(430);
        itensSprintLabel.setMaxWidth(430);
        itensSprintLabel.setMinHeight(30);
        itensSprintLabel.setMaxHeight(30);
        itensSprintLabel.setAlignment(Pos.CENTER);
        header.add(itensSprintLabel, 0, 1, 2, 1);
        
        // "PESSOAS" mesclado (laranja claro)
        Label pessoasHeaderLabel = new Label("PESSOAS");
        pessoasHeaderLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold; -fx-font-size: 12px; " +
            "-fx-background-color: #FFD4A3; -fx-padding: 5; -fx-alignment: center;");
        pessoasHeaderLabel.setMinWidth(150);
        pessoasHeaderLabel.setMaxWidth(150);
        pessoasHeaderLabel.setMinHeight(30);
        pessoasHeaderLabel.setMaxHeight(30);
        pessoasHeaderLabel.setAlignment(Pos.CENTER);
        header.add(pessoasHeaderLabel, 2, 1);
        
        // Separar dias por semanas na LINHA 1
        String[] weekColors = {"#A8D5E2", "#7FB3D5", "#5499C7"}; // Azul claro ao escuro
        int colIndex = 3;
        int weekNumber = 1;
        int daysInCurrentWeek = 0;
        int weekStartCol = colIndex;
        
        for (int i = 0; i < workingDays.size(); i++) {
            LocalDate day = workingDays.get(i);
            daysInCurrentWeek++;
            
            boolean isEndOfWeek = day.getDayOfWeek() == DayOfWeek.FRIDAY || i == workingDays.size() - 1;
            
            if (isEndOfWeek) {
                String weekColor = weekColors[(weekNumber - 1) % weekColors.length];
                Label weekLabel = new Label("S" + weekNumber);
                weekLabel.setStyle(String.format("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; " +
                    "-fx-background-color: %s; -fx-padding: 5; -fx-alignment: center;", weekColor));
                weekLabel.setMinWidth(daysInCurrentWeek * 32);
                weekLabel.setMaxWidth(daysInCurrentWeek * 32);
                weekLabel.setMinHeight(30);
                weekLabel.setMaxHeight(30);
                weekLabel.setAlignment(Pos.CENTER);
                header.add(weekLabel, weekStartCol, 1, daysInCurrentWeek, 1);
                
                weekNumber++;
                weekStartCol = colIndex + 1;
                daysInCurrentWeek = 0;
            }
            
            colIndex++;
        }
        
        // LINHA 2: Detalhes (TIPO, TÍTULO, PESSOAS) + Dias
        Label tipoLabel = new Label("TIPO");
        tipoLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold; -fx-font-size: 11px; " +
            "-fx-background-color: #E0E0E0; -fx-padding: 5; -fx-alignment: center;");
        tipoLabel.setMinWidth(80);
        tipoLabel.setMaxWidth(80);
        tipoLabel.setMinHeight(25);
        tipoLabel.setMaxHeight(25);
        tipoLabel.setAlignment(Pos.CENTER);
        header.add(tipoLabel, 0, 2);
        
        Label tituloLabel = new Label("TÍTULO");
        tituloLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold; -fx-font-size: 11px; " +
            "-fx-background-color: #E0E0E0; -fx-padding: 5 5 5 15; -fx-alignment: center-left;");
        tituloLabel.setMinWidth(350);
        tituloLabel.setMaxWidth(350);
        tituloLabel.setMinHeight(25);
        tituloLabel.setMaxHeight(25);
        tituloLabel.setAlignment(Pos.CENTER_LEFT);
        header.add(tituloLabel, 1, 2);
        
        Label pessoasLabel = new Label("MEMBRO");
        pessoasLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold; -fx-font-size: 11px; " +
            "-fx-background-color: #E0E0E0; -fx-padding: 5; -fx-alignment: center;");
        pessoasLabel.setMinWidth(150);
        pessoasLabel.setMaxWidth(150);
        pessoasLabel.setMinHeight(25);
        pessoasLabel.setMaxHeight(25);
        pessoasLabel.setAlignment(Pos.CENTER);
        header.add(pessoasLabel, 2, 2);
        
        // Dias individuais na LINHA 2
        colIndex = 3;
        weekNumber = 1;
        daysInCurrentWeek = 0;
        weekStartCol = colIndex;
        
        for (int i = 0; i < workingDays.size(); i++) {
            LocalDate day = workingDays.get(i);
            daysInCurrentWeek++;
            
            boolean isEndOfWeek = day.getDayOfWeek() == DayOfWeek.FRIDAY || i == workingDays.size() - 1;
            
            if (isEndOfWeek) {
                String weekColor = weekColors[(weekNumber - 1) % weekColors.length];
                
                for (int j = 0; j < daysInCurrentWeek; j++) {
                    int dayIndex = i - daysInCurrentWeek + j + 1;
                    if (dayIndex >= 0 && dayIndex < workingDays.size()) {
                        LocalDate currentDay = workingDays.get(dayIndex);
                        Label dayLabel = new Label(String.valueOf(currentDay.getDayOfMonth()));
                        dayLabel.setStyle(String.format("-fx-text-fill: #333; -fx-font-weight: bold; -fx-font-size: 10px; " +
                            "-fx-background-color: %s; -fx-padding: 3; -fx-alignment: center;", 
                            adjustBrightness(weekColor, 1.3)));
                        dayLabel.setMinWidth(32);
                        dayLabel.setMaxWidth(32);
                        dayLabel.setMinHeight(25);
                        dayLabel.setMaxHeight(25);
                        dayLabel.setAlignment(Pos.CENTER);
                        header.add(dayLabel, weekStartCol + j, 2);
                    }
                }
                
                weekNumber++;
                weekStartCol = colIndex + 1;
                daysInCurrentWeek = 0;
            }
            
            colIndex++;
        }
        
        return header;
    }

    private GridPane createItemRow(ItemSprintDTO item, int indentLevel, String color, String tipo, int startDay, int customDuration) {
        GridPane row = new GridPane();
        row.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0; -fx-padding: 0;");
        row.setHgap(0);
        
        // Coluna de tipo
        Label tipoLabel = new Label(tipo);
        tipoLabel.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: #333; -fx-font-size: 10px; " +
            "-fx-padding: 5; -fx-background-radius: 0; -fx-font-weight: bold;", color));
        tipoLabel.setMinWidth(80);
        tipoLabel.setMaxWidth(80);
        tipoLabel.setMinHeight(30);
        tipoLabel.setMaxHeight(30);
        tipoLabel.setAlignment(Pos.CENTER);
        row.add(tipoLabel, 0, 0);
        
        // Coluna de título (com indentação)
        HBox tituloBox = new HBox();
        tituloBox.setAlignment(Pos.CENTER_LEFT);
        tituloBox.setPadding(new Insets(5, 5, 5, 5 + (indentLevel * 20)));
        tituloBox.setStyle("-fx-background-color: white;");
        Label tituloLabel = new Label(item.getTitulo());
        tituloLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #333;");
        tituloBox.getChildren().add(tituloLabel);
        tituloBox.setMinWidth(350);
        tituloBox.setMaxWidth(350);
        tituloBox.setMinHeight(30);
        tituloBox.setMaxHeight(30);
        row.add(tituloBox, 1, 0);
        
        // Coluna de pessoas
        Label pessoaLabel = new Label(item.getMembroNome() != null ? item.getMembroNome() : "-");
        pessoaLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666; -fx-background-color: white; -fx-padding: 5;");
        pessoaLabel.setMinWidth(150);
        pessoaLabel.setMaxWidth(150);
        pessoaLabel.setMinHeight(30);
        pessoaLabel.setMaxHeight(30);
        pessoaLabel.setAlignment(Pos.CENTER);
        row.add(pessoaLabel, 2, 0);
        
        // Calcular dias de alocação
        // Se customDuration >= 0, usar ele; caso contrário, usar duração padrão do item
        int duration = customDuration >= 0 ? customDuration : calculateDurationInDays(item);
        
        // Colunas de dias
        int colIndex = 3;
        int daysAllocated = 0;
        for (int i = 0; i < workingDays.size(); i++) {
            Label dayCell = new Label();
            dayCell.setMinWidth(32);
            dayCell.setMaxWidth(32);
            dayCell.setMinHeight(30);
            dayCell.setMaxHeight(30);
            dayCell.setAlignment(Pos.CENTER);
            
            // Verificar se este dia está dentro do período de alocação do item
            if (i >= startDay && daysAllocated < duration) {
                dayCell.setStyle(String.format("-fx-background-color: %s; -fx-border-color: white; " +
                    "-fx-border-width: 0; -fx-text-fill: #333; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 0;", color));
                dayCell.setText(String.valueOf(workingDays.get(i).getDayOfMonth()));
                daysAllocated++;
            } else {
                dayCell.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
            }
            
            row.add(dayCell, colIndex, 0);
            colIndex++;
        }
        
        return row;
    }

    private int calculateDurationInDays(ItemSprintDTO item) {
        if (item.getDuracaoDias() != null && item.getDuracaoDias() > 0) {
            return item.getDuracaoDias();
        } else if (item.getDuracaoSemanas() != null && item.getDuracaoSemanas() > 0) {
            return item.getDuracaoSemanas() * 5;
        }
        return 0;
    }

    private String adjustBrightness(String hexColor, double factor) {
        Color color = Color.web(hexColor);
        double r = Math.min(1.0, color.getRed() * factor);
        double g = Math.min(1.0, color.getGreen() * factor);
        double b = Math.min(1.0, color.getBlue() * factor);
        return String.format("#%02X%02X%02X", 
            (int)(r * 255), (int)(g * 255), (int)(b * 255));
    }
    
    private List<TimelineService.TimelineItem> applyFilters(List<TimelineService.TimelineItem> items) {
        List<TimelineService.TimelineItem> filteredItems = new ArrayList<>();
        
        // Obter filtros selecionados
        boolean showFeatures = showFeaturesCheckBox.isSelected();
        boolean showHistorias = showHistoriasCheckBox.isSelected();
        boolean showTarefas = showTarefasCheckBox.isSelected();
        MembroDTO selectedMembro = membroFilterComboBox.getValue();
        
        for (TimelineService.TimelineItem timelineItem : items) {
            ItemSprintDTO item = timelineItem.getItem();
            int indentLevel = timelineItem.getIndentLevel();
            
            // Filtro por tipo de item
            boolean typeMatch = false;
            
            // Features (nível 0)
            if (item.getTipo() == TipoItem.FEATURE) {
                typeMatch = showFeatures;
            } 
            // Histórias (nível 1 - filhas de Features)
            else if (item.getTipo() == TipoItem.HISTORIA) {
                typeMatch = showHistorias;
            } 
            // Tarefas (nível 2 - filhas de Histórias)
            else if (item.getTipo() == TipoItem.TAREFA) {
                typeMatch = showTarefas;
            } 
            // SUBs (nível 3 - filhas de Tarefas ou Histórias)
            else if (item.getTipo() == TipoItem.SUB) {
                // SUBs são exibidas se Histórias ou Tarefas estiverem marcadas
                typeMatch = showHistorias || showTarefas;
            }
            
            if (!typeMatch) {
                continue;
            }
            
            // Filtro por membro
            if (selectedMembro != null) {
                // Se um membro está selecionado, mostrar apenas itens desse membro
                if (item.getMembroId() == null || !item.getMembroId().equals(selectedMembro.getId())) {
                    continue;
                }
            }
            
            filteredItems.add(timelineItem);
        }
        
        return filteredItems;
    }
    
    @Override
    protected javafx.stage.Stage getCurrentStage() {
        return (javafx.stage.Stage) timelineContainer.getScene().getWindow();
    }
}
