package com.gikk.clock;

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

public class ProjectSelectController implements Initializable, WindowController{
    private final ObservableList<Project> projectList = FXCollections.observableArrayList();

    @FXML private TextField textNewProject;
    @FXML private ComboBox<Project> comboboxPickExisting;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            QueryRunner qr = MainApp.getDatabase().getQueryRunner();
            List<Project> projects = ProjectManager.INSTANCE().getAll(qr);

            projectList.addAll(projects);
            comboboxPickExisting.setItems(projectList);

            // Set the choice to either the current, or the last of the list
            Optional<Project> currentProject = ProjectManager.INSTANCE().getCurrentProject();
            if(currentProject.isPresent() && projectList.size() > 0) {
                comboboxPickExisting.setValue(currentProject.get());
            }
            else if(projectList.size() > 0) {
                comboboxPickExisting.setValue(projectList.get( projectList.size() - 1));
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
        Stage stage = (Stage) textNewProject.getScene().getWindow();

        Project project = comboboxPickExisting.getValue();
        if(project == null) {
            AlertWindow.showInfo(
                stage,
                "Missing game",
                "You need to chose a game in the list.\n"
                + "Can't remove something that doesn't exist, ya?");
            return;
        }

        TextInputDialog dialogue = new TextInputDialog();
        dialogue.setTitle("Confirm");
        dialogue.setHeaderText("Confirm deleting project");
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
            // Delete the project and remove it from the chosable list
            QueryRunner qr = MainApp.getDatabase().getQueryRunner();
            ProjectManager.INSTANCE().delete(qr, project);
            projectList.remove(project);

            // Unset the current project, if it was the same as the one we deleted
            ProjectManager.INSTANCE()
                .getCurrentProject()
                .filter(p -> p.equals(project))
                .ifPresent(p -> ProjectManager.INSTANCE().unsetProject());

            // Unset the current game,
            // if the current game was of the deleted project
            GameManager.INSTANCE()
                .getCurrentGame()
                .filter(g -> g.getProject().equals(project))
                .ifPresent(g -> GameManager.INSTANCE().unsetGame());
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
        Project project = comboboxPickExisting.getValue();
        if(project == null) {
            Stage stage = (Stage) textNewProject.getScene().getWindow();
            AlertWindow.showInfo(
                stage,
                "Missing game",
                "You need to chose a game in the list.\n"
                + "Can't select remove something that doesn't exist, ya?");
            return;
        }

        try {
            QueryRunner qr = MainApp.getDatabase().getQueryRunner();

            // Set the chosen project as current project
            ProjectManager.INSTANCE().setProject(project);

            // Update the project timer
            GameManager.INSTANCE().updateProjectPlaytime(qr, project);

            // If the currently set game was of another project than the one we
            // just chose, unset it
            GameManager.INSTANCE()
                .getCurrentGame()
                .filter(g -> !g.getProject().equals(project))
                .ifPresent(g -> GameManager.INSTANCE().unsetGame());

            // Close window
            Stage stage = (Stage) textNewProject.getScene().getWindow();
            stage.close();
        } catch (Exception ex) {
            Stage stage = (Stage) textNewProject.getScene().getWindow();
            AlertWindow.showException(
                stage,
                "Error!",
                "Database write exception."
                + "Please screenshot this message and send to Gikk",
                ex);
        }
    }

    @FXML
    private void onCreateAndChose(){
        String name = textNewProject.getText();
        if(name.isEmpty()) {
            Stage stage = (Stage) textNewProject.getScene().getWindow();
            AlertWindow.showInfo(
                stage,
                "Missing info",
                "You need to assign a name to the project.\n"
                + "Believe me, it is easier for all of us that way.");
            return;
        }

        Stage stage = (Stage) textNewProject.getScene().getWindow();
        try {
            // Create the new project and set it as current
            QueryRunner qr = MainApp.getDatabase().getQueryRunner();
            Project project = ProjectManager.INSTANCE().create(qr, name);
            ProjectManager.INSTANCE().setProject(project);

            // Since we created a new project, the current game cannot be of
            // the project we just created. Thus, we unset it
            GameManager.INSTANCE().unsetGame();

            // Update the project timer
            GameManager.INSTANCE().updateProjectPlaytime(qr, project);

            // Close window
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
