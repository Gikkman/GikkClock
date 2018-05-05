package com.gikk.clock.model;

import com.gikk.clock.types.ChangeListener;
import com.gikk.clock.types.Observable;
import java.util.List;
import java.util.Optional;
import org.apache.commons.dbutils.QueryRunner;

/**
 *
 * @author Gikkman
 */
public class ProjectManager {
    /********************************************************
     * VARIABLES
     ********************************************************/
    private final Observable<Project> project = new Observable<>(null);
    private final Observable<Long> projectPlaytime = new Observable<>(0L);

    private ProjectManager() {}

    /********************************************************
     * GETTERS AND SETTERS
     ********************************************************/

    public Optional<Project> getCurrentProject() {
        return Optional.ofNullable(this.project.read());
    }

    public void setProject(Project project) {
        this.project.update(project);
    }

    public void unsetProject() {
        this.project.delete();
        this.projectPlaytime.delete();
    }

    public void addProjectListener(ChangeListener<Project> l) {
        this.project.addListener(l);
    }


    public Long getCurrentPlaytime() {
        return this.projectPlaytime.read();
    }

    void setProjectPlaytime(Long playtime) {
        this.projectPlaytime.update(playtime);
    }

    public void addProjectPlaytimeListener(ChangeListener<Long> l) {
        this.projectPlaytime.addListener(l);
    }

    void tickProjectPlaytime() {
        this.projectPlaytime.update( this.projectPlaytime.read() + 1L );
    }

    /********************************************************
     * PERSISTANCE ACCESS
     ********************************************************/

    public List<Project> getAll(QueryRunner qr) throws Exception {
        return Project.DAO.getAll(qr);
    }

    public Project create(QueryRunner qr, String name) throws Exception {
        return Project.DAO.create(qr, name);
    }

    public void delete(QueryRunner qr, Project project) throws Exception {
        Project.DAO.delete(qr, project);
    }

    /********************************************************
     * STARTUP AND SHUTDOWN
     ********************************************************/

    public void initiate(QueryRunner qr) throws Exception {
        String projectAsString = ConfigManager.getConfigOrDefault(qr, "project", null);
        if(projectAsString != null && !projectAsString.isEmpty()) {
            Integer projectID = Integer.valueOf(projectAsString);
            Project proj = Project.DAO.getAll(qr)
                    .stream()
                    .filter(p ->
                        p.getProjectID().equals(projectID))
                    .findAny()
                    .orElseThrow(() ->
                        new Exception("Project " + projectID + " missing"));
            setProject(proj);
            GameManager.INSTANCE().updateProjectPlaytime(qr, proj);
        }
    }

    public void finalize(QueryRunner qr) {
        if(project.read() != null) {
            ConfigManager.putConfig(qr, "project", project.read().getProjectID().toString());
        }
    }

    /********************************************************
     * SINGLETON
     ********************************************************/
    public static ProjectManager INSTANCE() { return INTERNAL.INSTANCE; }
    static class INTERNAL {
        private static final ProjectManager INSTANCE = new ProjectManager();
    }
}
