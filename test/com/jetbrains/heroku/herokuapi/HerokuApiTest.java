package com.jetbrains.heroku.herokuapi;

import com.google.gson.Gson;
import com.heroku.api.App;
import com.heroku.api.Collaborator;
import com.heroku.api.HerokuAPI;
import com.heroku.api.connection.Connection;
import com.heroku.api.exception.RequestFailedException;
import com.heroku.api.parser.TypeReference;
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
        assertEquals("heroku-test@mesirii.de", result.get(0).getEmail());
    }
    @Test
    public void testGetApplicationLogs() throws Exception {
        final LogStreamResponse result = herokuApi.getLogs(IDEA_TEST);
        assertNotNull("found logs", result);
    }
    @Test
    public void testGetApplications() throws Exception {
        final List<App> apps = herokuApi.listApps();
        assertTrue(apps.size() > 0);
        int count=0;
        for (App app : apps) {
            if (app.getWebUrl().equals("http://idea-test.heroku.com/")) count++;
        }
        assertEquals(1,count);
    }
    @Test
    public void testParseComplexJsonResponse() {
        String invalidJson="[{\"name\":\"dropphotos\",\"stack\":\"bamboo-ree-1.8.7\",\"slug_size\":2383872,\"requested_stack\":null,\"created_at\":\"2010/06/01 07:48:24 -0700\",\"web_url\":\"http://dropphotos.heroku.com/\",\"owner_email\":\"shane@digitalsanctum.com\",\"create_status\":\"complete\",\"id\":200391,\"domain_name\":{\"created_at\":\"2010/06/03 19:25:08 -0700\",\"updated_at\":\"2010/06/03 19:25:08 -0700\",\"default\":null,\"domain\":\"dropphotos.com\",\"id\":27324,\"app_id\":200391,\"base_domain\":\"dropphotos.com\"},\"repo_size\":630784,\"git_url\":\"git@heroku.com:dropphotos.git\",\"repo_migrate_status\":\"complete\",\"dynos\":1,\"workers\":0},{\"name\":\"digitalsanctum\",\"stack\":\"bamboo-ree-1.8.7\",\"slug_size\":4755456,\"requested_stack\":null,\"created_at\":\"2010/09/11 13:46:15 -0700\",\"web_url\":\"http://digitalsanctum.heroku.com/\",\"owner_email\":\"shane@digitalsanctum.com\",\"create_status\":\"complete\",\"id\":287273,\"domain_name\":null,\"repo_size\":6066176,\"git_url\":\"git@heroku.com:digitalsanctum.git\",\"repo_migrate_status\":\"complete\",\"dynos\":1,\"workers\":0},{\"name\":\"voxplanner\",\"stack\":\"bamboo-ree-1.8.7\",\"slug_size\":4284416,\"requested_stack\":null,\"created_at\":\"2011/02/03 12:01:02 -0800\",\"web_url\":\"http://voxplanner.heroku.com/\",\"owner_email\":\"shane@digitalsanctum.com\",\"create_status\":\"complete\",\"id\":431577,\"domain_name\":null,\"repo_size\":7344128,\"git_url\":\"git@heroku.com:voxplanner.git\",\"repo_migrate_status\":\"complete\",\"dynos\":1,\"workers\":0},{\"name\":\"manualpt\",\"stack\":\"cedar\",\"slug_size\":64991232,\"requested_stack\":null,\"created_at\":\"2011/09/05 04:52:13 -0700\",\"web_url\":\"http://manualpt.herokuapp.com/\",\"owner_email\":\"shane@digitalsanctum.com\",\"create_status\":\"complete\",\"id\":927556,\"domain_name\":{\"created_at\":\"2011/09/10 06:01:20 -0700\",\"updated_at\":\"2011/09/10 06:01:20 -0700\",\"default\":null,\"domain\":\"manualpt.com\",\"id\":174602,\"app_id\":927556,\"base_domain\":\"manualpt.com\"},\"repo_size\":81727488,\"git_url\":\"git@heroku.com:manualpt.git\",\"repo_migrate_status\":\"complete\",\"dynos\":0,\"workers\":0},{\"name\":\"javamatters-staging\",\"stack\":\"cedar\",\"slug_size\":22073344,\"requested_stack\":null,\"created_at\":\"2011/12/31 13:54:07 -0800\",\"web_url\":\"http://javamatters-staging.herokuapp.com/\",\"owner_email\":\"shane@digitalsanctum.com\",\"create_status\":\"complete\",\"id\":2290626,\"domain_name\":null,\"repo_size\":64675840,\"git_url\":\"git@heroku.com:javamatters-staging.git\",\"repo_migrate_status\":\"complete\",\"dynos\":0,\"workers\":0}]";
        new Gson().fromJson(invalidJson,new TypeReference<List<App>>(){}.getType());
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

