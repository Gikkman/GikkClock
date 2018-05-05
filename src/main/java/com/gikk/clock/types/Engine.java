package com.gikk.clock.types;

import com.gikk.clock.MainApp;
import com.gikk.clock.model.Game;
import com.gikk.clock.model.GameManager;
import com.gikk.clock.util.AlertWindow;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.commons.dbutils.QueryRunner;

/**
 *
 * @author Gikkman
 */
public class Engine {
    private ScheduledFuture future;
    private final Game thisGame;

    public Engine(Game game) {
        this.thisGame = game;
    }

    public void start() {
        future = MainApp.getExecutor().scheduleAtFixedRate(this::tick, 1, 1, TimeUnit.SECONDS);
    }

    public void stop() {
        if(future != null) {
            future.cancel(false);
            future = null;
        }
    }

    private void tick() {
        try {
            Optional<Game> currentGame = GameManager.INSTANCE().getCurrentGame();

            // Stop the engine if the current game's been unset
            if(!currentGame.isPresent()) {
                stop();
                return;
            }

            // Stop the engine if the current game's changed since this was created
            if(!currentGame.get().equals(thisGame)){
                stop();
                return;
            }

            // Update timers
            QueryRunner qr = MainApp.getDatabase().getQueryRunner();
            GameManager.INSTANCE().tickGamePlaytimer();
            GameManager.INSTANCE().flushCurrentGame(qr);
        }
        catch (Exception ex) {
            AlertWindow.showException(
                null,
                "Error!",
                "Timer update error"
                + "Please screenshot this message and send to Gikk",
                ex);
        }
    }
}
