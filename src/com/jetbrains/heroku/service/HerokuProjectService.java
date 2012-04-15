package com.jetbrains.heroku.service;

import com.heroku.api.*;
import com.heroku.api.exception.RequestFailedException;
import com.heroku.api.request.log.LogStreamResponse;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.jetbrains.heroku.notification.Notifications;
import com.jetbrains.heroku.git.GitHelper;
import com.jetbrains.heroku.ui.HerokuSetupWindow;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@State(name = "heroku-plugin-app", storages = @Storage(id = "heroku-plugin-app-name", file = "$WORKSPACE_FILE$"))
public class HerokuProjectService implements  PersistentStateComponent<HerokuProjectService.HerokuAppName> {

    private final static Logger LOG =Logger.getInstance(HerokuProjectService.class);

    public void stopApplication() {
        //this.herokuApi.stop(getHerokuAppName());
    }

    public List<Proc> getProcesses(String type) {
        final String prefix = type + ".";
        List<Proc> result=new ArrayList<Proc>();
        for (Proc proc : getProcesses()) {
            if (proc==null || proc.getProcess()==null) continue;
            if (proc.getProcess().startsWith(prefix)) {
                result.add(proc);
            }
        }
        return result;
    }

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
    transient private HerokuAPI herokuApi;
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
        if (this.herokuApi == null) {
            this.herokuApi = applicationService.getHerokuApi();
        }
        final String newAppName = newHerokuApp.name;
        try {
            if (newAppName !=null)
                this.app = this.herokuApi.getApp(newAppName);
            else
                this.app = null;
        } catch(RequestFailedException rfe) {
            LOG.error("Error updating app "+newAppName,rfe);
            Notifications.notifyError(project,"Request Error","Error retrieving app "+ newAppName,true,rfe);
        }
    }

    public App getApplicationInfo() {
        return this.app;
    }

    public Map<String, String> getApplicationConfig() {
        return this.herokuApi.listConfig(getHerokuAppName());
    }

    public List<Collaborator> getApplicationCollaborators() {
        return this.herokuApi.listCollaborators(getHerokuAppName());
    }

    public List<Addon> getApplicationAddOns() {
        return this.herokuApi.listAppAddons(getHerokuAppName());
    }

    public LogStreamResponse getApplicationLogStream() {
        return this.herokuApi.getLogs(getHerokuAppName());
    }
    public String getApplicationLogs() {
        final LogStreamResponse response = this.herokuApi.getLogs(getHerokuAppName());
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
        this.herokuApi.restart(this.app.getName());
    }

    public Project getProject() {
        return project;
    }

    public App getApp() {
        return this.app;
    }

    public void update(@Nullable App app) {
        final String appName = app == null ? null : app.getName();
        loadState(HerokuAppName.named(appName));
    }

    public List<Proc> getProcesses() {
        return this.herokuApi.listProcesses(getHerokuAppName());
    }

    public void scaleWeb(int count) {
        try {
            this.herokuApi.scaleProcess(getHerokuAppName(), "web", count);
        } catch (RequestFailedException rfe) {
            Notifications.notifyError(project, "Error scaling web processes", rfe.getResponseBody(), true, rfe);
        }
    }

    public void scaleWorkers(int count) {
        try {
            this.herokuApi.scaleProcess(getHerokuAppName(),"worker",count);
        } catch (RequestFailedException rfe) {
            Notifications.notifyError(project, "Error scaling workers", rfe.getResponseBody(), true, rfe);
        }
    }

    public List<Release> getReleases() {
        try {
            return this.herokuApi.listReleases(getHerokuAppName());
        } catch (RequestFailedException rfe) {
            // Notifications.notifyError(project, "Error retrieving releases", rfe.getResponseBody(), false, rfe);
            return Collections.emptyList();
        }
    }
    public Release getReleaseInfo(Release release) {
        try {
            return this.herokuApi.getReleaseInfo(getHerokuAppName(), release.getName());
        } catch (RequestFailedException rfe) {
            Notifications.notifyError(project, "Error retrieving release: " + release.getName(), rfe.getResponseBody(), false, rfe);
            return null;
        }
    }
    public void rollbackTo(Release release) {
        try {
            this.herokuApi.rollback(getHerokuAppName(),release.getName());
        } catch (RequestFailedException rfe) {
            Notifications.notifyError(project, "Error retrieving releases", rfe.getResponseBody(), false, rfe);
        }
    }

    public AddonChange addAddon(Addon addon) {
        return this.herokuApi.addAddon(getHerokuAppName(), addon.getName());
    }

    public AddonChange removeAddon(Addon addon) {
        return this.herokuApi.removeAddon(getHerokuAppName(),addon.getName());
    }


    public void addConfigVar(String name, String value) {
        this.herokuApi.addConfig(getHerokuAppName(),Collections.singletonMap(name,value));
    }

    public void removeConfigVar(String name) {
        this.herokuApi.removeConfig(getHerokuAppName(), name);
    }
    public void addCollaborator(String email) {
        this.herokuApi.addCollaborator(getHerokuAppName(),email);
    }
    public void removeCollaborator(String email) {
        this.herokuApi.removeCollaborator(getHerokuAppName(),email);
    }
}
