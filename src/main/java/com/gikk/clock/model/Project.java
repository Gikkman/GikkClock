package com.gikk.clock.model;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

/**
 *
 * @author Gikkman
 */
public class Project {
    /********************************************************
     * VARIABLES
     ********************************************************/
    private Integer projectID;
    private String name;

    /********************************************************
     * PUBLIC
     ********************************************************/
    @Override
    public String toString() {
        return name;
    }

    /********************************************************
     * GETTERS AND SETTERS
     ********************************************************/
    public Integer getProjectID() {
        return projectID;
    }

    public void setProjectID(Integer projectID) {
        this.projectID = projectID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /********************************************************
     * OVERRIDE
     ********************************************************/
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Project)){
            return false;
        }
        Project other = (Project) obj;
        return this.projectID.equals(other.projectID);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.projectID);
        return hash;
    }

    /********************************************************
     * INTERNAL DAO
     ********************************************************/
    static class DAO {
        private static final String TABLE_NAME = "`timers`.`project`";

        public static List<Project> getAll(QueryRunner qr) throws SQLException {
            String sql = "SELECT"
                        + " `project_id` AS `projectID`,"
                        + " `name` AS `name`"
                        + " FROM " + TABLE_NAME;
            BeanListHandler<Project> blh = new BeanListHandler<>(Project.class);
            return qr.query(sql, blh);
        }

        static Project create(QueryRunner qr, String name) throws SQLException {
            ScalarHandler<Integer> sh = new ScalarHandler<>();
            String sql = "INSERT INTO " + TABLE_NAME + "(`name`) VALUES (?)";
            Integer projectID = qr.insert(sql, sh, name);

            Project project = new Project();
            project.setName(name);
            project.setProjectID(projectID);
            return project;
        }

        static void delete(QueryRunner qr, Project project) throws SQLException {
            String sql = "DELETE FROM " + TABLE_NAME + " WHERE `project_id` = ?";
            qr.execute(sql, project.getProjectID());
        }
    }
}
