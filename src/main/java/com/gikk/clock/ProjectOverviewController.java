package com.gikk.clock;

import com.gikk.clock.model.Game;
import com.gikk.clock.model.GameManager;
import com.gikk.clock.model.Project;
import com.gikk.clock.model.ProjectManager;
import com.gikk.clock.util.AlertWindow;
import com.gikk.clock.util.TimeFormatter;
import com.gikk.clock.util.WindowController;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.commons.dbutils.QueryRunner;

/**
 * FXML Controller class
 *
 * @author Gikkman
 */
public class ProjectOverviewController implements Initializable, WindowController {
    private final ObservableList<Project> projectList = FXCollections.observableArrayList();
    @FXML private ComboBox<Project> comboboxProject;

    private final ObservableList<Game> gameList = FXCollections.observableArrayList();
    @FXML private TableView<Game> table;
    @FXML private TableColumn<Game, Integer> colIndex;
    @FXML private TableColumn<Game, String> colTitle;
    @FXML private TableColumn<Game, String> colSystem;
    @FXML private TableColumn<Game, String> colPlaytime;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try{
            QueryRunner qr = MainApp.getDatabase().getQueryRunner();
            
            List<Project> projects = ProjectManager.INSTANCE().getAll(qr);
            projectList.addAll(projects);
            comboboxProject.setItems(projectList);
            comboboxProject.valueProperty().addListener((obs, ov, nv) -> {
                try {
                    List<Game> games = GameManager.INSTANCE().getAllForProject(qr, nv);
                    gameList.clear();
                    gameList.addAll(games);
                } catch (Exception ex) {
                    AlertWindow.showException(
                    null,
                    "Error!",
                    "Game collection failed. "
                    + "Please screenshot this message and send to Gikk",
                    ex);
                }
            });
            Optional<Project> current = ProjectManager.INSTANCE().getCurrentProject();
            current.ifPresent(comboboxProject::setValue);
        }
        catch (Exception ex) {
            AlertWindow.showException(
                null,
                "Error!",
                "Project collection failed. "
                + "Please screenshot this message and send to Gikk",
                ex);
        }
        
        // Setup main table
        table.setItems(gameList);
        colIndex.setCellValueFactory( new PropertyValueFactory<>("index") );
        colTitle.setCellValueFactory( new PropertyValueFactory<>("title") );
        colSystem.setCellValueFactory( new PropertyValueFactory<>("system") );
        colPlaytime.setCellValueFactory( new PropertyValueFactory<>("playtime") );
    }
}
