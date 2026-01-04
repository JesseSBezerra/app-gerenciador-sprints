package br.tec.jessebezerra.app;

import br.tec.jessebezerra.app.config.DatabaseConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("sprint-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        
        stage.setTitle("Sistema de Gerenciamento de Sprints");
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