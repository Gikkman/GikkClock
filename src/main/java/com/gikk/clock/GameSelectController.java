package com.gikk.clock;

import com.gikk.clock.model.Game;
import com.gikk.clock.model.GameManager;
import com.gikk.clock.model.Project;
import com.gikk.clock.model.ProjectManager;
import com.gikk.clock.util.AlertWindow;
import com.gikk.clock.util.WindowController;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.dbutils.QueryRunner;

/**
 * FXML Controller class
 *
 * @author Gikkman
 */
public class GameSelectController implements Initializable, WindowController {
    private final ObservableList<Game> gameList = FXCollections.observableArrayList();
    private Project project;

    @FXML private ComboBox<Game> comboboxPickExisting;

    @FXML private TextField newTitle;
    @FXML private TextField newSystem;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            project = ProjectManager.INSTANCE()
                        .getCurrentProject()
                        .orElseThrow(() -> new Exception("No project set"));

            QueryRunner qr = MainApp.getDatabase().getQueryRunner();
            List<Game> games = GameManager.INSTANCE().getAllForProject(qr, project);

            gameList.addAll(games);
            comboboxPickExisting.setItems(gameList);

            // Set the choice to either the current, or the last of the list
            Optional<Game> currentGame = GameManager.INSTANCE().getCurrentGame();
            if(currentGame.isPresent() && gameList.size() > 0) {
                comboboxPickExisting.setValue(currentGame.get());
            }
            else if(gameList.size() > 0) {
                comboboxPickExisting.setValue(gameList.get( gameList.size() - 1));
            }
        }
        catch (Exception ex) {
            AlertWindow.showException(
                null,
                "Error!",
                "Window creation failed. "
                + "Please screenshot this message and send to Gikk",
                ex);
        }
    }

    @FXML
    private void onDeleteExisting() {
        Stage stage = (Stage) newTitle.getScene().getWindow();

        Game game = comboboxPickExisting.getValue();
        if(game == null) {
            AlertWindow.showInfo(
                stage,
                "Missing game",
                "You need to chose a game in the list.\n"
                + "Can't remove something that doesn't exist, ya?");
            return;
        }

        TextInputDialog dialogue = new TextInputDialog();
        dialogue.setTitle("Confirm");
        dialogue.setHeaderText("Confirm deleting game");
        dialogue.setContentText("Type DELETE (in caps) in the box to confirm:");
        dialogue.initStyle(StageStyle.UTILITY);
        dialogue.initOwner(stage);
        boolean confirm = dialogue.showAndWait()
                            .filter(text -> text.equals("DELETE"))
                            .isPresent();
        if(!confirm) {
            return;
        }

        try{
            // Delete the chosen game
            QueryRunner qr = MainApp.getDatabase().getQueryRunner();
            GameManager.INSTANCE().delete(qr, game);
            gameList.remove(game);

            // If the deleted game was the current game, unset it
            GameManager.INSTANCE()
                .getCurrentGame()
                .filter(g -> g.equals(game))
                .ifPresent(g -> GameManager.INSTANCE().unsetGame());

            // If the deleted game was of the current project,
            // update it's total timer (check done internally)
            GameManager.INSTANCE().updateProjectPlaytime(qr, game);
        }
        catch (Exception ex) {
            AlertWindow.showException(
                stage,
                "Error!",
                "Database write exception."
                + "Please screenshot this message and send to Gikk",
                ex);
        }
    }

    @FXML
    private void onChoseExisting() {
        Game game = comboboxPickExisting.getValue();
        if(game == null) {
            Stage stage = (Stage) newTitle.getScene().getWindow();
            AlertWindow.showInfo(
                stage,
                "Missing game",
                "You need to chose a game in the list.\n"
                + "Can't chose something that doesn't exist, ya?");
            return;
        }

        try {
            // Set the chosen game as the current games
            QueryRunner qr = MainApp.getDatabase().getQueryRunner();
            GameManager.INSTANCE().setGame(qr, game);

            // Update the project total time
            GameManager.INSTANCE().updateProjectPlaytime(qr, game);
        }
        catch (Exception ex) {
            Stage stage = (Stage) newTitle.getScene().getWindow();
            AlertWindow.showException(
                stage,
                "Error!",
                "Database write exception."
                + "Please screenshot this message and send to Gikk",
                ex);
            return;
        }

        Stage stage = (Stage) newTitle.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onGotoSystem() {
        newSystem.requestFocus();
    }

    @FXML
    private void onCreateAndChose(){
        String title = newTitle.getText();
        String system = newSystem.getText();

        if(title.isEmpty() || system.isEmpty()) {
            Stage stage = (Stage) newTitle.getScene().getWindow();
            AlertWindow.showInfo(
                stage,
                "Missing infor",
                "You need to assign both a title and a system to the game.\n"
                + "Believe me, it is easier for all of us that way.");
            return;
        }

        Stage stage = (Stage) newTitle.getScene().getWindow();
        try {
            QueryRunner qr = MainApp.getDatabase().getQueryRunner();
            Game game = GameManager.INSTANCE().createGame(qr, project, title, system);
            GameManager.INSTANCE().setGame(qr, game);

            stage.close();
        }
        catch (Exception ex) {
            AlertWindow.showException(
                stage,
                "Error!",
                "Database write exception."
                + "Please screenshot this message and send to Gikk",
                ex);
        }
    }
}
