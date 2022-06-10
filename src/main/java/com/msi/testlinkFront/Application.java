package com.msi.testlinkFront;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Objects;

public class Application extends javafx.application.Application {

    Controller controller;
    private static final Logger logger = LoggerFactory.getLogger(Application.class.getSimpleName());

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("main-view.fxml")));
        controller = fxmlLoader.getController();

        Scene scene = new Scene(root, 1028, 720);
        stage.setTitle("TestLink Tool");
        stage.getIcons().add(new Image("tl-logo.png"));
        stage.setScene(scene);
        scene.getStylesheets().add("styles.css");
        stage.setResizable(false);
        stage.show();

        // close the process by pushing 'X' btn
        stage.setOnCloseRequest(windowEvent -> {
            Platform.exit();
            logger.info("Closing the app. Shutting down process.");
            System.exit(0);
        });
    }

    private void dispose (){

    }

    public static void main(String[] args) {
        launch();
    }
}