package com.heroku.idea.herokuapi;

import com.heroku.idea.rest.RestApi;
import com.heroku.idea.rest.RestException;
import com.heroku.idea.rest.RestFormatHelper;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.heroku.idea.rest.RestFormatHelper.*;

/*
information from heroku gem and later https://api-docs.heroku.com
 TODO PluginService ??

<extensions defaultExtensionNs="com.intellij">
    <!-- Declare the application level service -->
      <applicationService serviceInterface="Mypackage.MyServiceInterfaceClass" serviceImplementation="Mypackage.MyServiceImplClass">
      </applicationService>

    <!-- Declare the project level service -->
      <projectService serviceInterface="Mypackage.MyProjectServiceInterfaceClass" serviceImplementation="Mypackage.MyProjectServiceImplClass">
      </projectService>
 </extensions>

MyServiceImplClass service = ServiceManager.getService(MyServiceImplClass.class);
 */
public class HerokuApi {
    public static final Map<String, String> NO_OPTIONS = Collections.emptyMap();
    private static final String server = "api.heroku.com";
    private RestApi restApi;
    private Credentials credentials;

    public HerokuApi(Credentials credentials) {
        this.credentials = credentials;
        this.restApi = new RestApi(server, credentials.user(), credentials.token());
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public List<Application> allApps() {
        final String result = restApi.get("/apps", NO_OPTIONS);
        List data = (List) parseJson(result);
        List<Application> apps = new ArrayList<Application>();
        for (Map<String, Object> app : (Collection<Map<String, Object>>) data) {
            final Application application = new Application((String) app.get("name"), this, app);
            apps.add(application);
        }
        return apps;
    }

    public static Credentials login(String email, String password) {
        try {
            final String result = createRestApi().post("/login", NO_OPTIONS, format(map("username", email, "password", password)));
            Map<String, Object> map = (Map<String, Object>) RestFormatHelper.parse(result);
            Credentials credentials = new Credentials(map);
            if (!credentials.valid()) return null;
            return credentials;
        } catch (RestException re) {
            re.printStackTrace();
            return null;
        }
    }

    private static RestApi createRestApi() {
        return new RestApi(server);
    }

    public void logout() {

    }

    public Application getApplication(String name) {
        return new Application(name, this, loadApplicationInfo(name));
    }

    public Application create(String name, Map<String, String> params) {
        Map<String, String> payload = new HashMap<String, String>();
        payload.putAll(params);
        if (name!=null && !name.isEmpty()) payload.put("name",name);
        final Map<String, Object> info = (Map<String, Object>) parseJson(this.restApi.post("/apps", NO_OPTIONS, format(Collections.singletonMap("app", payload))));
        final String newName = info.get("name").toString();
        while (getApplicationStatus(name) !=null) {
            wait(1);
        }
        return new Application(newName, this, info);
    }

    public String getApplicationStatus(String name) {
        try {
            return this.restApi.get("/apps/"+name+"/status",NO_OPTIONS);
        } catch(RestException re) {
            if (re.getStatus()==404) return null;
            throw re;
        }
    }

    private void wait(int seconds) {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(seconds));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public String destroyApplication(String name) {
        return this.restApi.delete("/apps/"+name,RestFormatHelper.<String>map("Content-Type",null));
    }

    public RestApi getRestApi() {
        return restApi;
    }

    Map<String, Object> loadApplicationInfo(final String name) {
        final String info = getRestApi().get("/apps/" + name, NO_OPTIONS);
        return parseXml(info);
    }

    Map<String, Object> listApplicationConfig(final String name) {
        final String info = getRestApi().get("/apps/" + name + "/config_vars", NO_OPTIONS);
        return (Map<String, Object>) parseJson(info);
    }

    List<Map<String, Object>> listApplicationAddons(final String name) {
        final String info = getRestApi().get("/apps/" + name + "/addons", NO_OPTIONS);
        return (List<Map<String, Object>>) parseJson(info);
    }

    String loadApplicationLogs(final String name, final int length) {
        try {
            final RestApi restApi = getRestApi();
            final String logplexUrl = restApi.get("/apps/" + name + "/logs?logplex=true", RestFormatHelper.<String>map("Content-Type", null));
            return restApi.stream(logplexUrl, NO_OPTIONS, length);
        } catch(Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    List<Map<String, Object>> loadApplicationCollaborators(final String name) {
        try {
            final String info = getRestApi().get("/apps/" + name + "/collaborators", Collections.singletonMap("Accept", "text/xml"));
            return parseNestedXml(info);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    boolean restartApplication(final String name) {
        try {
            final String info = getRestApi().delete("/apps/" + name + "/server", NO_OPTIONS);
            System.out.println("restart = " + info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
