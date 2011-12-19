package com.heroku.idea.component;

import com.heroku.idea.herokuapi.Application;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@State(name = "heroku-plugin-app-name",storages = @Storage(id = "heroku-plugin-app-name",file = "$WORKSPACE_FILE$"))
public class HerokuProjectComponent implements ProjectComponent, PersistentStateComponent {
    transient private final Project project;
    transient private final HerokuApplicationComponent applicationComponent;
    transient private Application application;
    private Map<String,String> name=new HashMap<String, String>();

    public HerokuProjectComponent(Project project, HerokuApplicationComponent applicationComponent) {
        this.project = project;
        this.applicationComponent = applicationComponent;
    }

    public String getHerokuApplicationName() {
        return this.name.get("name");
    }

    public HerokuApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }

    public boolean isHerokuProject() {
        return this.name.containsKey("name");
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    @NotNull
    public String getComponentName() {
        return "Heroku.ProjectComponent";
    }

    public void projectOpened() {
        if (isHerokuProject()) {
            application = this.applicationComponent.getApplication(getHerokuApplicationName());
        }
    }

    public void projectClosed() {

    }

    public Object getState() {
        return name;
    }

    public void loadState(Object name) {
        this.name.clear();
        this.name.putAll((Map<String, String>)name);
        if (isHerokuProject()) {
            application = this.applicationComponent.getApplication(getHerokuApplicationName());
        }
    }

    public Map<String,Object> getApplicationInfo() {
        return this.application.getInfo();
    }

    public Map<String, Object> getApplicationConfig() {
        return this.application.config();
    }

    public List<Map<String, Object>> getApplicationCollaborators() {
        return this.application.collaborators();
    }

    public List<Map<String, Object>> getApplicationAddOns() {
        return this.application.addons();
    }

    public String getApplicationLogs() {
        return this.application.logs();
    }

    public void restartApplication() {
        this.application.restart();
    }

    public Project getProject() {
        return project;
    }

    public Application getApplication() {
        return this.application;
    }

    public void update(String name) {
        loadState(Collections.singletonMap("name", name));
    }
}
