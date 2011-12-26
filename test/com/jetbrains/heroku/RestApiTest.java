package com.jetbrains.heroku;

import com.jetbrains.heroku.rest.RestApi;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertTrue;

/**
 * @author mh
 * @since 17.12.11
 */
public class RestApiTest {
    @Test
    public void testPost() throws Exception {
        final RestApi api = new RestApi("api.heroku.com");
        final String data = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", "heroku-test@mesirii.de", "herokutest");
        final String result = api.post("/login", Collections.<String, String>emptyMap(), data);
        System.out.println("result = " + result);
        assertTrue(result.contains("verified_at"));
    }
}
