package br.tec.jessebezerra.app.service;

import br.tec.jessebezerra.app.dto.ItemSprintDTO;
import br.tec.jessebezerra.app.dto.MembroDTO;
import br.tec.jessebezerra.app.dto.SprintDTO;
import br.tec.jessebezerra.app.entity.TipoItem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class TimelineExcelService {
    
    private static final String[] COLORS = {
        "#FFB6C1", "#FFD700", "#98FB98", "#87CEEB", "#DDA0DD",
        "#F0E68C", "#E0BBE4", "#FFDAB9", "#B0E0E6", "#FFE4B5"
    };
    
    private final TimelineService timelineService;
    
    public TimelineExcelService() {
        this.timelineService = new TimelineService();
    }
    
    public File exportToExcel(SprintDTO sprint, boolean showFeatures, boolean showHistorias, 
                              boolean showTarefas, boolean showSubs, boolean showProjeto, boolean showAplicacao,
                              MembroDTO membroFilter) throws IOException {
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Timeline - " + sprint.getNome());
        
        // Obter dias úteis da sprint
        List<LocalDate> workingDays = getWorkingDays(sprint.getDataInicio(), sprint.getDataFim());
        
        // Obter itens da timeline
        List<TimelineService.TimelineItem> timelineItems = timelineService.buildHierarchicalTimeline(sprint.getId());
        
        // Aplicar filtros
        timelineItems = applyFilters(timelineItems, showFeatures, showHistorias, showTarefas, showSubs, membroFilter);
        
        // Criar cabeçalho
        int currentRow = 0;
        currentRow = createHeader(workbook, sheet, sprint, workingDays, showProjeto, showAplicacao);
        
        // Criar linhas de itens
        createItemRows(workbook, sheet, timelineItems, workingDays, currentRow, showProjeto, showAplicacao);
        
        // Ajustar larguras das colunas
        adjustColumnWidths(sheet, workingDays.size(), showProjeto, showAplicacao);
        
        // Salvar arquivo
        File tempFile = File.createTempFile("timeline_" + sprint.getNome().replaceAll("\\s+", "_"), ".xlsx");
        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            workbook.write(outputStream);
        }
        workbook.close();
        
        return tempFile;
    }
    
    private List<LocalDate> getWorkingDays(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> workingDays = new ArrayList<>();
        LocalDate current = startDate;
        
        while (!current.isAfter(endDate)) {
            if (current.getDayOfWeek() != DayOfWeek.SATURDAY && 
                current.getDayOfWeek() != DayOfWeek.SUNDAY) {
                workingDays.add(current);
            }
            current = current.plusDays(1);
        }
        
        return workingDays;
    }
    
    private List<TimelineService.TimelineItem> applyFilters(
            List<TimelineService.TimelineItem> items,
            boolean showFeatures,
            boolean showHistorias,
            boolean showTarefas,
            boolean showSubs,
            MembroDTO membroFilter) {
        
        return items.stream().filter(timelineItem -> {
            ItemSprintDTO item = timelineItem.getItem();
            
            boolean typeMatch = false;
            if (item.getTipo() == TipoItem.FEATURE) {
                typeMatch = showFeatures;
            } else if (item.getTipo() == TipoItem.HISTORIA) {
                typeMatch = showHistorias;
            } else if (item.getTipo() == TipoItem.TAREFA) {
                typeMatch = showTarefas;
            } else if (item.getTipo() == TipoItem.SUB) {
                typeMatch = showSubs;
            }
            
            if (!typeMatch) {
                return false;
            }
            
            if (membroFilter != null && item.getMembroId() != null) {
                return item.getMembroId().equals(membroFilter.getId());
            }
            
            return true;
        }).collect(Collectors.toList());
    }
    
    private int createHeader(Workbook workbook, Sheet sheet, SprintDTO sprint, 
                            List<LocalDate> workingDays, boolean showProjeto, boolean showAplicacao) {
        
        int extraColumns = 0;
        if (showProjeto) extraColumns++;
        if (showAplicacao) extraColumns++;
        
        int totalDays = workingDays.size();
        
        // LINHA 0: ROADMAP + Nome da Sprint
        Row row0 = sheet.createRow(0);
        row0.setHeightInPoints(30);
        
        Cell roadmapCell = row0.createCell(0);
        roadmapCell.setCellValue("ROADMAP");
        roadmapCell.setCellStyle(createHeaderStyle(workbook, "#FFB6D9", true, 13));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2 + extraColumns));
        
        Cell sprintNameCell = row0.createCell(3 + extraColumns);
        sprintNameCell.setCellValue(sprint.getNome().toUpperCase());
        sprintNameCell.setCellStyle(createHeaderStyle(workbook, "#FFD4A3", true, 13));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 3 + extraColumns, 2 + extraColumns + totalDays));
        
        // LINHA 1: ITENS DA SPRINT + PESSOAS + PROJETO + APLICAÇÃO + Semanas
        Row row1 = sheet.createRow(1);
        row1.setHeightInPoints(25);
        
        Cell itensCell = row1.createCell(0);
        itensCell.setCellValue("ITENS DA SPRINT");
        itensCell.setCellStyle(createHeaderStyle(workbook, "#FFD4A3", true, 12));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 1));
        
        Cell pessoasCell = row1.createCell(2);
        pessoasCell.setCellValue("PESSOAS");
        pessoasCell.setCellStyle(createHeaderStyle(workbook, "#FFD4A3", true, 12));
        
        int currentCol = 3;
        
        if (showProjeto) {
            Cell projetoCell = row1.createCell(currentCol);
            projetoCell.setCellValue("PROJETO");
            projetoCell.setCellStyle(createHeaderStyle(workbook, "#FFD4A3", true, 12));
            currentCol++;
        }
        
        if (showAplicacao) {
            Cell aplicacaoCell = row1.createCell(currentCol);
            aplicacaoCell.setCellValue("APLICAÇÃO");
            aplicacaoCell.setCellStyle(createHeaderStyle(workbook, "#FFD4A3", true, 12));
            currentCol++;
        }
        
        // Adicionar semanas
        String[] weekColors = {"#A8D5E2", "#7FB3D5", "#5499C7"};
        int weekNumber = 1;
        int weekStartCol = currentCol;
        int daysInWeek = 0;
        
        for (int i = 0; i < workingDays.size(); i++) {
            LocalDate day = workingDays.get(i);
            daysInWeek++;
            
            boolean isEndOfWeek = day.getDayOfWeek() == DayOfWeek.FRIDAY || i == workingDays.size() - 1;
            
            if (isEndOfWeek) {
                String weekColor = weekColors[(weekNumber - 1) % weekColors.length];
                Cell weekCell = row1.createCell(weekStartCol);
                weekCell.setCellValue("S" + weekNumber);
                weekCell.setCellStyle(createHeaderStyle(workbook, weekColor, true, 11));
                
                if (daysInWeek > 1) {
                    sheet.addMergedRegion(new CellRangeAddress(1, 1, weekStartCol, weekStartCol + daysInWeek - 1));
                }
                
                weekNumber++;
                weekStartCol = currentCol + i + 1;
                daysInWeek = 0;
            }
        }
        
        // LINHA 2: TIPO + TÍTULO + MEMBRO + PROJETO + APLICAÇÃO + Dias
        Row row2 = sheet.createRow(2);
        row2.setHeightInPoints(20);
        
        Cell tipoCell = row2.createCell(0);
        tipoCell.setCellValue("TIPO");
        tipoCell.setCellStyle(createDetailHeaderStyle(workbook));
        
        Cell tituloCell = row2.createCell(1);
        tituloCell.setCellValue("TÍTULO");
        tituloCell.setCellStyle(createDetailHeaderStyle(workbook));
        
        Cell membroCell = row2.createCell(2);
        membroCell.setCellValue("MEMBRO");
        membroCell.setCellStyle(createDetailHeaderStyle(workbook));
        
        currentCol = 3;
        
        if (showProjeto) {
            Cell projetoDetailCell = row2.createCell(currentCol);
            projetoDetailCell.setCellValue("PROJETO");
            projetoDetailCell.setCellStyle(createDetailHeaderStyle(workbook));
            currentCol++;
        }
        
        if (showAplicacao) {
            Cell aplicacaoDetailCell = row2.createCell(currentCol);
            aplicacaoDetailCell.setCellValue("APLICAÇÃO");
            aplicacaoDetailCell.setCellStyle(createDetailHeaderStyle(workbook));
            currentCol++;
        }
        
        // Adicionar dias
        weekNumber = 1;
        daysInWeek = 0;
        
        for (int i = 0; i < workingDays.size(); i++) {
            LocalDate day = workingDays.get(i);
            daysInWeek++;
            
            boolean isEndOfWeek = day.getDayOfWeek() == DayOfWeek.FRIDAY || i == workingDays.size() - 1;
            
            if (isEndOfWeek) {
                String weekColor = weekColors[(weekNumber - 1) % weekColors.length];
                
                for (int j = 0; j < daysInWeek; j++) {
                    int dayIndex = i - daysInWeek + j + 1;
                    if (dayIndex >= 0 && dayIndex < workingDays.size()) {
                        LocalDate currentDay = workingDays.get(dayIndex);
                        Cell dayCell = row2.createCell(currentCol + j);
                        dayCell.setCellValue(currentDay.getDayOfMonth());
                        dayCell.setCellStyle(createDayHeaderStyle(workbook, weekColor));
                    }
                }
                
                currentCol += daysInWeek;
                weekNumber++;
                daysInWeek = 0;
            }
        }
        
        return 3;
    }
    
    private void createItemRows(Workbook workbook, Sheet sheet, 
                                List<TimelineService.TimelineItem> timelineItems,
                                List<LocalDate> workingDays, int startRow,
                                boolean showProjeto, boolean showAplicacao) {
        
        int currentRow = startRow;
        int colorIndex = 0;
        String currentFeatureColor = COLORS[0];
        
        // Rastrear alocação de dias por membro (para SUBs)
        Map<Long, Integer> memberAllocations = new HashMap<>();
        
        for (TimelineService.TimelineItem timelineItem : timelineItems) {
            ItemSprintDTO item = timelineItem.getItem();
            int indentLevel = timelineItem.getIndentLevel();
            
            // Calcular startDay e duration baseado no tipo de item
            int startDay = 0;
            int duration = timelineService.calculateDurationInDays(item);
            
            if (item.getTipo() == br.tec.jessebezerra.app.entity.TipoItem.SUB) {
                // SUBs: alocação sequencial por membro
                if (item.getMembroId() != null) {
                    startDay = memberAllocations.getOrDefault(item.getMembroId(), 0);
                    memberAllocations.put(item.getMembroId(), startDay + duration);
                }
            } else if (item.getTipo() == br.tec.jessebezerra.app.entity.TipoItem.TAREFA) {
                // Tarefas: usar startDay do TimelineItem
                startDay = timelineItem.getStartDay();
            } else if (item.getTipo() == br.tec.jessebezerra.app.entity.TipoItem.HISTORIA) {
                // Histórias: usar startDay do TimelineItem
                startDay = timelineItem.getStartDay();
            } else if (item.getTipo() == br.tec.jessebezerra.app.entity.TipoItem.FEATURE) {
                // Features: começam no dia 0
                startDay = 0;
            }
            
            String rowColor;
            if (indentLevel == 0) {
                currentFeatureColor = COLORS[colorIndex % COLORS.length];
                colorIndex++;
                rowColor = currentFeatureColor;
            } else if (indentLevel == 1) {
                rowColor = adjustBrightness(currentFeatureColor, 0.9);
            } else {
                rowColor = adjustBrightness(currentFeatureColor, 0.8);
            }
            
            Row row = sheet.createRow(currentRow);
            row.setHeightInPoints(25);
            
            // TIPO
            Cell tipoCell = row.createCell(0);
            tipoCell.setCellValue(getTipoLabel(item.getTipo()));
            tipoCell.setCellStyle(createItemCellStyle(workbook, rowColor, true, 10));
            
            // TÍTULO (com indentação)
            Cell tituloCell = row.createCell(1);
            String indent = "  ".repeat(indentLevel);
            tituloCell.setCellValue(indent + item.getTitulo());
            tituloCell.setCellStyle(createItemCellStyle(workbook, "#FFFFFF", false, 11));
            
            // MEMBRO
            Cell membroCell = row.createCell(2);
            membroCell.setCellValue(item.getMembroNome() != null ? item.getMembroNome() : "-");
            membroCell.setCellStyle(createItemCellStyle(workbook, "#FFFFFF", false, 11));
            
            int currentCol = 3;
            
            // PROJETO
            if (showProjeto) {
                Cell projetoCell = row.createCell(currentCol);
                projetoCell.setCellValue(item.getProjetoNome() != null ? item.getProjetoNome() : "-");
                projetoCell.setCellStyle(createItemCellStyle(workbook, "#FFFFFF", false, 11));
                currentCol++;
            }
            
            // APLICAÇÃO
            if (showAplicacao) {
                Cell aplicacaoCell = row.createCell(currentCol);
                aplicacaoCell.setCellValue(item.getAplicacaoNome() != null ? item.getAplicacaoNome() : "-");
                aplicacaoCell.setCellStyle(createItemCellStyle(workbook, "#FFFFFF", false, 11));
                currentCol++;
            }
            
            // DIAS
            int daysAllocated = 0;
            for (int i = 0; i < workingDays.size(); i++) {
                Cell dayCell = row.createCell(currentCol + i);
                
                if (i >= startDay && daysAllocated < duration) {
                    dayCell.setCellValue(workingDays.get(i).getDayOfMonth());
                    dayCell.setCellStyle(createItemCellStyle(workbook, rowColor, true, 10));
                    daysAllocated++;
                } else {
                    dayCell.setCellValue("");
                    dayCell.setCellStyle(createItemCellStyle(workbook, "#FFFFFF", false, 10));
                }
            }
            
            currentRow++;
        }
    }
    
    private void adjustColumnWidths(Sheet sheet, int totalDays, boolean showProjeto, boolean showAplicacao) {
        sheet.setColumnWidth(0, 15 * 256); // TIPO
        sheet.setColumnWidth(1, 50 * 256); // TÍTULO
        sheet.setColumnWidth(2, 20 * 256); // MEMBRO
        
        int currentCol = 3;
        
        if (showProjeto) {
            sheet.setColumnWidth(currentCol, 20 * 256); // PROJETO
            currentCol++;
        }
        
        if (showAplicacao) {
            sheet.setColumnWidth(currentCol, 20 * 256); // APLICAÇÃO
            currentCol++;
        }
        
        // Dias
        for (int i = 0; i < totalDays; i++) {
            sheet.setColumnWidth(currentCol + i, 5 * 256);
        }
    }
    
    private CellStyle createHeaderStyle(Workbook workbook, String hexColor, boolean bold, int fontSize) {
        XSSFWorkbook xssfWorkbook = (XSSFWorkbook) workbook;
        XSSFCellStyle style = xssfWorkbook.createCellStyle();
        
        Font font = workbook.createFont();
        font.setBold(bold);
        font.setFontHeightInPoints((short) fontSize);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);
        
        // Aplicar cor RGB personalizada
        byte[] rgb = hexToRgb(hexColor);
        XSSFColor color = new XSSFColor(rgb, null);
        style.setFillForegroundColor(color);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }
    
    private CellStyle createDetailHeaderStyle(Workbook workbook) {
        XSSFWorkbook xssfWorkbook = (XSSFWorkbook) workbook;
        XSSFCellStyle style = xssfWorkbook.createCellStyle();
        
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);
        
        // Cor cinza claro #E0E0E0
        byte[] rgb = hexToRgb("#E0E0E0");
        XSSFColor color = new XSSFColor(rgb, null);
        style.setFillForegroundColor(color);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }
    
    private CellStyle createDayHeaderStyle(Workbook workbook, String weekColor) {
        XSSFWorkbook xssfWorkbook = (XSSFWorkbook) workbook;
        XSSFCellStyle style = xssfWorkbook.createCellStyle();
        
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);
        
        // Aplicar cor mais clara da semana
        String lighterColor = adjustBrightness(weekColor, 1.3);
        byte[] rgb = hexToRgb(lighterColor);
        XSSFColor color = new XSSFColor(rgb, null);
        style.setFillForegroundColor(color);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }
    
    private CellStyle createItemCellStyle(Workbook workbook, String hexColor, boolean bold, int fontSize) {
        XSSFWorkbook xssfWorkbook = (XSSFWorkbook) workbook;
        XSSFCellStyle style = xssfWorkbook.createCellStyle();
        
        Font font = workbook.createFont();
        font.setBold(bold);
        font.setFontHeightInPoints((short) fontSize);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);
        
        // Aplicar cor RGB personalizada
        byte[] rgb = hexToRgb(hexColor);
        XSSFColor color = new XSSFColor(rgb, null);
        style.setFillForegroundColor(color);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }
    
    private String getTipoLabel(TipoItem tipo) {
        switch (tipo) {
            case FEATURE: return "FEATURE";
            case HISTORIA: return "HISTÓRIA";
            case TAREFA: return "TAREFA";
            case SUB: return "SUB";
            default: return "";
        }
    }
    
    private String adjustBrightness(String hexColor, double factor) {
        try {
            int r = Integer.parseInt(hexColor.substring(1, 3), 16);
            int g = Integer.parseInt(hexColor.substring(3, 5), 16);
            int b = Integer.parseInt(hexColor.substring(5, 7), 16);
            
            r = Math.min(255, (int)(r * factor));
            g = Math.min(255, (int)(g * factor));
            b = Math.min(255, (int)(b * factor));
            
            return String.format("#%02X%02X%02X", r, g, b);
        } catch (Exception e) {
            return hexColor;
        }
    }
    
    private byte[] hexToRgb(String hexColor) {
        try {
            int r = Integer.parseInt(hexColor.substring(1, 3), 16);
            int g = Integer.parseInt(hexColor.substring(3, 5), 16);
            int b = Integer.parseInt(hexColor.substring(5, 7), 16);
            return new byte[]{(byte) r, (byte) g, (byte) b};
        } catch (Exception e) {
            return new byte[]{(byte) 255, (byte) 255, (byte) 255};
        }
    }
}
