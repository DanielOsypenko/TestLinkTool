package com.msi.testlinkFront;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1028, 720);
        stage.setTitle("Test Link Tool");
        stage.setScene(scene);
        scene.getStylesheets().add("styles.css");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}