package com.msi.testlinkFront;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

public class Application extends javafx.application.Application {

    Controller controller;
    private static final java.util.logging.Logger jfxLogger =
            java.util.logging.Logger.getLogger(Application.class.getSimpleName());

    @Override
    public void start(Stage stage) throws IOException {
//        setJfxLogger();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        jfxLogger.info(">>>>>>> Test Link Tool start "+ formatter.format(new Date()) + " <<<<<<<");
        FXMLLoader fxmlLoader = new FXMLLoader();

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("main-view.fxml")));
        controller = fxmlLoader.getController();

        Scene scene = new Scene(root, 1280, 720);
        stage.setTitle("TestLink Tool");
        stage.getIcons().add(new Image("tl-logo.png"));
        stage.setScene(scene);
        scene.getStylesheets().add("styles.css");
        stage.setResizable(false);
        stage.show();

        // close the process by pushing 'X' btn
        stage.setOnCloseRequest(windowEvent -> {
            Platform.exit();
            jfxLogger.info("Closing the app. Shutting down process.");
            System.exit(0);
        });
    }

    private void setJfxLogger() throws IOException {
        jfxLogger.setLevel(Level.FINEST);
        FileHandler fh = new FileHandler("testLink.log", (1048576 * 30), 1000);
        fh.setFormatter(new SimpleFormatter());
        jfxLogger.addHandler(fh);
    }

    public static void main(String[] args) {
        launch();
    }
}