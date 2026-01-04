package br.tec.jessebezerra.app;

import br.tec.jessebezerra.app.config.DatabaseConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("sprint-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        
        stage.setTitle("Sistema de Gerenciamento de Sprints");
        
        // Adicionar ícone da aplicação
        try {
            var iconStream = HelloApplication.class.getResourceAsStream("images/sgs.png");
            if (iconStream != null) {
                Image icon = new Image(iconStream);
                stage.getIcons().add(icon);
                System.out.println("Ícone carregado com sucesso!");
            } else {
                System.err.println("Arquivo de ícone não encontrado em: images/sgs.ico");
                // Tentar caminho alternativo
                iconStream = HelloApplication.class.getResourceAsStream("/br/tec/jessebezerra/app/images/sgs.ico");
                if (iconStream != null) {
                    Image icon = new Image(iconStream);
                    stage.getIcons().add(icon);
                    System.out.println("Ícone carregado com caminho alternativo!");
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar ícone da aplicação: " + e.getMessage());
            e.printStackTrace();
        }
        
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        
        stage.setOnCloseRequest(event -> {
            DatabaseConfig.closeConnection();
        });
        
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}