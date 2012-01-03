package com.jetbrains.heroku.herokuapi;

import com.heroku.api.App;
import com.heroku.api.Collaborator;
import com.heroku.api.HerokuAPI;
import com.heroku.api.connection.Connection;
import com.heroku.api.exception.RequestFailedException;
import com.heroku.api.request.log.LogStreamResponse;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author mh
 * @since 17.12.11
 */
public class HerokuApiTest {


    private static HerokuAPI herokuApi;
    private static final String API_KEY = "8e8d207358c24f68314ccfb7b47e93f6f298f7c8";
    private static final String INVALID_API_KEY = API_KEY.substring(1);
    private static final String IDEA_TEST = "idea-test";

    @BeforeClass
    public static void init() throws Exception {
        HerokuApiTest.herokuApi = new HerokuAPI(API_KEY);
    }

    @Test(expected = RequestFailedException.class)
    public void testInvalidApiToken() {
        new HerokuAPI(INVALID_API_KEY).listApps();
    }
    @Test(expected = RequestFailedException.class)
    public void testInvalidApiListKeysToken() {
        new HerokuAPI(INVALID_API_KEY).listKeys();
    }

    @Test(expected = RequestFailedException.class)
    public void testEmtpyApiKey() {
        new HerokuAPI("").listApps();
    }
    @Test(expected = RequestFailedException.class)
    public void testSpaceOnlyApiKey() {
        new HerokuAPI(" ").listApps();
    }
    @Test(expected = RequestFailedException.class)
    public void testNullApiKey() {
        new HerokuAPI((String)null).listApps();
    }

    @Test
    public void testGetApplication() throws Exception {
        final App application = herokuApi.getApp(IDEA_TEST);
        assertEquals("http://idea-test.heroku.com/",application.getWebUrl());
    }

    @Test
    @Ignore
    public void testGetApplicationStatus() throws Exception {
        final String status = herokuApi.getApp(IDEA_TEST).getCreateStatus();
        System.out.println("status = " + status);
        assertNotNull(status);
    }
    @Test
    public void testGetApplicationCollaborators() throws Exception {
        final List<Collaborator> result = herokuApi.listCollaborators(IDEA_TEST);
        assertEquals(1, result.size());
        assertEquals("heroku-test@mesirii.de",result.get(0).getEmail());
    }
    @Test
    public void testGetApplicationLogs() throws Exception {
        final LogStreamResponse result = herokuApi.getLogs(IDEA_TEST);
        assertNotNull("found logs", result);
    }
    @Test
    public void testGetApplications() throws Exception {
        final List<App> apps = herokuApi.listApps();
        assertTrue(apps.size()>0);
        assertEquals("http://idea-test.heroku.com/",apps.get(0).getWebUrl());
    }

    @Test
    public void testCreateApplication() {
        final String prefix = "test-create";
        final App application = createApplication(prefix);
        herokuApi.destroyApp(application.getName());
    }

    private App createApplication(String prefix) {
        final String newName = prefix + System.currentTimeMillis();
        final App application = herokuApi.createApp(new App().named(newName));
        assertNotNull("created application",application);
        assertEquals("correct application name",newName,application.getName());
        return application;
    }

    @Test
    public void testDestroyApplication() throws InterruptedException {
        final App application = createApplication("test-destroy");
        herokuApi.destroyApp(application.getName());
        try {
            assertNull(herokuApi.getApp(application.getName()));
            fail("App still exists");
        } catch(com.heroku.api.exception.RequestFailedException rfe) { }
    }
}

