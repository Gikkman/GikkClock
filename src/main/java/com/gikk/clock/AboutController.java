package com.gikk.clock;

import com.gikk.clock.util.AlertWindow;
import com.gikk.clock.util.WindowController;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Gikkman
 */
public class AboutController implements Initializable, WindowController {

    @FXML Button buttonClose;

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    @FXML
    private void onClose() {
        Stage stage = (Stage) buttonClose.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onHyperlink() {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI("https://github.com/Gikkman/GikkClock"));
            }
            else {
                throw new IOException();
            }
        }
        catch (URISyntaxException | IOException ex) {
            Stage stage = (Stage) buttonClose.getScene().getWindow();
            AlertWindow.showError(
                stage,
                "URI Error",
                "Well... this is embarasing by apparently this URI does not work.\n"
                + "Guess you gotta copy-paste it manually into your browser.");
        }
    }
}
