package com.gikk.clock;

import com.gikk.clock.model.Game;
import com.gikk.clock.model.GameManager;
import com.gikk.clock.util.AlertWindow;
import com.gikk.clock.util.TimeFormatter;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.commons.dbutils.QueryRunner;

/**
 * FXML Controller class
 *
 * @author Gikkman
 */
public class GameEditController implements Initializable {

    @FXML private TextField title;
    @FXML private TextField system;
    @FXML private TextField text_hh;
    @FXML private TextField text_mm;
    @FXML private TextField text_ss;

    private Game game;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            String time = TimeFormatter.getHoursMinutesSeconds(game.getPlaytimeSeconds());
            String[] segments = time.split(":");

            text_hh.setText(segments[0]);
            text_mm.setText(segments[1]);
            text_ss.setText(segments[2]);

            makeNumberFields(text_hh, "[0-9]*", "0");
            makeNumberFields(text_mm, "[0-5][0-9]", "00");
            makeNumberFields(text_ss, "[0-5][0-9]", "00");

            game = GameManager.INSTANCE()
                .getCurrentGame()
                .orElseThrow( () -> new Exception() );
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
    void onApply(ActionEvent event) {
        Stage stage = (Stage) title.getScene().getWindow();

        if(title.getText().isEmpty() || system.getText().isEmpty()) {
            AlertWindow.showInfo(
                stage,
                "Missing info",
                "You need to assign both a title and a system to the game.\n"
                + "Believe me, it is easier for all of us that way.");
            return;
        }

        Long hh, mm, ss;
        try{
            hh = Long.parseLong(text_hh.getText());
            mm = Long.parseLong(text_mm.getText());
            ss = Long.parseLong(text_ss.getText());
        } catch (NumberFormatException e) {
            AlertWindow.showInfo(
                stage,
                "Missing info",
                "Hour, minutes and seconds needs to be numbers!");
            return;
        }
        Long seconds = TimeFormatter.getSeconds(hh, mm, ss);

       try {
            QueryRunner qr = MainApp.getDatabase().getQueryRunner();
            GameManager.INSTANCE().editGame(qr, game, title.getText(), system.getText(), seconds);

            GameManager.INSTANCE().setGame(qr, game);
            GameManager.INSTANCE().updateProjectPlaytime(qr, game);

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

    @FXML
    void onCancel(ActionEvent event) {
        Stage stage = (Stage) title.getScene().getWindow();
        stage.close();
    }

    private void makeNumberFields(TextField field, String regex, String defaultVal) {
        field.textProperty().addListener((obs, ov, nv) -> {
            if(nv.isEmpty()) {

            } else if(!nv.matches(regex)) {
                field.setText(ov);
            }
        });
        field.focusedProperty().addListener( (obs, ov, nv) -> {
            if(!nv && field.getText().isEmpty()) {
                field.setText(defaultVal);
            }
        });
    }
}
