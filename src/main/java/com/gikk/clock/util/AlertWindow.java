package com.gikk.clock.util;

import com.gikk.clock.MainApp;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Gikkman
 */
public class AlertWindow {
    public static void showInfo(Stage owner, String title, String message){
        simpleAlert(AlertType.INFORMATION, owner, title, message);
    }

    public static void showWarning(Stage owner, String title, String message){
        simpleAlert(AlertType.WARNING, owner, title, message);
    }

    public static void showError(Stage owner, String title, String message){
        simpleAlert(AlertType.ERROR, owner, title, message);
    }

    public static void showException(Stage owner, String title, String header, Exception ex){
        Platform.runLater( () -> {
            Alert alert = formatAlert(AlertType.ERROR, owner, title, header, "An error occured. Please see the StackTrace bellow");

            // Create expandable Exception.
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            String exceptionText = sw.toString();

            Label label = new Label("The exception stacktrace was:");

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);

            // Set expandable Exception into the dialog pane.
            alert.getDialogPane().setExpandableContent(expContent);
            alert.getDialogPane().setExpanded(true);
            alert.showAndWait();
        });
    }

    public static Optional<ButtonType> showConfirm(Stage owner, String title, String header, String message ){
        CompletableFuture<Optional<ButtonType>> future = new CompletableFuture<>();

        Platform.runLater( () -> {
            Alert alert = formatAlert(AlertType.CONFIRMATION, owner, title, header, message);
            future.complete( alert.showAndWait() );
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException ex) { }
        return Optional.empty();
    }

    private static void simpleAlert(AlertType type, Stage owner, String title, String message){
        Platform.runLater( () -> {
            Alert alert = formatAlert(type, owner, title, null, message);
            alert.showAndWait();
        });
    }

    private static Alert formatAlert(AlertType type, Stage owner, String title, String header, String message){
        Alert alert = new Alert(type);
        alert.initOwner(owner != null ? owner : MainApp.getRoot() );
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);

        return alert;
    }
}
