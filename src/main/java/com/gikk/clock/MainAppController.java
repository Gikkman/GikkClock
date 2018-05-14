package com.gikk.clock;

import com.gikk.clock.model.Game;
import com.gikk.clock.model.GameManager;
import com.gikk.clock.model.Project;
import com.gikk.clock.model.ProjectManager;
import com.gikk.clock.types.Engine;
import com.gikk.clock.util.AlertWindow;
import com.gikk.clock.util.FileUtil;
import com.gikk.clock.util.TimeFormatter;
import com.gikk.clock.util.WindowController;
import com.gikk.clock.util.WindowLoader;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainAppController implements Initializable {
    /********************************************************
     * VARIABLES
     ********************************************************/

    @FXML private Button buttonStart;

    @FXML private Label labelProjectName;
    @FXML private Label labelGameTitle;
    @FXML private Label labelGameSystem;
    @FXML private Label labeProjectTimer;
    @FXML private Label labelGameTimer;

    private Engine engine;

    /********************************************************
     * INIT
     ********************************************************/

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Bind project name
        ProjectManager.INSTANCE().addProjectListener( (p) -> {
            MainApp.runFx( () -> {
                labelProjectName.setText( p == null ? "" : p.getName() );
            });
        });
        // Bind project timer
        ProjectManager.INSTANCE().addProjectPlaytimeListener( (t) -> {
            MainApp.runFx( () -> {
                labeProjectTimer. setText(TimeFormatter.getHoursMinutesSeconds(t));
            });
        });
        // Bind game title and system
        GameManager.INSTANCE().addGameListener( (g) -> {
            MainApp.runFx( () -> {
                labelGameTitle.setText( g == null ? "" : g.getTitle() );
                labelGameSystem.setText( g == null ? "" : g.getSystem() );
            });
        });
        // Bind game timer
        GameManager.INSTANCE().addGamePlaytimeListener( (t) -> {
            MainApp.runFx( () -> {
                labelGameTimer. setText(TimeFormatter.getHoursMinutesSeconds(t));
            });
        });

        // Sync values to file
        FileUtil.sync("game_title.txt", labelGameTitle.textProperty());
        FileUtil.sync("game_system.txt", labelGameSystem.textProperty());
        FileUtil.sync("game_timer.txt", labelGameTimer.textProperty());

        FileUtil.sync("project_name.txt", labelProjectName.textProperty());
        FileUtil.sync("project_timer.txt", labeProjectTimer.textProperty());

        // Set button action
        buttonStart.setOnAction(e -> onStart());
    }

    /********************************************************
     * ENGINE
     ********************************************************/

    @FXML
    private void onStart() {
        Optional<Project> project = ProjectManager.INSTANCE().getCurrentProject();
        if(!project.isPresent()) {
            Stage stage = (Stage) buttonStart.getScene().getWindow();
            AlertWindow.showInfo(
                stage,
                "No project selected!",
                "You gotta select a project before you can start the timers.\n"
                + "Check out Edit -> Set Current Project in the menu above.");
            return;
        }

        Optional<Game> game = GameManager.INSTANCE().getCurrentGame();
        if(!game.isPresent()) {
            Stage stage = (Stage) buttonStart.getScene().getWindow();
            AlertWindow.showInfo(
                stage,
                "No game selected!",
                "You gotta select a game before you can start the timers.\n"
                + "Check out Edit -> Set Current Game in the menu above.");
            return;
        }

        engine = new Engine(game.get());
        engine.start();

        buttonStart.setText("Stop");
        buttonStart.setOnAction(e -> onStop());
    }

    private void onStop() {
        engine.stop();

        buttonStart.setText("Start");
        buttonStart.setOnAction(e -> onStart());
    }

    /********************************************************
     * EXIT
     ********************************************************/

    @FXML
    private void onExit() {
        Platform.exit();
    }

    /********************************************************
     * PROJECT
     ********************************************************/
    @FXML
    private void onSetProject() {
        Stage stage = (Stage) buttonStart.getScene().getWindow();

        if(engine != null && engine.isRunning()) {
            AlertWindow.showInfo(
                stage,
                "Timers are running!",
                "Please don't edit stuff while timers are running. It creates "
                + "a lot of side effects and I'm too lazy to solve them.");
            return;
        }

        openWindowAndWait("Set Current Project", "ProjectSelect.fxml");
    }

    /********************************************************
     * GAME
     ********************************************************/
    @FXML
    private void onSetGame() {
        Stage stage = (Stage) buttonStart.getScene().getWindow();

        if(engine != null && engine.isRunning()) {
            AlertWindow.showInfo(
                stage,
                "Timers are running!",
                "Please don't edit stuff while timers are running. It creates "
                + "a lot of side effects and I'm too lazy to solve them.");
            return;
        }

        Optional<Project> project = ProjectManager.INSTANCE().getCurrentProject();
        if(project.isPresent()) {
            openWindowAndWait("Set Current Game", "GameSelect.fxml");
        }
        else {
            AlertWindow.showInfo(
                stage,
                "No project selected!",
                "You gotta select a project before you can select a game.\n"
                + "Check out Edit -> Set Current Project in the menu above.");
        }
    }

    @FXML
    private void onEditGame() {
        Stage stage = (Stage) buttonStart.getScene().getWindow();

        if(engine != null && engine.isRunning()) {
            AlertWindow.showInfo(
                stage,
                "Timers are running!",
                "Please don't edit stuff while timers are running. It creates "
                + "a lot of side effects and I'm too lazy to solve them.");
            return;
        }

        Optional<Game> game = GameManager.INSTANCE().getCurrentGame();
        if(game.isPresent()) {
            openWindowAndWait("Edit Current Game", "GameEdit.fxml");
        }
        else {
            AlertWindow.showInfo(
                stage,
                "No game selected!",
                "You gotta select a game before you can edit it.\n"
                + "Check out Edit -> Set Current Game in the menu above.");
        }
    }

    /********************************************************
     * ABOUT
     ********************************************************/

    @FXML
    private void onAbout() {
        openWindowAndWait("About", "About.fxml");
    }

    /********************************************************
     * UTIL
     ********************************************************/

    private void openWindowAndWait(String title, String fxml) {
        try {
            Stage owner = (Stage) buttonStart.getScene().getWindow();

            CompletableFuture<WindowLoader> loader = WindowLoader.load(owner, title, fxml);
            WindowLoader<WindowController> window = loader.get(10,TimeUnit.SECONDS);

            Stage stage =
                window.getStage().orElseThrow(() -> new Exception("Stage missing"));

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        }
        catch (Exception ex) {
            Stage stage = (Stage) buttonStart.getScene().getWindow();
            AlertWindow.showException(
                stage,
                "Error!",
                "Window creation error"
                + "Please screenshot this message and send to Gikk",
                ex);
        }
    }
}
