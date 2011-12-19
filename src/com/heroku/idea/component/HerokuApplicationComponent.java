package com.heroku.idea.component;

import com.heroku.idea.herokuapi.Application;
import com.heroku.idea.herokuapi.Credentials;
import com.heroku.idea.herokuapi.HerokuApi;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * @author mh
 * @since 17.12.11
 */
@State(name = "heroku-plugin", storages = @Storage(id = "heroku-plugin", file = "$APP_CONFIG$/heroku-plugin.app.xml"))
public class HerokuApplicationComponent implements ApplicationComponent, PersistentStateComponent<Credentials> {
    private Credentials credentials = null;
    private HerokuApi herokuApi;

    public HerokuApplicationComponent() { // inject dependencies
    }

    public void initComponent() {
        initApi(credentials);
    }

    // todo move to config
    public Credentials login(final String email, final String password) {
        final Credentials result = HerokuApi.login(email, password);
        if (result == null || !result.valid()) {
            Messages.showMessageDialog("Could not log into Heroku, please supply your credentials in the settings.", "Heroku Login Error", Messages.getErrorIcon());
        }
        return result;
    }

    public void disposeComponent() {
    }

    @NotNull
    public String getComponentName() {
        return "Heroku.ApplicationComponent";
    }

    public Credentials getState() {
        return credentials;
    }

    public void loadState(Credentials credentials) {
        this.credentials = credentials;
    }

    private void initApi(Credentials newCredentials) {
        this.credentials = newCredentials;
        this.herokuApi = null;
        if (this.credentials != null) {
            herokuApi = new HerokuApi(this.credentials); // todo make sure that escaped herokuApi instances are kept in sync
        }
    }

    public boolean isInitialized() {
        return herokuApi != null;
    }

    public Application getApplication(String name) {
        return herokuApi.getApplication(name);
    }

    public void update(String name, String password) {
        initApi(login(name, password));
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public List<Application> allApps(Credentials credentials) {
        return new HerokuApi(credentials).allApps();
    }
    public List<Application> allApps() {
        return herokuApi.allApps();
    }

    public Application createApplication(String applicationName) {
        return this.herokuApi.create(applicationName, Collections.<String,String>emptyMap());
    }
}
