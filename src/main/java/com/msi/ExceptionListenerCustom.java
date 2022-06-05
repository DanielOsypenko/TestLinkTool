package com.msi;

import com.msi.testlinkBack.ToolManager;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.locks.ReentrantLock;

public class ExceptionListenerCustom implements Runnable {

    Stage stage;


    private static final Logger logger = LoggerFactory.getLogger(ExceptionListenerCustom.class.getSimpleName());

    public ExceptionListenerCustom(Stage stage) {
        this.stage = stage;
    }

    static Popup createPopup(String message){
        final Popup popup = new Popup();
        popup.setAutoFix(true);
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);
        Label label = new Label(message);
        popup.getContent().add(label);
        label.setOnMouseReleased(e -> popup.hide());
        return popup;
    }

    public static void showPopupMessage(String message, Stage stage){
        logger.info("showing popup message:" + message);
        final Popup popup = createPopup(message);
        popup.setOnShown(windowEvent -> {
            popup.setX(stage.getX() + stage.getWidth()/2 - popup.getWidth()/2);
            popup.setY(stage.getY() + stage.getHeight()/2 - popup.getHeight()/2);
        });
        Platform.runLater(()->popup.show(stage));

    }

    @Override
    public void run() {
        while (true) {
            ReentrantLock lock = ToolManager.getManager().getLock();
            try {
                synchronized (lock) {
                    lock.wait();
                    showPopupMessage("TestLink error. Check your permissions", stage);
                    ToolManager.getManager().getTestProjectApi().getTestPlanApi().setTestPlan(null);
                    break;
                }
            } catch (Exception e) {
                logger.info("Unlocked "  + ExceptionUtils.getStackTrace(e));
            }
        }
    }
}

