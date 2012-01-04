package com.jetbrains.heroku.service;

import com.heroku.api.*;
import com.heroku.api.exception.LoginFailedException;
import com.heroku.api.exception.RequestFailedException;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import com.jetbrains.heroku.notification.Notifications;
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
    private static final Logger LOG = Logger.getInstance(HerokuApplicationService.class);

    public HerokuApplicationService() { // inject dependencies
    }

    // todo move to config
    public Credentials login(final String token) {
        if (!validateToken(token)) {
            showCredentialsError();
            return new Credentials(null);
        }
        return new Credentials(token);
    }

    public boolean validateToken(String token) {
        try {
            final HerokuAPI herokuAPI = new HerokuAPI(token);
            herokuAPI.listKeys();
            return true;
        } catch (RequestFailedException rfe) {
            LOG.warn("Error validating token "+token,rfe);
        } catch (IllegalStateException ise) {
            LOG.warn("Error validating token "+token,ise);
        }
        return false;
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
        if (this.credentials != null && this.credentials.valid()) {
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

    public void update(String token) {
        initApi(login(token));
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public List<App> allApps(Credentials credentials) {
        return new HerokuAPI(credentials.getToken()).listApps();
    }

    public List<App> listApps() {
        if (!isInitialized()) {
            showCredentialsError();
            return Collections.emptyList();
        }
        return herokuApi.listApps();
    }

    public App createApplication(String applicationName, Heroku.Stack stack) {
        if (!isInitialized()) {
            showCredentialsError();
            return null;
        }
        App app = new App().on(stack);
        if (applicationName != null && !applicationName.isEmpty()) {
            app = app.named(applicationName);
        }
        return this.herokuApi.createApp(app);
    }

    public List<Addon> getAllAddons() {
        return this.herokuApi.listAllAddons();
    }

    public List<Key> getKeys() {
        return this.herokuApi.listKeys();
    }

    public String obtainApiToken(String email, String password) {
        try {
            return HerokuAPI.obtainApiKey(email, password);
        } catch(LoginFailedException lfe) {
            LOG.warn("Failed to authenticate "+email+" "+lfe.getMessage());
            return null;
        }
    }

    public List<Key> listKeys() {
        return herokuApi.listKeys();
    }

    public void addKey(String key) {
        try {
            herokuApi.addKey(key);
        } catch (RequestFailedException rfe) {
            Notifications.notifyError(null, "Error Adding key", rfe.getResponseBody(), true, rfe);
        }
    }

    public void removeKey(Key key) {
        try {
            herokuApi.removeKey(key.getEmail());
        } catch (RequestFailedException rfe) {
            Notifications.notifyError(null, "Error Adding key", rfe.getResponseBody(), false, rfe);
        }
    }

    public void destroyApp(App app) {
        try {
            if (app == null) return;
            herokuApi.destroyApp(app.getName());
        } catch (RequestFailedException rfe) {
            Notifications.notifyError(null, "Error Destroying App " + app.getName(), rfe.getResponseBody(), true, rfe);
        }
    }
}
