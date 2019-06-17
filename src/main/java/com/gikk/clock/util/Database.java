package com.gikk.clock.util;

import com.gikk.clock.MainApp;
import com.gikk.clock.model.ConfigManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.dbutils.QueryRunner;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.Server;

/**
 *
 * @author Gikkman
 */
public class Database {
    private final static JdbcDataSource DATASOURCE = new JdbcDataSource();
    private final static Logger LOGGER = Logger.getLogger(Database.class.getName());
    private static Server server;
    static{
        try {
            server = Server.createTcpServer("-baseDir", MainApp.getLocation()).start();
            String url = "localhost:" + server.getPort();
            LOGGER.log(Level.INFO, "DB url: {0}", url);

            DATASOURCE.setURL("jdbc:h2:" + url + "/database;MODE=MySQL");
            DATASOURCE.setUser("user");
            DATASOURCE.setPassword("");
        }
        catch (SQLException ex) {
            LOGGER.log(Level.INFO, "Could not create server", ex);
            System.exit(-1001);
        }


        QueryRunner qr = new QueryRunner(DATASOURCE);
        try {
            qr.execute("SELECT 1");
        }
        catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error creating database connection", ex);
            System.exit(1);
        }

        try {            
            boolean up = true;
            while(up)
            switch(ConfigManager.getConfigOrDefault(qr, "db_version", 0)) {
                case 0:
                    System.out.println("Up database from 0 -> 1");
                    upDatabaseTo1(qr);
                    ConfigManager.putConfig(qr, "db_version", "1");
                    System.out.println("Upped database from 0 -> 1");
                    break;
                case 1:
                    System.out.println("Up database from 1 -> 2");
                    upDatebaseTo2(qr);
                    ConfigManager.putConfig(qr, "db_version", "2");
                    System.out.println("Upped database from 1 -> 2");
                    break;
                default:
                    up = false;
            }
        }
        catch (SQLException ex) {
            LOGGER.log(Level.INFO, "Database already exists", ex);
        }
    }

    public static void shutdown() {
        server.stop();
    }

    public QueryRunner getQueryRunner(){
        return new QueryRunner(DATASOURCE);
    }

    private static void upDatabaseTo1(QueryRunner qr) throws SQLException {
        qr.execute("CREATE SCHEMA IF NOT EXISTS `timers`");

        qr.execute(
              "CREATE TABLE IF NOT EXISTS `timers`.`config` ("
            + " `key` VARCHAR(50) NOT NULL,"
            + " `value` VARCHAR(50),"
            + "PRIMARY KEY (`key`));"
        );

        qr.execute(
              "CREATE TABLE IF NOT EXISTS `timers`.`project` ("
            + " `project_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,"
            + " `created` DATETIME NOT NULL DEFAULT NOW(),"
            + " `updated` DATETIME NOT NULL DEFAULT NOW(),"
            + "`name` VARCHAR(50) NOT NULL,"
            + "PRIMARY KEY (`project_id`));"
        );

        qr.execute(
            "CREATE TABLE IF NOT EXISTS `timers`.`game` ("
          + " `game_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,"
          + " `project_id` INT UNSIGNED NOT NULL,"
          + " `created` DATETIME NOT NULL DEFAULT NOW(),"
          + " `updated` DATETIME NOT NULL DEFAULT NOW(),"
          + " `title` VARCHAR(100) NOT NULL,"
          + " `system` VARCHAR(100) NOT NULL,"
          + " `playtime_seconds` INT UNSIGNED NOT NULL DEFAULT 0,"
              + " PRIMARY KEY (`game_id`),"
          + " CONSTRAINT `fk_game_projectid`"
              + " FOREIGN KEY (`project_id`)"
              + " REFERENCES `timers`.`project` (`project_id`)"
              + " ON DELETE CASCADE"
              + " ON UPDATE CASCADE);"
        );
    }
    
    private static void upDatebaseTo2(QueryRunner qr) throws SQLException {
        qr.execute(
            "ALTER TABLE `timers`.`game`"
            + " ADD COLUMN IF NOT EXISTS"
            + " `completed` INT DEFAULT 0 NOT NULL"
            + " AFTER `playtime_seconds`");
    }
}
