package com.jetbrains.heroku.service;

import com.heroku.api.App;
import com.heroku.api.HerokuAPI;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.ui.Messages;
import com.jetbrains.heroku.herokuapi.Credentials;

import java.util.Collections;
import java.util.List;

/**
 * @author mh
 * @since 17.12.11
 */
@State(name = "heroku-plugin", storages = @Storage(id = "heroku-plugin", file = "$APP_CONFIG$/heroku-plugin.app.xml"))
public class HerokuApplicationService implements PersistentStateComponent<Credentials> {
    private Credentials credentials = null;
    private transient HerokuAPI herokuApi;

    public HerokuApplicationService() { // inject dependencies
    }

    // todo move to config
    public Credentials login(final String email, final String token) {
        final HerokuAPI herokuAPI = new HerokuAPI(token);
        final String apiKey = herokuAPI.getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            showCredentialsError();
        }
        return new Credentials(email, apiKey);
    }

    private static void showCredentialsError() {
        Messages.showMessageDialog("Could not log into Heroku, please supply your credentials in the settings.", "Heroku Login Error", Messages.getErrorIcon());
    }

    public Credentials getState() {
        return credentials;
    }

    public void loadState(Credentials credentials) {
        initApi(credentials);
    }

    private void initApi(Credentials newCredentials) {
        this.credentials = newCredentials;
        this.herokuApi = null;
        if (this.credentials != null) {
            herokuApi = new HerokuAPI(this.credentials.getToken()); // todo make sure that escaped herokuApi instances are kept in sync
        }
    }

    public boolean isInitialized() {
        return herokuApi != null;
    }

    public App getApplication(String name) {
        return herokuApi.getApp(name);
    }

    public HerokuAPI getHerokuApi() {
        return herokuApi;
    }

    public void update(String name, String password) {
        initApi(login(name, password));
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public List<App> allApps(Credentials credentials) {
        return new HerokuAPI(credentials.getToken()).listApps();
    }

    public List<App> allApps() {
        if (!isInitialized()) {
            showCredentialsError();
            return Collections.emptyList();
        }
        return herokuApi.listApps();
    }

    public App createApplication(String applicationName) {
        if (!isInitialized()) {
            showCredentialsError();
            return null;
        }
        return this.herokuApi.createApp(new App().named(applicationName));
    }
}
