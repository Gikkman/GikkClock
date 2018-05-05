package com.gikk.clock.util;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.dbutils.QueryRunner;
import org.h2.jdbcx.JdbcDataSource;

/**
 *
 * @author Gikkman
 */
public class Database {
    private final static JdbcDataSource DATASOURCE = new JdbcDataSource();
    private final static Logger LOGGER = Logger.getLogger(Database.class.getName());
    static{
        DATASOURCE.setURL("jdbc:h2:./database;mode=MySQL");
        DATASOURCE.setUser("user");
        DATASOURCE.setPassword("");

        QueryRunner qr = new QueryRunner(DATASOURCE);
        try {
            qr.execute("SELECT 1");
        }
        catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error creating database connection", ex);
            System.exit(1);
        }

        try {
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
        catch (SQLException ex) {
            LOGGER.log(Level.INFO, "Database already exists", ex);
        }
    }

    public QueryRunner getQueryRunner(){
        return new QueryRunner(DATASOURCE);
    }
}
