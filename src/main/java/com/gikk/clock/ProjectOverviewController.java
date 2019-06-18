package com.gikk.clock;

import com.gikk.clock.model.Game;
import com.gikk.clock.model.GameManager;
import com.gikk.clock.model.Project;
import com.gikk.clock.model.ProjectManager;
import com.gikk.clock.util.AlertWindow;
import com.gikk.clock.util.TimeFormatter;
import com.gikk.clock.util.WindowController;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.apache.commons.dbutils.QueryRunner;

/**
 * FXML Controller class
 *
 * @author Gikkman
 */
public class ProjectOverviewController implements Initializable, WindowController {
    private final ObservableList<Project> projectList = FXCollections.observableArrayList();
    @FXML private ComboBox<Project> comboboxProject;

    private final ObservableList<GameWrapper> gameList = FXCollections.observableArrayList();
    @FXML private TableView<GameWrapper> table;
    @FXML private TableColumn<GameWrapper, Integer> colIndex;
    @FXML private TableColumn<GameWrapper, String> colDone;
    @FXML private TableColumn<GameWrapper, String> colTitle;
    @FXML private TableColumn<GameWrapper, String> colSystem;
    @FXML private TableColumn<GameWrapper, String> colPlaytime;
    
    @FXML private Label lbl_gamecount;
    @FXML private Label lbl_completecount;
    @FXML private Label lbl_longesttitle;
    @FXML private Label lbl_shortesttitle;
    @FXML private Label lbl_meantime;
    @FXML private Label lbl_mediantime;
    @FXML private Label lbl_longesttime;
    @FXML private Label lbl_shortesttime;
    
    @FXML private GridPane pane;
    
    private List<Game> currentProjectGames;
    
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
                onNewProject(nv);
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
        colIndex.setStyle( "-fx-alignment: CENTER;");
        
        colDone.setCellValueFactory( new PropertyValueFactory<>("done") );
        colDone.setStyle( "-fx-alignment: CENTER;");
        
        colTitle.setCellValueFactory( new PropertyValueFactory<>("title") );
        colTitle.setStyle( "-fx-alignment: CENTER;");
        
        colSystem.setCellValueFactory( new PropertyValueFactory<>("system") );
        colSystem.setStyle( "-fx-alignment: CENTER;");
        
        colPlaytime.setCellValueFactory( new PropertyValueFactory<>("playtime") );
        colPlaytime.setStyle( "-fx-alignment: CENTER;");
    }
    
    @FXML
    void onClose() {
        Stage stage = (Stage) pane.getScene().getWindow();
        stage.close();
    }
    
    private void onNewProject(Project p) {
        try {
            QueryRunner qr = MainApp.getDatabase().getQueryRunner();
            currentProjectGames = GameManager.INSTANCE().getAllForProject(qr, p);
            List<GameWrapper> wrapped = new ArrayList<>();
            
            for(int i = 0; i < currentProjectGames.size(); i++) {
                Game game = currentProjectGames.get(i);
                // i+1 for nice display indexes
                GameWrapper w= new GameWrapper(i+1, game.getCompleted(),
                        game.getTitle(), game.getSystem(), 
                        game.getPlaytimeSeconds());
                wrapped.add(w);
            }
            gameList.clear();
            gameList.addAll(wrapped);
            
            calculateProjectStats(currentProjectGames);
        } catch (Exception ex) {
            AlertWindow.showException(
            null,
            "Error!",
            "Game collection failed. "
            + "Please screenshot this message and send to Gikk",
            ex);
        } 
    }
    
    private void calculateProjectStats(List<Game> games) {
        // Game count
        lbl_gamecount.setText( String.valueOf(games.size()) );

        // Only work with complted games for calculating stats
        List<Game> completedGames = games.stream().filter(Game::getCompleted).collect(Collectors.toList());
        lbl_completecount.setText( String.valueOf(completedGames.size()) );

        if(completedGames.size() > 0) {
            // Calculate median lenght
            completedGames.sort( (left, right) -> {
                return Math.toIntExact(left.getPlaytimeSeconds() - right.getPlaytimeSeconds());
            });
            int middle = completedGames.size() / 2;
            int lowerIndex, upperIndex;
            if(completedGames.size() % 2 == 1) {
                lowerIndex = upperIndex = middle;
            }
            else {
                lowerIndex = middle - 1;
                upperIndex = middle;
            }
            Game lowerGame = completedGames.get(lowerIndex);
            Game upperGame = completedGames.get(upperIndex);
            long medianSeconds =  (lowerGame.getPlaytimeSeconds() + upperGame.getPlaytimeSeconds()) / 2;
            lbl_mediantime.setText( TimeFormatter.getHoursMinutesSeconds(medianSeconds));

            // Calculate mean lengt
            long meanTime = completedGames.stream().mapToLong(Game::getPlaytimeSeconds).sum() / completedGames.size();
            lbl_meantime.setText( TimeFormatter.getHoursMinutesSeconds(meanTime));

            //Find longest game
            Game longestGame = completedGames.get( completedGames.size() - 1);
            lbl_longesttitle.setText( longestGame.getTitle());
            lbl_longesttime.setText( TimeFormatter.getHoursMinutesSeconds( longestGame.getPlaytimeSeconds()));

            //Find shortest game
            Game shortestGame = completedGames.get( 0 );
            lbl_shortesttitle.setText( shortestGame.getTitle());
            lbl_shortesttime.setText( TimeFormatter.getHoursMinutesSeconds( shortestGame.getPlaytimeSeconds()));
        }
        else {
            lbl_mediantime.setText("0:00:00");
            lbl_meantime.setText("0:00:00");
            lbl_longesttime.setText("0:00:00");
            lbl_shortesttime.setText("0:00:00");
            lbl_longesttitle.setText("");
            lbl_shortesttitle.setText("");
        }
    }
    
    public static class GameWrapper {
        int index;
        boolean done;
        String title;
        String system;
        long playtimeSeconds;

        public GameWrapper(int index, boolean done, String title, String system, long playtimeSeconds) {
            this.index = index;
            this.done = done;
            this.title = title;
            this.system = system;
            this.playtimeSeconds = playtimeSeconds;
        }
        
        /********************************************************
        * PROPERTIES
        ********************************************************/
        public IntegerProperty indexProperty() {
            return new SimpleIntegerProperty(index);
        }

        public StringProperty titleProperty() {
            return new SimpleStringProperty(title);
        }

        public StringProperty systemProperty() {
            return new SimpleStringProperty(system);
        }

        public StringProperty playtimeProperty() {
            return new SimpleStringProperty(TimeFormatter.getHoursMinutesSeconds(playtimeSeconds));
        } 
        
        public StringProperty doneProperty() {
            return new SimpleStringProperty(done ? "X" : "");
        } 
    }
}
