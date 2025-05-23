package com.gameadvisor.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class GameAdvisorClient extends Application {

    @Override
    public void start(Stage primaryStage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        
        Label label = new Label("GameAdvisor Client\n" + 
            "Running on Java " + javaVersion + "\n" +
            "JavaFX " + javafxVersion);

        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 800, 600);
        
        primaryStage.setTitle("GameAdvisor");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}