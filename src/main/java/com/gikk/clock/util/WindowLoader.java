package com.gikk.clock.util;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Gikkman
 */
public class WindowLoader<T extends WindowController>{
    /***************************************************************************
     *                          VARIABLES
     **************************************************************************/
    private final String fxml;

    private T controller = null;
    private Stage stage = null;
    private Scene scene = null;

    /***************************************************************************
     *                          STATIC
     **************************************************************************/
    public static CompletableFuture<WindowLoader> load(Stage owner, String title, String fxml) {
        CompletableFuture<WindowLoader> future = new CompletableFuture<>();

        if( Platform.isFxApplicationThread()) {
            doLoad(owner, future, title, fxml);
        } else {
            Platform.runLater( () -> {
                doLoad(owner, future, title, fxml);
            });
        }
        return future;
    }

    private static void doLoad(Stage owner, CompletableFuture<WindowLoader> future, String title, String fxml) {
        WindowLoader loader = new WindowLoader(fxml);
        loader.internalLoad(owner, title, "Styles.css", Stage::new );
        future.complete(loader);

    }

    /***************************************************************************
     *                          CONSTRUCTOR
     **************************************************************************/
    private WindowLoader(String fxml){
        this.fxml = fxml;
    }

    /***************************************************************************
     *                          GETTERS
     **************************************************************************/
    public Optional<T> getController(){
        return Optional.ofNullable(controller);
    }

    public Optional<Stage> getStage(){
        return Optional.ofNullable(stage);
    }

    public Optional<Scene> getScene(){
        return Optional.ofNullable(scene);
    }

    /***************************************************************************
     *                          PRIVATE
     **************************************************************************/
    private void internalLoad(Stage owner, String title, String css, Supplier<Stage> stageSupplier){
        try {
            final Class<?> clazz = getClass();
            final FXMLLoader loader = new FXMLLoader( clazz.getResource("/fxml/" + fxml) );
            scene = new Scene( loader.load() );
            scene.getStylesheets().add( clazz.getResource("/styles/" + css).toExternalForm() );
            controller = (T) loader.getController();
            if( controller == null ) {
                throw new IOException("No controller loaded for " + fxml + "!");
            }

            stage = stageSupplier.get();
            if( stage == null ) {
                throw new IOException("No stage loaded for " + fxml + "!");
            }
            stage.setTitle(title);
            stage.setScene(scene);

            stage.getIcons().add( owner.getIcons().get(0) );

            // Position popups relative to the main window
            ChangeListener<Number> widthListener = (observable, oldValue, newValue) -> {
                    double stageWidth = newValue.doubleValue();
                    stage.setX(owner.getX() + owner.getWidth() / 2 - stageWidth / 2);
            };
            ChangeListener<Number> heightListener = (observable, oldValue, newValue) -> {
                    double stageHeight = newValue.doubleValue();
                    stage.setY(owner.getY() + owner.getHeight() / 2 - stageHeight / 2);
            };
            stage.widthProperty().addListener(widthListener);
            stage.heightProperty().addListener(heightListener);

            //Once the window is visible, remove the listeners
            stage.setOnShown(e -> {
                stage.widthProperty().removeListener(widthListener);
                stage.heightProperty().removeListener(heightListener);
            });

        } catch (Exception ex) {
            Log.error("Error loading window " + title, ex);
        }
    }
}
