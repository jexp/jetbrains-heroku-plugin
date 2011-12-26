package com.jetbrains.heroku.herokuapi;

import com.jetbrains.heroku.herokuapi.Application;
import com.jetbrains.heroku.herokuapi.Credentials;
import com.jetbrains.heroku.herokuapi.HerokuApi;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author mh
 * @since 17.12.11
 */
public class HerokuApiTest {


    private static HerokuApi herokuApi;

    @BeforeClass
    public static void init() throws Exception {
        final Credentials credentials = HerokuApi.login("heroku-test@mesirii.de", "herokutest");
        herokuApi = new HerokuApi(credentials);
    }


    @Test
    public void testGetApplication() throws Exception {
        final Application application = herokuApi.getApplication("idea-test");
        final Map<String,Object> result = application.getInfo();
        assertEquals("http://idea-test.heroku.com/",result.get("web_url"));
    }
    @Test
    public void testGetApplicationStatus() throws Exception {
        final String status = herokuApi.getApplicationStatus("idea-test");
        System.out.println("status = " + status);
        assertNotNull(status);
    }
    @Test
    public void testGetApplicationCollaborators() throws Exception {
        final List<Map<String, Object>> result = herokuApi.loadApplicationCollaborators("idea-test");
        assertEquals(2, result.size());
        assertNotNull(result.get(0).get("email").equals("heroku-test@mesirii.de") || result.get(1).get("email").equals("heroku-test@mesirii.de"));
    }
    @Test
    public void testGetApplicationLogs() throws Exception {
        final String result = herokuApi.loadApplicationLogs("idea-test", 10000);
        assertNotNull("found logs",result);
    }
    @Test
    public void testGetApplications() throws Exception {
        final List<Application> apps = herokuApi.allApps();
        assertEquals(1,apps.size());
        assertEquals("http://idea-test.heroku.com/",apps.get(0).get("web_url"));
    }

    @Test
    public void testCreateApplication() {
        final String prefix = "test-create";
        createApplication(prefix);
    }

    private Application createApplication(String prefix) {
        final String newName = prefix + System.currentTimeMillis();
        final Application application = herokuApi.create(newName, HerokuApi.NO_OPTIONS);
        assertNotNull("created application",application);
        assertEquals("correct application name",newName,application.getName());
        return application;
    }

    @Test
    public void testDestroyApplication() throws InterruptedException {
        final Application application = createApplication("test-destroy");
        herokuApi.destroyApplication(application.getName());
        assertNull(herokuApi.getApplicationStatus(application.getName()));
    }
}

