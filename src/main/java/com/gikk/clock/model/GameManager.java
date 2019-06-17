package com.gikk.clock.model;

import com.gikk.clock.types.ChangeListener;
import com.gikk.clock.types.Observable;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.apache.commons.dbutils.QueryRunner;

/**
 *
 * @author Gikkman
 */
public class GameManager {
    /********************************************************
     * GETTERS AND SETTERS
     ********************************************************/
    private final Observable<Game> game = new Observable<>(null);
    private final Observable<Long> gameTime = new Observable<>(0L);

    private GameManager(){};

    /********************************************************
     * GETTERS AND SETTERS
     ********************************************************/

    public Optional<Game> getCurrentGame() {
        return Optional.ofNullable(this.game.read());
    }

    public void setGame(QueryRunner qr, Game game) throws Exception {
        this.game.update(game);
        this.gameTime.update(game.getPlaytimeSeconds());
    }

    public void unsetGame() {
        this.game.delete();
        this.gameTime.delete();
    }

    public void addGameListener(ChangeListener<Game> l) {
        this.game.addListener(l);
    }



    public void addGamePlaytimeListener(ChangeListener<Long> l) {
        this.gameTime.addListener(l);
    }

    public void tickGamePlaytimer() throws Exception {
        this.getCurrentGame()
            .orElseThrow(() ->
                new Exception("Cannot tick timer. Game is missing")
            )
            .addPlaytimeSeconds(1L);

        // Update the in-memory timers
        this.gameTime.update( this.gameTime.read() + 1L );
        ProjectManager.INSTANCE().tickProjectPlaytime();
    }

    /********************************************************
     * UTIL
     ********************************************************/

    /**Update the current project time, but only if the parameter game
     * is of the same project as the currently selected project.
     *
     * @param qr
     * @param game
     * @throws Exception
     */
    public void updateProjectPlaytime(QueryRunner qr, Game game) throws Exception {
        boolean sameProject = ProjectManager.INSTANCE()
                                .getCurrentProject()
                                .filter(p -> game.getProject().equals(p))
                                .isPresent();
        if(sameProject) {
            Long projectPlaytime = Game.DAO.getPlaytimeForProject(qr, game.getProject());
            ProjectManager.INSTANCE().setProjectPlaytime(projectPlaytime);
        }
    }

    /**Update the current project time, but only if the parameter game
     * is of the same project as the currently selected project.
     * <p>
     * I know it is a bit strange to let this logic reside here, but I
     * wanted to isolate the knowledge of the Game table from the Project class.
     * Maybe it wasn't the best idea in the end, but I am too lazy to fix it now
     *
     * @param qr
     * @param game
     * @throws Exception
     */
    public void updateProjectPlaytime(QueryRunner qr, Project project) throws Exception {
        Long projectPlaytime = Game.DAO.getPlaytimeForProject(qr, project);
        ProjectManager.INSTANCE().setProjectPlaytime(projectPlaytime);

    }

    /********************************************************
     * PERSISTANCE ACCESS
     ********************************************************/

    public List<Game> getAllForProject(QueryRunner qr, Project project) throws Exception {
        return Game.DAO.getAllForProject(qr, project);
    }

    public void delete(QueryRunner qr, Game game) throws Exception {
        Game.DAO.delete(qr, game);
    }

    public Game createGame(QueryRunner qr, Project project, String title, String system) throws Exception {
        return Game.DAO.create(qr, project, title, system);
    }

    public void flushGame(QueryRunner qr, Game game) throws Exception {
        Game.DAO.flush(qr, game);
    }

    public void editGame(QueryRunner qr, Game game, String title, String system, Long seconds, Boolean completed) throws Exception {
        game.setTitle(title);
        game.setSystem(system);
        game.setPlaytimeSeconds(seconds);
        game.setCompleted(completed);
        Game.DAO.flush(qr, game);
    }

    /********************************************************
     * START AND SHUTDOWN
     ********************************************************/

    public void initiate(QueryRunner qr, Project project) throws Exception {
        String gameAsString = ConfigManager.getConfigOrDefault(qr, "game", null);
        if(gameAsString != null && !gameAsString.isEmpty() ) {
            Integer gameID = Integer.valueOf(gameAsString);
            Game gam = Game.DAO.getAllForProject(qr, project)
                    .stream()
                    .filter(g ->
                        g.getGameID().equals(gameID))
                    .findAny()
                    .orElseThrow(() ->
                        new Exception("Game " + gameID + " missing"));
            setGame(qr, gam);
        }
    }

    public void finalize(QueryRunner qr) {
        Game g = game.read();

        // Check that the game really is of the same project
        // (mostly a safety feature, in case it goes awry)
        boolean sameAsProject =
            g != null &&
            ProjectManager.INSTANCE()
                .getCurrentProject()
                .filter(p -> p.equals(g.getProject()))
                .isPresent();

        if(sameAsProject) {
            ConfigManager.putConfig(qr, "game", g.getGameID().toString());
        } else {
            ConfigManager.putConfig(qr, "game", null);
        }
    }

    /********************************************************
     * SINGLETON
     ********************************************************/
    public static GameManager INSTANCE() { return INTERNAL.INSTANCE; }

    static class INTERNAL {
        private static final GameManager INSTANCE = new GameManager();
    }
}
