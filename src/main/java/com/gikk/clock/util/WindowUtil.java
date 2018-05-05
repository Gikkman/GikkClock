package com.gikk.clock.util;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.stage.Stage;

/**
 *
 * @author Gikkman
 */
public class WindowUtil {
    private final static Preferences PREFERENCES = Preferences.userNodeForPackage(WindowUtil.class);
    private final static String WINDOW_X_POS = "windowXPos";
    private final static String WINDOW_Y_POS = "windowYPos";

    public static void lockWindowSize(Stage stage, int width, int height){
        stage.setMinWidth(width);
        stage.setMinHeight(height);
        stage.setWidth(width);
        stage.setHeight(height);
        stage.setMaxWidth(width);
        stage.setMaxHeight(height);
        stage.setResizable(false);
    }

    public static void loadPosition(Stage stage){
        try {
            String name = stage.getTitle();
            if( PREFERENCES.nodeExists(name) ){
                Preferences stagePreferences = PREFERENCES.node(name);
                stage.setX( stagePreferences.getDouble(WINDOW_X_POS, stage.getX()));
                stage.setY( stagePreferences.getDouble(WINDOW_Y_POS, stage.getY()));
            }
        } catch (BackingStoreException ex) {
            AlertWindow.showException(null, "Backing Store Exception", "Attempt to restore window position failed", ex);
        }
    }

    public static void savePosition(Stage stage){
        String name = stage.getTitle();
        try {
            Preferences stagePreferences = PREFERENCES.node(name);
            stagePreferences.putDouble(WINDOW_X_POS, stage.getX());
            stagePreferences.putDouble(WINDOW_Y_POS, stage.getY());
            stagePreferences.flush();
        } catch (final BackingStoreException ex) {
            AlertWindow.showException(null, "Backing Store Exception", "Attempt to save window position failed", ex);
        }
    }
}