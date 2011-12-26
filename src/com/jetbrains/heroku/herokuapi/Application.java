package com.jetbrains.heroku.herokuapi;

import java.util.List;
import java.util.Map;

/**
 * @author mh
 * @since 17.12.11
 */
public class Application {
    private final String name;
    private final HerokuApi api;
    private Map<String, Object> info;

    public Application(String name, HerokuApi api, Map<String, Object> info) {
        this.name = name;
        this.api = api;
        this.info = info == null ? api.loadApplicationInfo(name) : info;
    }
    /*InputStream getLogs();

    void push();

    void ps(int dynos);

    void destroy();

    URL open(); // todo appurl for browser

    String getName();

    boolean rename(String newName);

    Map<String, Object> config(); // todo config object with add , remove
    */

    public Object get(String key) {
        return info != null ? info.get(key) : null;
    }

    public Map<String, Object> config() {
        return api.listApplicationConfig(name);
    }

    public List<Map<String, Object>> addons() {
        return api.listApplicationAddons(name);
    }

    public String logs() {
        return api.loadApplicationLogs(name, 10000);
    }

    public List<Map<String, Object>> collaborators() {
        return api.loadApplicationCollaborators(name);
    }

    public boolean restart() {
        return api.restartApplication(name);
    }

    public String getName() {
        return getString("name");
    }

    public Map<String, Object> getInfo() {
        return info;
    }

    public String getString(String key) {
        final Object result = get(key);
        return result != null ? result.toString() : null;
    }

    public String destroy() {
        return api.destroyApplication(name);
    }
}
