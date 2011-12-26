package com.jetbrains.heroku.service;

import com.heroku.api.Addon;
import com.heroku.api.App;
import com.heroku.api.Collaborator;
import com.heroku.api.HerokuAPI;
import com.heroku.api.request.log.LogStreamResponse;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.jetbrains.heroku.git.GitHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

@State(name = "heroku-plugin-app", storages = @Storage(id = "heroku-plugin-app-name", file = "$WORKSPACE_FILE$"))
public class HerokuProjectService implements  PersistentStateComponent<HerokuProjectService.HerokuAppName> {
    public static class HerokuAppName {
        public String name;
        public static HerokuAppName named(String name) {
            final HerokuAppName herokuApp = new HerokuAppName();
            herokuApp.name = name;
            return herokuApp;
        }
    }
    transient private final Project project;
    transient private HerokuApplicationService applicationService;
    transient private App app;
    transient private HerokuAPI api;
    private HerokuAppName herokuAppName;
    
    public HerokuProjectService(Project project, HerokuApplicationService applicationService) {
        this.project = project;
        this.applicationService = applicationService;
    }

    public String getHerokuAppName() {
        return this.app.getName();
    }

    public HerokuApplicationService getApplicationService() {
        return applicationService;
    }

    public boolean isHerokuProject() {
        return this.app != null && GitHelper.isGitEnabled(project);
    }

    public HerokuAppName getState() {
        return herokuAppName;
    }


    public void loadState(HerokuAppName newHerokuApp) {
        this.herokuAppName = newHerokuApp;
        if (this.api==null) {
            this.api = applicationService.getHerokuApi();
        }
        this.app = this.api.getApp(newHerokuApp.name);
    }

    public App getApplicationInfo() {
        return this.app;
    }

    public Map<String, String> getApplicationConfig() {
        return this.api.listConfig(getHerokuAppName());
    }

    public List<Collaborator> getApplicationCollaborators() {
        return this.api.listCollaborators(getHerokuAppName());
    }

    public List<Addon> getApplicationAddOns() {
        return this.api.listAppAddons(getHerokuAppName());
    }

    public String getApplicationLogs() {
        final LogStreamResponse response = this.api.getLogs(getHerokuAppName());
        final InputStream inputStream = response.openStream();
        return readString(inputStream, 10 * 1024);
    }

    private String readString(InputStream inputStream, final int size) {
        BufferedReader reader=null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            final char[] buffer = new char[size];
            reader.read(buffer);
            reader.close();
            return String.valueOf(buffer);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return ioe.getMessage();
        } finally {
            closeReader(reader);
        }
    }

    private void closeReader(BufferedReader reader) {
        if (reader == null) return;
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void restartApplication() {
        // TODO this.app.restart();
    }

    public Project getProject() {
        return project;
    }

    public App getApp() {
        return this.app;
    }

    public void update(App app) {
        loadState(HerokuAppName.named(app.getName()));
    }
}
