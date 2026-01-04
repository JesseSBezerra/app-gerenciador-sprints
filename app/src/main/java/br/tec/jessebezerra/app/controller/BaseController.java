package br.tec.jessebezerra.app.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Classe base para controllers, compartilhando comportamentos comuns.
 */
public abstract class BaseController {
    
    @FXML protected Button menuToggleButton;
    @FXML protected VBox sideMenu;
    
    protected boolean menuExpanded = true;
    
    /**
     * Alterna expansão/colapso do menu lateral
     */
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
    
    /**
     * Navega para outra tela mantendo o tamanho da janela
     */
    protected void navigateTo(String fxmlPath, String title) {
        try {
            Stage stage = getCurrentStage();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(
                getClass().getResource(fxmlPath)
            );
            javafx.scene.Scene scene = new javafx.scene.Scene(
                fxmlLoader.load(), 
                currentWidth, 
                currentHeight
            );
            stage.setScene(scene);
            stage.setTitle(title);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erro de Navegação", "Erro ao navegar: " + e.getMessage());
        }
    }
    
    /**
     * Obtém o Stage atual
     */
    protected abstract Stage getCurrentStage();
    
    /**
     * Exibe alerta de informação
     */
    protected void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Exibe alerta de erro
     */
    protected void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Exibe alerta de aviso
     */
    protected void showWarningAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // Métodos de navegação padrão
    
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
        navigateTo("/br/tec/jessebezerra/app/historia-view.fxml", "Gerenciamento de Histórias");
    }
    
    @FXML
    protected void onNavigateToTarefas() {
        navigateTo("/br/tec/jessebezerra/app/tarefa-view.fxml", "Gerenciamento de Tarefas");
    }
    
    @FXML
    protected void navigateToTimeline() {
        navigateTo("/br/tec/jessebezerra/app/timeline-view.fxml", "Timeline da Sprint");
    }
    
    @FXML
    protected void onNavigateToProjetos() {
        navigateTo("/br/tec/jessebezerra/app/projeto-view.fxml", "Gerenciamento de Projetos");
    }
    
    @FXML
    protected void onNavigateToAplicacoes() {
        navigateTo("/br/tec/jessebezerra/app/aplicacao-view.fxml", "Gerenciamento de Aplicações");
    }
    
    @FXML
    protected void onNavigateToMembros() {
        navigateTo("/br/tec/jessebezerra/app/membro-view.fxml", "Gerenciamento de Membros");
    }
}
