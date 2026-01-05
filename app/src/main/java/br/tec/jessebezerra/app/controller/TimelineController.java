package br.tec.jessebezerra.app.controller;

import br.tec.jessebezerra.app.dto.ItemSprintDTO;
import br.tec.jessebezerra.app.dto.MembroDTO;
import br.tec.jessebezerra.app.dto.SprintDTO;
import br.tec.jessebezerra.app.entity.TipoItem;
import br.tec.jessebezerra.app.service.ItemSprintService;
import br.tec.jessebezerra.app.service.MembroService;
import br.tec.jessebezerra.app.service.PrioridadeService;
import br.tec.jessebezerra.app.service.SprintService;
import br.tec.jessebezerra.app.service.TimelineExcelService;
import br.tec.jessebezerra.app.service.TimelineService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
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
    private CheckBox showSubsCheckBox;
    
    @FXML
    private CheckBox showProjetoCheckBox;
    
    @FXML
    private CheckBox showAplicacaoCheckBox;
    
    @FXML
    private ComboBox<MembroDTO> membroFilterComboBox;
    
    @FXML
    private ComboBox<SprintDTO> sprintFilterComboBox;
    
    private SprintService sprintService;
    private TimelineService timelineService;
    private MembroService membroService;
    private TimelineExcelService excelService;
    private PrioridadeService prioridadeService;
    
    private boolean menuExpanded = true;
    private SprintDTO selectedSprint;
    private List<LocalDate> workingDays;
    private List<SprintDTO> allSprints;
    private Map<Long, List<LocalDate>> workingDaysBySprint;
    
    private static final String[] COLORS = {
        "#FFB6C1", "#FFD700", "#98FB98", "#87CEEB", "#DDA0DD",
        "#F0E68C", "#E0BBE4", "#FFDAB9", "#B0E0E6", "#FFE4B5"
    };

    public TimelineController() {
        this.sprintService = new SprintService();
        this.timelineService = new TimelineService();
        this.membroService = new MembroService();
        this.excelService = new TimelineExcelService();
        this.prioridadeService = new PrioridadeService();
        this.workingDays = new ArrayList<>();
        this.allSprints = new ArrayList<>();
        this.workingDaysBySprint = new HashMap<>();
    }

    @FXML
    public void initialize() {
        loadMembros();
        loadSprints();
        loadAndBuildTimeline();
        setupTimelineClickHandler();
    }
    
    /**
     * Configura handler de clique no Timeline para criar Feature em área vazia
     */
    private void setupTimelineClickHandler() {
        timelineContainer.setOnMouseClicked(event -> {
            // Verificar se NÃO clicou em um GridPane (que são os itens do timeline)
            // e se NÃO clicou em um Label (que são as células dos itens)
            javafx.scene.Node target = (javafx.scene.Node) event.getTarget();
            
            // Percorrer a hierarquia para verificar se está dentro de um GridPane de item
            boolean isInsideItemRow = false;
            javafx.scene.Node current = target;
            while (current != null && current != timelineContainer) {
                if (current instanceof javafx.scene.layout.GridPane) {
                    // Verificar se este GridPane tem contexto de item (tem mais de 3 colunas)
                    javafx.scene.layout.GridPane grid = (javafx.scene.layout.GridPane) current;
                    if (grid.getChildren().size() > 3) {
                        isInsideItemRow = true;
                        break;
                    }
                }
                current = current.getParent();
            }
            
            // Se não está dentro de uma linha de item e é clique direito, mostrar menu
            if (!isInsideItemRow && event.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                // Detectar em qual Sprint o usuário clicou baseado na posição X
                SprintDTO clickedSprint = detectSprintFromClickPosition(event.getX());
                showCreateFeatureContextMenu(event.getScreenX(), event.getScreenY(), clickedSprint);
                event.consume();
            }
        });
    }
    
    /**
     * Detecta em qual Sprint o usuário clicou baseado na posição X do clique
     */
    private SprintDTO detectSprintFromClickPosition(double clickX) {
        // Calcular largura das colunas extras
        boolean showProjeto = showProjetoCheckBox.isSelected();
        boolean showAplicacao = showAplicacaoCheckBox.isSelected();
        int extraColumnsCount = 0;
        if (showProjeto) extraColumnsCount++;
        if (showAplicacao) extraColumnsCount++;
        
        // Largura das colunas fixas (TIPO + TÍTULO + MEMBRO + extras)
        int fixedColumnsWidth = 80 + 350 + 150 + (extraColumnsCount * 120);
        
        // Se clicou antes das colunas de dias, retornar primeira Sprint
        if (clickX < fixedColumnsWidth) {
            return allSprints.isEmpty() ? null : allSprints.get(0);
        }
        
        // Calcular em qual Sprint clicou baseado na posição
        double currentX = fixedColumnsWidth;
        for (SprintDTO sprint : allSprints) {
            List<LocalDate> sprintDays = workingDaysBySprint.get(sprint.getId());
            if (sprintDays != null) {
                double sprintWidth = sprintDays.size() * 32; // 32px por dia
                if (clickX >= currentX && clickX < currentX + sprintWidth) {
                    return sprint;
                }
                currentX += sprintWidth;
            }
        }
        
        // Se não encontrou, retornar última Sprint
        return allSprints.isEmpty() ? null : allSprints.get(allSprints.size() - 1);
    }
    
    private void loadMembros() {
        List<MembroDTO> membros = membroService.findAll();
        membroFilterComboBox.setItems(FXCollections.observableArrayList(membros));
    }
    
    private void loadSprints() {
        List<SprintDTO> sprints = sprintService.findAll();
        sprintFilterComboBox.setItems(FXCollections.observableArrayList(sprints));
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
    protected void onClearSprintFilter() {
        sprintFilterComboBox.setValue(null);
        loadAndBuildTimeline();
    }
    
    @FXML
    protected void onRefreshTimeline() {
        loadAndBuildTimeline();
    }
    
    @FXML
    protected void onColumnVisibilityChanged() {
        loadAndBuildTimeline();
    }
    
    @FXML
    protected void onExportToExcel() {
        if (allSprints == null || allSprints.isEmpty()) {
            showErrorAlert("Erro", "Nenhuma sprint disponível para exportar.");
            return;
        }
        
        try {
            boolean showFeatures = showFeaturesCheckBox.isSelected();
            boolean showHistorias = showHistoriasCheckBox.isSelected();
            boolean showTarefas = showTarefasCheckBox.isSelected();
            boolean showSubs = showSubsCheckBox.isSelected();
            boolean showProjeto = showProjetoCheckBox.isSelected();
            boolean showAplicacao = showAplicacaoCheckBox.isSelected();
            MembroDTO membroFilter = membroFilterComboBox.getValue();
            
            java.io.File excelFile = excelService.exportToExcel(
                allSprints, 
                showFeatures, 
                showHistorias, 
                showTarefas, 
                showSubs, 
                showProjeto, 
                showAplicacao, 
                membroFilter
            );
            
            // Abrir o arquivo gerado
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(excelFile);
            }
            
            showAlert("Sucesso", "Excel gerado com sucesso!\nArquivo: " + excelFile.getAbsolutePath());
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erro", "Erro ao gerar Excel: " + e.getMessage());
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


    private void loadAndBuildTimeline() {
        List<SprintDTO> allSprintsFromDB = sprintService.findAll();
        
        // Aplicar filtro de Sprint se selecionado
        SprintDTO sprintFilter = sprintFilterComboBox.getValue();
        if (sprintFilter != null) {
            allSprints = allSprintsFromDB.stream()
                .filter(s -> s.getId().equals(sprintFilter.getId()))
                .collect(Collectors.toList());
        } else {
            allSprints = allSprintsFromDB;
        }
        
        if (!allSprints.isEmpty()) {
            selectedSprint = allSprints.get(0); // Manter para compatibilidade com export
            calculateWorkingDaysForAllSprints();
            buildTimeline();
        }
    }

    private void calculateWorkingDays() {
        workingDays = timelineService.calculateWorkingDays(selectedSprint);
    }
    
    private void calculateWorkingDaysForAllSprints() {
        workingDaysBySprint.clear();
        for (SprintDTO sprint : allSprints) {
            List<LocalDate> days = timelineService.calculateWorkingDays(sprint);
            workingDaysBySprint.put(sprint.getId(), days);
        }
        // Manter workingDays para compatibilidade
        if (!allSprints.isEmpty()) {
            workingDays = workingDaysBySprint.get(allSprints.get(0).getId());
        }
    }

    private void buildTimeline() {
        timelineContainer.getChildren().clear();
        
        // Criar cabeçalho
        GridPane header = createHeader();
        timelineContainer.getChildren().add(header);
        
        // Buscar itens de todas as Sprints organizados hierarquicamente
        List<TimelineService.TimelineItem> allTimelineItems = new ArrayList<>();
        for (SprintDTO sprint : allSprints) {
            List<TimelineService.TimelineItem> sprintItems = timelineService.buildHierarchicalTimeline(sprint.getId());
            allTimelineItems.addAll(sprintItems);
        }
        
        List<TimelineService.TimelineItem> timelineItems = allTimelineItems;
        
        // Aplicar filtros
        timelineItems = applyFilters(timelineItems);
        
        // Buscar todos os itens para cálculos
        List<ItemSprintDTO> allItems = new ArrayList<>();
        for (TimelineService.TimelineItem ti : timelineItems) {
            allItems.add(ti.getItem());
        }
        
        // Rastrear dias alocados por membro POR SPRINT
        Map<Long, Map<Long, Integer>> memberAllocationsBySprint = new HashMap<>();
        for (SprintDTO sprint : allSprints) {
            memberAllocationsBySprint.put(sprint.getId(), new HashMap<>());
        }
        
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
            
            // Obter memberAllocations da Sprint específica do item
            Map<Long, Integer> memberAllocations = memberAllocationsBySprint.get(item.getSprintId());
            if (memberAllocations == null) {
                memberAllocations = new HashMap<>();
                memberAllocationsBySprint.put(item.getSprintId(), memberAllocations);
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
        
        // Calcular largura das colunas extras
        boolean showProjeto = showProjetoCheckBox.isSelected();
        boolean showAplicacao = showAplicacaoCheckBox.isSelected();
        int extraColumnsWidth = 0;
        int extraColumnsCount = 0;
        
        if (showProjeto) {
            extraColumnsWidth += 120;
            extraColumnsCount++;
        }
        if (showAplicacao) {
            extraColumnsWidth += 120;
            extraColumnsCount++;
        }
        
        // Calcular total de dias de todas as Sprints
        int totalDaysAllSprints = 0;
        for (SprintDTO sprint : allSprints) {
            List<LocalDate> days = workingDaysBySprint.get(sprint.getId());
            if (days != null) {
                totalDaysAllSprints += days.size();
            }
        }
        
        int roadmapWidth = 580 + extraColumnsWidth;
        int itensSprintWidth = 430;
        int pessoasWidth = 150;
        int currentCol = 0;
        
        // LINHA 0: "ROADMAP" mesclado até PESSOAS + colunas extras (rosa) + Nomes das Sprints mesclados nas semanas (laranja)
        Label roadmapLabel = new Label("ROADMAP");
        roadmapLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold; -fx-font-size: 13px; " +
            "-fx-background-color: #FFB6D9; -fx-padding: 8; -fx-alignment: center; " +
            "-fx-border-color: transparent; -fx-border-width: 0;");
        roadmapLabel.setMinWidth(roadmapWidth);
        roadmapLabel.setMaxWidth(roadmapWidth);
        roadmapLabel.setMinHeight(35);
        roadmapLabel.setMaxHeight(35);
        roadmapLabel.setAlignment(Pos.CENTER);
        header.add(roadmapLabel, 0, 0, 3 + extraColumnsCount, 1);
        
        // Adicionar nome de cada Sprint sobre suas semanas (laranja)
        int sprintColStart = 3 + extraColumnsCount;
        for (SprintDTO sprint : allSprints) {
            List<LocalDate> sprintDays = workingDaysBySprint.get(sprint.getId());
            if (sprintDays != null && !sprintDays.isEmpty()) {
                int sprintTotalDays = sprintDays.size();
                Label sprintNameLabel = new Label(sprint.getNome().toUpperCase());
                sprintNameLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold; -fx-font-size: 13px; " +
                    "-fx-background-color: #FFD4A3; -fx-padding: 8; -fx-alignment: center; " +
                    "-fx-border-color: #999; -fx-border-width: 0 1 0 0;");
                sprintNameLabel.setMinWidth(sprintTotalDays * 32);
                sprintNameLabel.setMaxWidth(sprintTotalDays * 32);
                sprintNameLabel.setMinHeight(35);
                sprintNameLabel.setMaxHeight(35);
                sprintNameLabel.setAlignment(Pos.CENTER);
                header.add(sprintNameLabel, sprintColStart, 0, sprintTotalDays, 1);
                sprintColStart += sprintTotalDays;
            }
        }
        
        // LINHA 1: "ITENS DA SPRINT" mesclado (laranja claro)
        Label itensSprintLabel = new Label("ITENS DA SPRINT");
        itensSprintLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold; -fx-font-size: 12px; " +
            "-fx-background-color: #FFD4A3; -fx-padding: 5; -fx-alignment: center;");
        itensSprintLabel.setMinWidth(itensSprintWidth);
        itensSprintLabel.setMaxWidth(itensSprintWidth);
        itensSprintLabel.setMinHeight(30);
        itensSprintLabel.setMaxHeight(30);
        itensSprintLabel.setAlignment(Pos.CENTER);
        header.add(itensSprintLabel, 0, 1, 2, 1);
        
        currentCol = 2;
        
        // "PESSOAS" (laranja claro)
        Label pessoasHeaderLabel = new Label("PESSOAS");
        pessoasHeaderLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold; -fx-font-size: 12px; " +
            "-fx-background-color: #FFD4A3; -fx-padding: 5; -fx-alignment: center;");
        pessoasHeaderLabel.setMinWidth(pessoasWidth);
        pessoasHeaderLabel.setMaxWidth(pessoasWidth);
        pessoasHeaderLabel.setMinHeight(30);
        pessoasHeaderLabel.setMaxHeight(30);
        pessoasHeaderLabel.setAlignment(Pos.CENTER);
        header.add(pessoasHeaderLabel, currentCol, 1);
        currentCol++;
        
        // "PROJETO" se visível (laranja claro)
        if (showProjeto) {
            Label projetoHeaderLabel = new Label("PROJETO");
            projetoHeaderLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold; -fx-font-size: 12px; " +
                "-fx-background-color: #FFD4A3; -fx-padding: 5; -fx-alignment: center;");
            projetoHeaderLabel.setMinWidth(120);
            projetoHeaderLabel.setMaxWidth(120);
            projetoHeaderLabel.setMinHeight(30);
            projetoHeaderLabel.setMaxHeight(30);
            projetoHeaderLabel.setAlignment(Pos.CENTER);
            header.add(projetoHeaderLabel, currentCol, 1);
            currentCol++;
        }
        
        // "APLICAÇÃO" se visível (laranja claro)
        if (showAplicacao) {
            Label aplicacaoHeaderLabel = new Label("APLICAÇÃO");
            aplicacaoHeaderLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold; -fx-font-size: 12px; " +
                "-fx-background-color: #FFD4A3; -fx-padding: 5; -fx-alignment: center;");
            aplicacaoHeaderLabel.setMinWidth(120);
            aplicacaoHeaderLabel.setMaxWidth(120);
            aplicacaoHeaderLabel.setMinHeight(30);
            aplicacaoHeaderLabel.setMaxHeight(30);
            aplicacaoHeaderLabel.setAlignment(Pos.CENTER);
            header.add(aplicacaoHeaderLabel, currentCol, 1);
            currentCol++;
        }
        
        // Separar dias por semanas na LINHA 1 para cada Sprint
        String[] weekColors = {"#A8D5E2", "#7FB3D5", "#5499C7"}; // Azul claro ao escuro
        int colIndex = currentCol; // Começa após as colunas de PESSOAS, PROJETO e APLICAÇÃO
        
        for (SprintDTO sprint : allSprints) {
            List<LocalDate> sprintDays = workingDaysBySprint.get(sprint.getId());
            if (sprintDays != null && !sprintDays.isEmpty()) {
                int weekNumber = 1;
                int daysInCurrentWeek = 0;
                int weekStartCol = colIndex;
                
                for (int i = 0; i < sprintDays.size(); i++) {
                    LocalDate day = sprintDays.get(i);
                    daysInCurrentWeek++;
                    
                    boolean isEndOfWeek = day.getDayOfWeek() == DayOfWeek.FRIDAY || i == sprintDays.size() - 1;
                    
                    if (isEndOfWeek) {
                        String weekColor = weekColors[(weekNumber - 1) % weekColors.length];
                        Label weekLabel = new Label("S" + weekNumber);
                        weekLabel.setStyle(String.format("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; " +
                            "-fx-background-color: %s; -fx-padding: 5; -fx-alignment: center; " +
                            "-fx-border-color: #999; -fx-border-width: 0 1 0 0;", weekColor));
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
            }
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
        
        currentCol = 3;
        
        // Adicionar coluna PROJETO na linha 2 se visível
        if (showProjeto) {
            Label projetoDetailLabel = new Label("PROJETO");
            projetoDetailLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold; -fx-font-size: 11px; " +
                "-fx-background-color: #E0E0E0; -fx-padding: 5; -fx-alignment: center;");
            projetoDetailLabel.setMinWidth(120);
            projetoDetailLabel.setMaxWidth(120);
            projetoDetailLabel.setMinHeight(25);
            projetoDetailLabel.setMaxHeight(25);
            projetoDetailLabel.setAlignment(Pos.CENTER);
            header.add(projetoDetailLabel, currentCol, 2);
            currentCol++;
        }
        
        // Adicionar coluna APLICAÇÃO na linha 2 se visível
        if (showAplicacao) {
            Label aplicacaoDetailLabel = new Label("APLICAÇÃO");
            aplicacaoDetailLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold; -fx-font-size: 11px; " +
                "-fx-background-color: #E0E0E0; -fx-padding: 5; -fx-alignment: center;");
            aplicacaoDetailLabel.setMinWidth(120);
            aplicacaoDetailLabel.setMaxWidth(120);
            aplicacaoDetailLabel.setMinHeight(25);
            aplicacaoDetailLabel.setMaxHeight(25);
            aplicacaoDetailLabel.setAlignment(Pos.CENTER);
            header.add(aplicacaoDetailLabel, currentCol, 2);
            currentCol++;
        }
        
        // Dias individuais na LINHA 2 para cada Sprint
        colIndex = currentCol; // Começa após as colunas extras
        
        for (SprintDTO sprint : allSprints) {
            List<LocalDate> sprintDays = workingDaysBySprint.get(sprint.getId());
            if (sprintDays != null && !sprintDays.isEmpty()) {
                int weekNumber = 1;
                int daysInCurrentWeek = 0;
                int weekStartCol = colIndex;
                
                for (int i = 0; i < sprintDays.size(); i++) {
                    LocalDate day = sprintDays.get(i);
                    daysInCurrentWeek++;
                    
                    boolean isEndOfWeek = day.getDayOfWeek() == DayOfWeek.FRIDAY || i == sprintDays.size() - 1;
            
                    if (isEndOfWeek) {
                        String weekColor = weekColors[(weekNumber - 1) % weekColors.length];
                        
                        for (int j = 0; j < daysInCurrentWeek; j++) {
                            int dayIndex = i - daysInCurrentWeek + j + 1;
                            if (dayIndex >= 0 && dayIndex < sprintDays.size()) {
                                LocalDate currentDay = sprintDays.get(dayIndex);
                                Label dayLabel = new Label(String.valueOf(currentDay.getDayOfMonth()));
                                dayLabel.setStyle(String.format("-fx-text-fill: #333; -fx-font-weight: bold; -fx-font-size: 10px; " +
                                    "-fx-background-color: %s; -fx-padding: 3; -fx-alignment: center; " +
                                    "-fx-border-color: #999; -fx-border-width: 0 1 0 0;", 
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
            }
        }
        
        return header;
    }

    private GridPane createItemRow(ItemSprintDTO item, int indentLevel, String color, String tipo, int startDay, int customDuration) {
        GridPane row = new GridPane();
        row.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0; -fx-padding: 0;");
        row.setHgap(0);
        
        // Adicionar drag-and-drop apenas para SUBs
        if (item.getTipo() == TipoItem.SUB) {
            setupDragAndDrop(row, item);
        }
        
        // Adicionar menu de contexto para Features, Histórias e Tarefas
        if (item.getTipo() == TipoItem.FEATURE) {
            setupFeatureContextMenu(row, item);
        } else if (item.getTipo() == TipoItem.HISTORIA || item.getTipo() == TipoItem.TAREFA) {
            setupContextMenu(row, item);
        }
        
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
        
        int currentCol = 3;
        
        // Coluna de Projeto (se visível)
        if (showProjetoCheckBox.isSelected()) {
            Label projetoLabel = new Label(item.getProjetoNome() != null ? item.getProjetoNome() : "-");
            projetoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666; -fx-background-color: white; -fx-padding: 5;");
            projetoLabel.setMinWidth(120);
            projetoLabel.setMaxWidth(120);
            projetoLabel.setMinHeight(30);
            projetoLabel.setMaxHeight(30);
            projetoLabel.setAlignment(Pos.CENTER);
            row.add(projetoLabel, currentCol, 0);
            currentCol++;
        }
        
        // Coluna de Aplicação (se visível)
        if (showAplicacaoCheckBox.isSelected()) {
            Label aplicacaoLabel = new Label(item.getAplicacaoNome() != null ? item.getAplicacaoNome() : "-");
            aplicacaoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666; -fx-background-color: white; -fx-padding: 5;");
            aplicacaoLabel.setMinWidth(120);
            aplicacaoLabel.setMaxWidth(120);
            aplicacaoLabel.setMinHeight(30);
            aplicacaoLabel.setMaxHeight(30);
            aplicacaoLabel.setAlignment(Pos.CENTER);
            row.add(aplicacaoLabel, currentCol, 0);
            currentCol++;
        }
        
        // Calcular dias de alocação
        // Se customDuration >= 0, usar ele; caso contrário, usar duração padrão do item
        int duration = customDuration >= 0 ? customDuration : calculateDurationInDays(item);
        
        // Calcular offset de coluna baseado na Sprint do item
        int sprintColumnOffset = currentCol;
        for (SprintDTO sprint : allSprints) {
            if (sprint.getId().equals(item.getSprintId())) {
                break;
            }
            List<LocalDate> sprintDays = workingDaysBySprint.get(sprint.getId());
            if (sprintDays != null) {
                sprintColumnOffset += sprintDays.size();
            }
        }
        
        // Obter dias úteis da Sprint do item
        List<LocalDate> itemSprintDays = workingDaysBySprint.get(item.getSprintId());
        if (itemSprintDays == null) {
            itemSprintDays = new ArrayList<>();
        }
        
        // Renderizar células de dias para TODAS as Sprints
        int colIndex = currentCol;
        for (SprintDTO sprint : allSprints) {
            List<LocalDate> sprintDays = workingDaysBySprint.get(sprint.getId());
            if (sprintDays != null) {
                for (int i = 0; i < sprintDays.size(); i++) {
                    Label dayCell = new Label();
                    dayCell.setMinWidth(32);
                    dayCell.setMaxWidth(32);
                    dayCell.setMinHeight(30);
                    dayCell.setMaxHeight(30);
                    dayCell.setAlignment(Pos.CENTER);
                    
                    // Verificar se este item pertence a esta Sprint e se este dia está dentro do período de alocação
                    if (sprint.getId().equals(item.getSprintId()) && i >= startDay && i < startDay + duration) {
                        dayCell.setStyle(String.format("-fx-background-color: %s; -fx-border-color: white; " +
                            "-fx-border-width: 0; -fx-text-fill: #333; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 0;", color));
                        dayCell.setText(String.valueOf(sprintDays.get(i).getDayOfMonth()));
                    } else {
                        dayCell.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                    }
                    
                    row.add(dayCell, colIndex, 0);
                    colIndex++;
                }
            }
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
        boolean showSubs = showSubsCheckBox.isSelected();
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
                typeMatch = showSubs;
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
    
    /**
     * Configura menu de contexto para Features
     */
    private void setupFeatureContextMenu(GridPane row, ItemSprintDTO item) {
        ContextMenu contextMenu = new ContextMenu();
        
        // Opção "Ver"
        MenuItem verItem = new MenuItem("Ver");
        verItem.setOnAction(event -> {
            try {
                openFeatureDetails(item);
            } catch (Exception e) {
                showErrorAlert("Erro", "Erro ao abrir detalhes: " + e.getMessage());
            }
        });
        
        // Opção "Criar História"
        MenuItem criarHistoriaItem = new MenuItem("Criar História");
        criarHistoriaItem.setOnAction(event -> {
            try {
                openCreateHistoriaDialog(item);
            } catch (Exception e) {
                showErrorAlert("Erro", "Erro ao criar História: " + e.getMessage());
            }
        });
        
        // Opção "Criar Tarefa"
        MenuItem criarTarefaItem = new MenuItem("Criar Tarefa");
        criarTarefaItem.setOnAction(event -> {
            try {
                openCreateTarefaDialog(item);
            } catch (Exception e) {
                showErrorAlert("Erro", "Erro ao criar Tarefa: " + e.getMessage());
            }
        });
        
        contextMenu.getItems().addAll(verItem, criarHistoriaItem, criarTarefaItem);
        
        // Adicionar menu de contexto à linha
        row.setOnContextMenuRequested(event -> {
            contextMenu.show(row, event.getScreenX(), event.getScreenY());
            event.consume();
        });
    }
    
    /**
     * Configura menu de contexto para Histórias e Tarefas
     */
    private void setupContextMenu(GridPane row, ItemSprintDTO item) {
        ContextMenu contextMenu = new ContextMenu();
        
        // Opção "Ver"
        MenuItem verItem = new MenuItem("Ver");
        verItem.setOnAction(event -> {
            try {
                openItemDetails(item);
            } catch (Exception e) {
                showErrorAlert("Erro", "Erro ao abrir detalhes: " + e.getMessage());
            }
        });
        
        // Opção "Criar SubItem"
        MenuItem criarSubItem = new MenuItem("Criar SubItem");
        criarSubItem.setOnAction(event -> {
            try {
                openCreateSubDialog(item);
            } catch (Exception e) {
                showErrorAlert("Erro", "Erro ao criar SubItem: " + e.getMessage());
            }
        });
        
        contextMenu.getItems().addAll(verItem, criarSubItem);
        
        // Adicionar menu de contexto à linha
        row.setOnContextMenuRequested(event -> {
            contextMenu.show(row, event.getScreenX(), event.getScreenY());
            event.consume();
        });
    }
    
    /**
     * Abre detalhes do item (História ou Tarefa)
     */
    private void openItemDetails(ItemSprintDTO item) {
        try {
            String viewName = item.getTipo() == TipoItem.HISTORIA ? "historia-view.fxml" : "tarefa-view.fxml";
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/br/tec/jessebezerra/app/" + viewName)
            );
            
            javafx.scene.Parent root = loader.load();
            
            // Obter controller e passar o item para edição
            Object controller = loader.getController();
            if (item.getTipo() == TipoItem.HISTORIA) {
                br.tec.jessebezerra.app.controller.HistoriaController historiaController = 
                    (br.tec.jessebezerra.app.controller.HistoriaController) controller;
                historiaController.editItem(item);
            } else {
                br.tec.jessebezerra.app.controller.TarefaController tarefaController = 
                    (br.tec.jessebezerra.app.controller.TarefaController) controller;
                tarefaController.editItem(item);
            }
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle(item.getTipo() == TipoItem.HISTORIA ? "Detalhes da História" : "Detalhes da Tarefa");
            stage.setScene(new javafx.scene.Scene(root, 1200, 800));
            stage.setResizable(true);
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // Recarregar timeline após fechar
            loadAndBuildTimeline();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erro", "Erro ao abrir detalhes: " + e.getMessage());
        }
    }
    
    /**
     * Abre dialog para criar SUB
     */
    private void openCreateSubDialog(ItemSprintDTO parentItem) {
        javafx.scene.control.Dialog<ItemSprintDTO> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Criar SubItem");
        dialog.setHeaderText("Criar SubItem para: " + parentItem.getTitulo());
        
        // Botões
        javafx.scene.control.ButtonType salvarButtonType = new javafx.scene.control.ButtonType("Salvar", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(salvarButtonType, javafx.scene.control.ButtonType.CANCEL);
        
        // Criar formulário
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        javafx.scene.control.TextField tituloField = new javafx.scene.control.TextField();
        tituloField.setPromptText("Título da SUB");
        
        javafx.scene.control.TextArea descricaoArea = new javafx.scene.control.TextArea();
        descricaoArea.setPromptText("Descrição");
        descricaoArea.setPrefRowCount(3);
        
        javafx.scene.control.TextField duracaoDiasField = new javafx.scene.control.TextField();
        duracaoDiasField.setPromptText("Duração em dias");
        
        javafx.scene.control.ComboBox<MembroDTO> membroComboBox = new javafx.scene.control.ComboBox<>();
        membroComboBox.setItems(javafx.collections.FXCollections.observableArrayList(membroService.findAll()));
        membroComboBox.setPromptText("Selecione um membro");
        
        javafx.scene.control.ComboBox<String> statusComboBox = new javafx.scene.control.ComboBox<>();
        statusComboBox.setItems(javafx.collections.FXCollections.observableArrayList(
            "CRIADO", "PLANEJADO", "REFINADO", "EM_EXECUCAO", "EM_TESTES", "CONCLUIDO", "CANCELADO", "IMPEDIDO"
        ));
        statusComboBox.setValue("CRIADO");
        
        grid.add(new javafx.scene.control.Label("Título:"), 0, 0);
        grid.add(tituloField, 1, 0);
        grid.add(new javafx.scene.control.Label("Descrição:"), 0, 1);
        grid.add(descricaoArea, 1, 1);
        grid.add(new javafx.scene.control.Label("Duração (dias):"), 0, 2);
        grid.add(duracaoDiasField, 1, 2);
        grid.add(new javafx.scene.control.Label("Membro:"), 0, 3);
        grid.add(membroComboBox, 1, 3);
        grid.add(new javafx.scene.control.Label("Status:"), 0, 4);
        grid.add(statusComboBox, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        // Focar no campo título
        javafx.application.Platform.runLater(() -> tituloField.requestFocus());
        
        // Validação e conversão do resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == salvarButtonType) {
                if (tituloField.getText().trim().isEmpty()) {
                    showErrorAlert("Erro", "O título é obrigatório!");
                    return null;
                }
                
                ItemSprintDTO sub = new ItemSprintDTO();
                sub.setTipo(TipoItem.SUB);
                sub.setTitulo(tituloField.getText().trim());
                sub.setDescricao(descricaoArea.getText().trim());
                sub.setSprintId(parentItem.getSprintId());
                sub.setItemPaiId(parentItem.getId());
                sub.setStatus(br.tec.jessebezerra.app.entity.StatusItem.valueOf(statusComboBox.getValue()));
                
                if (!duracaoDiasField.getText().trim().isEmpty()) {
                    try {
                        sub.setDuracaoDias(Integer.parseInt(duracaoDiasField.getText().trim()));
                    } catch (NumberFormatException e) {
                        showErrorAlert("Erro", "Duração deve ser um número válido!");
                        return null;
                    }
                }
                
                if (membroComboBox.getValue() != null) {
                    sub.setMembroId(membroComboBox.getValue().getId());
                }
                
                return sub;
            }
            return null;
        });
        
        // Mostrar dialog e processar resultado
        java.util.Optional<ItemSprintDTO> result = dialog.showAndWait();
        result.ifPresent(sub -> {
            try {
                ItemSprintService itemSprintService = new ItemSprintService();
                itemSprintService.create(sub);
                showAlert("Sucesso", "SubItem criado com sucesso!");
                loadAndBuildTimeline();
            } catch (Exception e) {
                showErrorAlert("Erro", "Erro ao salvar SubItem: " + e.getMessage());
            }
        });
    }
    
    /**
     * Abre detalhes da Feature
     */
    private void openFeatureDetails(ItemSprintDTO item) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/br/tec/jessebezerra/app/feature-view.fxml")
            );
            
            javafx.scene.Parent root = loader.load();
            
            // Obter controller e passar o item para edição
            br.tec.jessebezerra.app.controller.FeatureController featureController = 
                (br.tec.jessebezerra.app.controller.FeatureController) loader.getController();
            featureController.editItem(item);
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Detalhes da Feature");
            stage.setScene(new javafx.scene.Scene(root, 1200, 800));
            stage.setResizable(true);
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // Recarregar timeline após fechar
            loadAndBuildTimeline();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erro", "Erro ao abrir detalhes: " + e.getMessage());
        }
    }
    
    /**
     * Abre dialog para criar História filha de uma Feature
     */
    private void openCreateHistoriaDialog(ItemSprintDTO parentFeature) {
        javafx.scene.control.Dialog<ItemSprintDTO> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Criar História");
        dialog.setHeaderText("Nova História para Feature: " + parentFeature.getTitulo());
        
        javafx.scene.control.ButtonType salvarButtonType = new javafx.scene.control.ButtonType("Salvar", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(salvarButtonType, javafx.scene.control.ButtonType.CANCEL);
        
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        javafx.scene.control.TextField tituloField = new javafx.scene.control.TextField();
        tituloField.setPromptText("Título");
        
        javafx.scene.control.TextArea descricaoArea = new javafx.scene.control.TextArea();
        descricaoArea.setPromptText("Descrição");
        descricaoArea.setPrefRowCount(3);
        
        javafx.scene.control.TextField duracaoSemanasField = new javafx.scene.control.TextField();
        duracaoSemanasField.setPromptText("Duração em semanas");
        
        javafx.scene.control.ComboBox<MembroDTO> membroComboBox = new javafx.scene.control.ComboBox<>();
        membroComboBox.setItems(javafx.collections.FXCollections.observableArrayList(membroService.findAll()));
        membroComboBox.setPromptText("Selecione um membro");
        
        javafx.scene.control.ComboBox<String> statusComboBox = new javafx.scene.control.ComboBox<>();
        statusComboBox.setItems(javafx.collections.FXCollections.observableArrayList(
            "CRIADO", "PLANEJADO", "REFINADO", "EM_EXECUCAO", "EM_TESTES", "CONCLUIDO", "CANCELADO", "IMPEDIDO"
        ));
        statusComboBox.setValue("CRIADO");
        
        grid.add(new javafx.scene.control.Label("Título:"), 0, 0);
        grid.add(tituloField, 1, 0);
        grid.add(new javafx.scene.control.Label("Descrição:"), 0, 1);
        grid.add(descricaoArea, 1, 1);
        grid.add(new javafx.scene.control.Label("Duração (semanas):"), 0, 2);
        grid.add(duracaoSemanasField, 1, 2);
        grid.add(new javafx.scene.control.Label("Membro:"), 0, 3);
        grid.add(membroComboBox, 1, 3);
        grid.add(new javafx.scene.control.Label("Status:"), 0, 4);
        grid.add(statusComboBox, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        javafx.application.Platform.runLater(() -> tituloField.requestFocus());
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == salvarButtonType) {
                if (tituloField.getText().trim().isEmpty()) {
                    showErrorAlert("Erro", "O título é obrigatório!");
                    return null;
                }
                
                ItemSprintDTO historia = new ItemSprintDTO();
                historia.setTipo(TipoItem.HISTORIA);
                historia.setTitulo(tituloField.getText().trim());
                historia.setDescricao(descricaoArea.getText().trim());
                historia.setSprintId(parentFeature.getSprintId());
                historia.setItemPaiId(parentFeature.getId());
                historia.setStatus(br.tec.jessebezerra.app.entity.StatusItem.valueOf(statusComboBox.getValue()));
                
                if (!duracaoSemanasField.getText().trim().isEmpty()) {
                    try {
                        historia.setDuracaoSemanas(Integer.parseInt(duracaoSemanasField.getText().trim()));
                    } catch (NumberFormatException e) {
                        showErrorAlert("Erro", "Duração deve ser um número válido!");
                        return null;
                    }
                }
                
                if (membroComboBox.getValue() != null) {
                    historia.setMembroId(membroComboBox.getValue().getId());
                }
                
                return historia;
            }
            return null;
        });
        
        java.util.Optional<ItemSprintDTO> result = dialog.showAndWait();
        result.ifPresent(historia -> {
            try {
                ItemSprintService itemSprintService = new ItemSprintService();
                itemSprintService.create(historia);
                showAlert("Sucesso", "História criada com sucesso!");
                loadAndBuildTimeline();
            } catch (Exception e) {
                showErrorAlert("Erro", "Erro ao salvar História: " + e.getMessage());
            }
        });
    }
    
    /**
     * Abre dialog para criar Tarefa filha de uma Feature
     */
    private void openCreateTarefaDialog(ItemSprintDTO parentFeature) {
        javafx.scene.control.Dialog<ItemSprintDTO> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Criar Tarefa");
        dialog.setHeaderText("Nova Tarefa para Feature: " + parentFeature.getTitulo());
        
        javafx.scene.control.ButtonType salvarButtonType = new javafx.scene.control.ButtonType("Salvar", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(salvarButtonType, javafx.scene.control.ButtonType.CANCEL);
        
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        javafx.scene.control.TextField tituloField = new javafx.scene.control.TextField();
        tituloField.setPromptText("Título");
        
        javafx.scene.control.TextArea descricaoArea = new javafx.scene.control.TextArea();
        descricaoArea.setPromptText("Descrição");
        descricaoArea.setPrefRowCount(3);
        
        javafx.scene.control.TextField duracaoSemanasField = new javafx.scene.control.TextField();
        duracaoSemanasField.setPromptText("Duração em semanas");
        
        javafx.scene.control.ComboBox<MembroDTO> membroComboBox = new javafx.scene.control.ComboBox<>();
        membroComboBox.setItems(javafx.collections.FXCollections.observableArrayList(membroService.findAll()));
        membroComboBox.setPromptText("Selecione um membro");
        
        javafx.scene.control.ComboBox<String> statusComboBox = new javafx.scene.control.ComboBox<>();
        statusComboBox.setItems(javafx.collections.FXCollections.observableArrayList(
            "CRIADO", "PLANEJADO", "REFINADO", "EM_EXECUCAO", "EM_TESTES", "CONCLUIDO", "CANCELADO", "IMPEDIDO"
        ));
        statusComboBox.setValue("CRIADO");
        
        grid.add(new javafx.scene.control.Label("Título:"), 0, 0);
        grid.add(tituloField, 1, 0);
        grid.add(new javafx.scene.control.Label("Descrição:"), 0, 1);
        grid.add(descricaoArea, 1, 1);
        grid.add(new javafx.scene.control.Label("Duração (semanas):"), 0, 2);
        grid.add(duracaoSemanasField, 1, 2);
        grid.add(new javafx.scene.control.Label("Membro:"), 0, 3);
        grid.add(membroComboBox, 1, 3);
        grid.add(new javafx.scene.control.Label("Status:"), 0, 4);
        grid.add(statusComboBox, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        javafx.application.Platform.runLater(() -> tituloField.requestFocus());
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == salvarButtonType) {
                if (tituloField.getText().trim().isEmpty()) {
                    showErrorAlert("Erro", "O título é obrigatório!");
                    return null;
                }
                
                ItemSprintDTO tarefa = new ItemSprintDTO();
                tarefa.setTipo(TipoItem.TAREFA);
                tarefa.setTitulo(tituloField.getText().trim());
                tarefa.setDescricao(descricaoArea.getText().trim());
                tarefa.setSprintId(parentFeature.getSprintId());
                tarefa.setItemPaiId(parentFeature.getId());
                tarefa.setStatus(br.tec.jessebezerra.app.entity.StatusItem.valueOf(statusComboBox.getValue()));
                
                if (!duracaoSemanasField.getText().trim().isEmpty()) {
                    try {
                        tarefa.setDuracaoSemanas(Integer.parseInt(duracaoSemanasField.getText().trim()));
                    } catch (NumberFormatException e) {
                        showErrorAlert("Erro", "Duração deve ser um número válido!");
                        return null;
                    }
                }
                
                if (membroComboBox.getValue() != null) {
                    tarefa.setMembroId(membroComboBox.getValue().getId());
                }
                
                return tarefa;
            }
            return null;
        });
        
        java.util.Optional<ItemSprintDTO> result = dialog.showAndWait();
        result.ifPresent(tarefa -> {
            try {
                ItemSprintService itemSprintService = new ItemSprintService();
                itemSprintService.create(tarefa);
                showAlert("Sucesso", "Tarefa criada com sucesso!");
                loadAndBuildTimeline();
            } catch (Exception e) {
                showErrorAlert("Erro", "Erro ao salvar Tarefa: " + e.getMessage());
            }
        });
    }
    
    /**
     * Exibe menu de contexto para criar Feature em área vazia
     */
    private void showCreateFeatureContextMenu(double screenX, double screenY, SprintDTO detectedSprint) {
        ContextMenu contextMenu = new ContextMenu();
        
        String sprintName = detectedSprint != null ? detectedSprint.getNome() : "Sprint";
        MenuItem criarFeatureItem = new MenuItem("Criar Feature na " + sprintName);
        criarFeatureItem.setOnAction(event -> {
            try {
                openCreateFeatureDialog(detectedSprint);
            } catch (Exception e) {
                showErrorAlert("Erro", "Erro ao criar Feature: " + e.getMessage());
            }
        });
        
        contextMenu.getItems().add(criarFeatureItem);
        contextMenu.show(timelineContainer.getScene().getWindow(), screenX, screenY);
    }
    
    /**
     * Abre dialog para criar nova Feature
     */
    private void openCreateFeatureDialog(SprintDTO preSelectedSprint) {
        javafx.scene.control.Dialog<ItemSprintDTO> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Criar Feature");
        dialog.setHeaderText("Nova Feature");
        
        javafx.scene.control.ButtonType salvarButtonType = new javafx.scene.control.ButtonType("Salvar", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(salvarButtonType, javafx.scene.control.ButtonType.CANCEL);
        
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        javafx.scene.control.TextField tituloField = new javafx.scene.control.TextField();
        tituloField.setPromptText("Título");
        
        javafx.scene.control.TextArea descricaoArea = new javafx.scene.control.TextArea();
        descricaoArea.setPromptText("Descrição");
        descricaoArea.setPrefRowCount(3);
        
        javafx.scene.control.TextField duracaoSemanasField = new javafx.scene.control.TextField();
        duracaoSemanasField.setPromptText("Duração em semanas");
        
        javafx.scene.control.ComboBox<SprintDTO> sprintComboBox = new javafx.scene.control.ComboBox<>();
        sprintComboBox.setItems(javafx.collections.FXCollections.observableArrayList(allSprints));
        sprintComboBox.setPromptText("Selecione uma Sprint");
        // Pré-selecionar a Sprint detectada pelo clique
        if (preSelectedSprint != null) {
            sprintComboBox.setValue(preSelectedSprint);
        } else if (!allSprints.isEmpty()) {
            sprintComboBox.setValue(allSprints.get(0));
        }
        
        javafx.scene.control.ComboBox<MembroDTO> membroComboBox = new javafx.scene.control.ComboBox<>();
        membroComboBox.setItems(javafx.collections.FXCollections.observableArrayList(membroService.findAll()));
        membroComboBox.setPromptText("Selecione um membro");
        
        javafx.scene.control.ComboBox<String> statusComboBox = new javafx.scene.control.ComboBox<>();
        statusComboBox.setItems(javafx.collections.FXCollections.observableArrayList(
            "CRIADO", "PLANEJADO", "REFINADO", "EM_EXECUCAO", "EM_TESTES", "CONCLUIDO", "CANCELADO", "IMPEDIDO"
        ));
        statusComboBox.setValue("CRIADO");
        
        grid.add(new javafx.scene.control.Label("Título:"), 0, 0);
        grid.add(tituloField, 1, 0);
        grid.add(new javafx.scene.control.Label("Descrição:"), 0, 1);
        grid.add(descricaoArea, 1, 1);
        grid.add(new javafx.scene.control.Label("Duração (semanas):"), 0, 2);
        grid.add(duracaoSemanasField, 1, 2);
        grid.add(new javafx.scene.control.Label("Sprint:"), 0, 3);
        grid.add(sprintComboBox, 1, 3);
        grid.add(new javafx.scene.control.Label("Membro:"), 0, 4);
        grid.add(membroComboBox, 1, 4);
        grid.add(new javafx.scene.control.Label("Status:"), 0, 5);
        grid.add(statusComboBox, 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        
        javafx.application.Platform.runLater(() -> tituloField.requestFocus());
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == salvarButtonType) {
                if (tituloField.getText().trim().isEmpty()) {
                    showErrorAlert("Erro", "O título é obrigatório!");
                    return null;
                }
                
                if (sprintComboBox.getValue() == null) {
                    showErrorAlert("Erro", "A Sprint é obrigatória!");
                    return null;
                }
                
                ItemSprintDTO feature = new ItemSprintDTO();
                feature.setTipo(TipoItem.FEATURE);
                feature.setTitulo(tituloField.getText().trim());
                feature.setDescricao(descricaoArea.getText().trim());
                feature.setSprintId(sprintComboBox.getValue().getId());
                feature.setStatus(br.tec.jessebezerra.app.entity.StatusItem.valueOf(statusComboBox.getValue()));
                
                if (!duracaoSemanasField.getText().trim().isEmpty()) {
                    try {
                        feature.setDuracaoSemanas(Integer.parseInt(duracaoSemanasField.getText().trim()));
                    } catch (NumberFormatException e) {
                        showErrorAlert("Erro", "Duração deve ser um número válido!");
                        return null;
                    }
                }
                
                if (membroComboBox.getValue() != null) {
                    feature.setMembroId(membroComboBox.getValue().getId());
                }
                
                return feature;
            }
            return null;
        });
        
        java.util.Optional<ItemSprintDTO> result = dialog.showAndWait();
        result.ifPresent(feature -> {
            try {
                ItemSprintService itemSprintService = new ItemSprintService();
                itemSprintService.create(feature);
                showAlert("Sucesso", "Feature criada com sucesso!");
                loadAndBuildTimeline();
            } catch (Exception e) {
                showErrorAlert("Erro", "Erro ao salvar Feature: " + e.getMessage());
            }
        });
    }
    
    /**
     * Configura drag-and-drop para reordenação de SUBs
     */
    private void setupDragAndDrop(GridPane row, ItemSprintDTO item) {
        // Tornar a linha arrastável
        row.setOnDragDetected(event -> {
            if (item.getTipo() == TipoItem.SUB) {
                Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(item.getId().toString());
                db.setContent(content);
                row.setStyle(row.getStyle() + "-fx-opacity: 0.5;");
                event.consume();
            }
        });
        
        // Permitir drop sobre outras SUBs
        row.setOnDragOver(event -> {
            if (event.getGestureSource() != row && 
                event.getDragboard().hasString() &&
                item.getTipo() == TipoItem.SUB) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        
        // Destacar linha quando drag está sobre ela
        row.setOnDragEntered(event -> {
            if (event.getGestureSource() != row && 
                event.getDragboard().hasString() &&
                item.getTipo() == TipoItem.SUB) {
                row.setStyle(row.getStyle() + "-fx-border-color: #2196F3; -fx-border-width: 2;");
            }
            event.consume();
        });
        
        // Remover destaque quando drag sai
        row.setOnDragExited(event -> {
            row.setStyle(row.getStyle().replace("-fx-border-color: #2196F3; -fx-border-width: 2;", ""));
            event.consume();
        });
        
        // Executar drop
        row.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            
            if (db.hasString()) {
                try {
                    Long draggedSubId = Long.parseLong(db.getString());
                    Long targetSubId = item.getId();
                    
                    // Verificar se são SUBs do mesmo pai
                    ItemSprintDTO draggedSub = new ItemSprintService().findById(draggedSubId).orElse(null);
                    ItemSprintDTO targetSub = item;
                    
                    if (draggedSub != null && 
                        draggedSub.getItemPaiId() != null &&
                        draggedSub.getItemPaiId().equals(targetSub.getItemPaiId())) {
                        
                        // Obter todas as SUBs do mesmo pai ordenadas
                        List<ItemSprintDTO> subs = prioridadeService.getOrderedSubs(targetSub.getItemPaiId());
                        
                        // Encontrar posições
                        int targetPosition = -1;
                        for (int i = 0; i < subs.size(); i++) {
                            if (subs.get(i).getId().equals(targetSubId)) {
                                targetPosition = i;
                                break;
                            }
                        }
                        
                        if (targetPosition >= 0) {
                            // Reordenar
                            prioridadeService.reorderSub(draggedSubId, targetPosition);
                            
                            // Recarregar timeline
                            loadAndBuildTimeline();
                            success = true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showErrorAlert("Erro", "Erro ao reordenar SUB: " + e.getMessage());
                }
            }
            
            event.setDropCompleted(success);
            event.consume();
        });
        
        // Restaurar opacidade quando drag termina
        row.setOnDragDone(event -> {
            row.setStyle(row.getStyle().replace("-fx-opacity: 0.5;", ""));
            event.consume();
        });
        
        // Adicionar cursor de mão para indicar que é arrastável
        row.setOnMouseEntered(event -> row.setStyle(row.getStyle() + "-fx-cursor: hand;"));
        row.setOnMouseExited(event -> row.setStyle(row.getStyle().replace("-fx-cursor: hand;", "")));
    }
    
    @Override
    protected javafx.stage.Stage getCurrentStage() {
        return (javafx.stage.Stage) timelineContainer.getScene().getWindow();
    }
}
