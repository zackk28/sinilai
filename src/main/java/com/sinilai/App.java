package com.sinilai;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sinilai/view/LoginView.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("Register");
        primaryStage.setScene(scene);

        // Set ukuran minimum agar tidak mengecil terlalu kecil
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(500);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}