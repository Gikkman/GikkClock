package com.gikk.clock.model;

import com.gikk.clock.util.TimeFormatter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

/**
 *
 * @author Gikkman
 */
public class Game {
    /********************************************************
     * VARIABLES
     ********************************************************/
    private Integer gameID;
    private Project project;
    private String title;
    private String system;
    private Long playtimeSeconds;
    private Boolean completed;

    /********************************************************
     * PUBLIC
     ********************************************************/
    @Override
    public String toString() {
        return title + " (" + system + ")";
    }

    /********************************************************
     * GETTERS AND SETTERS
     ********************************************************/
    public Integer getGameID() {
        return gameID;
    }

    void setGameID(Integer gameID) {
        this.gameID = gameID;
    }

    public Project getProject() {
        return project;
    }

    void setProject(Project project) {
        this.project = project;
    }

    public String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    public String getSystem() {
        return system;
    }

    void setSystem(String system) {
        this.system = system;
    }

    public Long getPlaytimeSeconds() {
        return playtimeSeconds;
    }

    void setPlaytimeSeconds(Long seconds) {
        this.playtimeSeconds = seconds;
    }

    void addPlaytimeSeconds(Long toAdd) {
        this.playtimeSeconds += toAdd;
    }
    
    void setCompleted(Boolean completed) {
        this.completed = completed;
    }
    
    public Boolean getCompleted() {
        return this.completed;
    }
    
    /********************************************************
     * PROPERTIES
     ********************************************************/
    public IntegerProperty indexProperty() {
        return new SimpleIntegerProperty(gameID);
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
    
    public BooleanProperty completedProperty() {
        return new SimpleBooleanProperty(completed);
    } 

    /********************************************************
     * OVERRIDE
     ********************************************************/

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Game)) {
            return false;
        }
        Game other = (Game) obj;
        return this.gameID.equals(other.gameID);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.gameID);
        return hash;
    }

    /********************************************************
     * INTERNAL DAO
     ********************************************************/
    static class DAO {
        private static final String TABLE_NAME = "`timers`.`game`";

        static Game create(QueryRunner qr, Project project, String title, String system) throws SQLException {
            ScalarHandler<Integer> sh = new ScalarHandler<>();
            String sql = "INSERT INTO "
                         + TABLE_NAME
                         + " (`project_id`, `title`, `system`)"
                         + " VALUES (?,?,?)";

            Integer gameID = qr.insert(sql, sh, project.getProjectID(), title, system);

            Game game = new Game();
            game.setGameID(gameID);
            game.setProject(project);
            game.setSystem(system);
            game.setTitle(title);
            game.setPlaytimeSeconds(0L);
            game.setCompleted(Boolean.FALSE);
            return game;
        }

        static void flush(QueryRunner qr, Game game) throws SQLException {
            String sql = "UPDATE "
                         + TABLE_NAME
                         + " SET"
                         + " `title` = ?,"
                         + " `system` = ?,"
                         + " `playtime_seconds` = ?,"
                         + " `completed` = ?,"
                         + " `updated` = NOW()"
                         + " WHERE `game_id` = ?";
            qr.update(sql,
                      game.getTitle(),
                      game.getSystem(),
                      game.getPlaytimeSeconds(),
                      game.getCompleted(),
                      game.getGameID());
        }

        static Long getPlaytimeForProject(QueryRunner qr, Project project) throws SQLException {
            ScalarHandler<Long> sh = new ScalarHandler<>();
            String sql = "SELECT"
                         + " SUM(`playtime_seconds`)"
                         + " FROM " + TABLE_NAME
                         + " WHERE `project_id` = ?";
            return qr.query(sql, sh, project.getProjectID());
        }
        
        static Long getCompletedCountForProject(QueryRunner qr, Project project) throws SQLException {
            ScalarHandler<Long> sh = new ScalarHandler<>();
            String sql = "SELECT"
                    + " COUNT(*)"
                    + " FROM " + TABLE_NAME
                    + " WHERE `project_id` = ?"
                    + " AND `completed` = 1";
            return qr.query(sql, sh, project.getProjectID());        
        }

        static List<Game> getAllForProject(QueryRunner qr, Project project) throws SQLException {
            MapListHandler mlh = new MapListHandler();
            String sql = "SELECT"
                         + " `game_id`, `title`, `system`, `completed`,"
                         + " SUM(`playtime_seconds`) as playtime_seconds"
                         + " FROM " + TABLE_NAME
                         + " WHERE `project_id` = ?"
                         + " GROUP BY `game_id`";
            List<Map<String, Object>> rows = qr.query(sql, mlh, project.getProjectID());

            List<Game> output = new ArrayList<>();
            for(Map<String, Object> row : rows) {
                Game game = new Game();
                game.setGameID((Integer) row.get("game_id"));
                game.setProject(project);
                game.setSystem((String) row.get("system"));
                game.setTitle((String) row.get("title"));
                game.setPlaytimeSeconds((Long) row.get("playtime_seconds"));
                game.setCompleted(((Integer) row.get("completed")) == 1);
                output.add(game);
            }
            return output;
        }

        static void delete(QueryRunner qr, Game game) throws SQLException {
            String sql = "DELETE FROM " + TABLE_NAME  + " WHERE `game_id` = ?";
            qr.execute(sql, game.getGameID());
        }
    }
}
