package com.gikk.clock;

import com.gikk.clock.model.ConfigManager;
import com.gikk.clock.model.GameManager;
import com.gikk.clock.model.Project;
import com.gikk.clock.model.ProjectManager;
import com.gikk.clock.util.Database;
import com.gikk.clock.util.Log;
import com.gikk.clock.util.WindowUtil;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.commons.dbutils.QueryRunner;

import static javafx.application.Application.*;


public class MainApp extends Application {
    private static String fileLocation;
    private static Window rootWindow;
    private static Database db;
    private static ScheduledExecutorService executor;

    @Override
    public void start(Stage stage) throws Exception {
        initialize();

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainScene.fxml"));

        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");

        stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/icon.png")));
        stage.setTitle("Gikk Clock");
        stage.setScene(scene);

        WindowUtil.loadPosition(stage);

        stage.show();

        // On shutdown mechanics
        stage.setOnHiding(e -> {
            WindowUtil.savePosition(stage);
        });

        stage.setOnHidden( (e) -> {
            Log.info("Shutting down");
            try {
                executor.shutdown();
                    executor.awaitTermination(10, TimeUnit.SECONDS);
                if(!executor.isTerminated()) {
                    executor.shutdownNow();
                }
            }
            catch (InterruptedException ex) {
                Log.error("Shutting down executor failed", ex);
            }

            QueryRunner qr = db.getQueryRunner();
            ProjectManager.INSTANCE().finalize(qr);
            GameManager.INSTANCE().finalize(qr);
            Database.shutdown();

            Log.info("Shutting completed");
        });

        this.rootWindow = stage;
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    public static Window getRoot() {
        return rootWindow;
    }

    public static Database getDatabase() {
        return db;
    }

    public static ScheduledExecutorService getExecutor(){
        return executor;
    }

    public static void runFx(Runnable runnable){
        if( Platform.isFxApplicationThread() ) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }

    public static String getLocation() {
        return fileLocation;
    }

    private void initialize() throws Exception {
        fileLocation = new File(
                MainApp.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI())
            .getParent();

        this.db = new Database();
        this.executor = new ScheduledThreadPoolExecutor(1);

        QueryRunner qr = this.db.getQueryRunner();

        String projectAsString = ConfigManager.getConfigOrDefault(qr, "project", null);
        if(projectAsString != null ) {
            ProjectManager.INSTANCE().initiate(qr);
            Optional<Project> project = ProjectManager.INSTANCE().getCurrentProject();

            String gameAsString = ConfigManager.getConfigOrDefault(qr, "game", null);
            if(project.isPresent() && gameAsString != null) {
                GameManager.INSTANCE().initiate(qr, project.get());
            }
        }

    }
}
