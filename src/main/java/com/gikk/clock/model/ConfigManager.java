package com.gikk.clock.model;

import com.gikk.clock.util.Log;
import java.sql.SQLException;
import org.apache.commons.dbutils.BaseResultSetHandler;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 *
 * @author Gikkman
 */
public class ConfigManager {
    private static final String TABLE_NAME = "`timers`.`config`";

    public static String getConfigOrDefault(QueryRunner qr, String key, String defaultValue) {
        ResultSetHandler<String> rsh = new BaseResultSetHandler<String>() {
            @Override
            protected String handle() throws SQLException {
                if(!next()) {
                    return null;
                }
                else {
                    String obj = getString(1);
                    if(obj == null) {
                        return null;
                    }
                    return obj;
                }
            }
        };
        try{
            String val = qr.query("SELECT `value` FROM " + TABLE_NAME + " WHERE `key` = ?" , rsh, key);
            if(val == null) {
                return defaultValue;
            }
            return val;
        }
        catch (SQLException ex) {
            Log.info("Getting config failed", ex);
        }
        return defaultValue;
    }

    public static void putConfig(QueryRunner qr, String key, String value) {
        try {
            qr.execute("INSERT INTO "
                       + TABLE_NAME
                       + " (`key`,`value`) VALUES (?,?)"
                       + " ON DUPLICATE KEY UPDATE"
                       + " `value` = ?",
                       key, value, value);
        }
        catch (SQLException ex) {
            Log.info("Putting config failed", ex);
        }
    }
}
