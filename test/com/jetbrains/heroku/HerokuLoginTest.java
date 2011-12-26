package com.jetbrains.heroku;

import com.jetbrains.heroku.herokuapi.Credentials;
import com.jetbrains.heroku.herokuapi.HerokuApi;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author mh
 * @since 17.12.11
 */
public class HerokuLoginTest {

    public static final String EMAIL = "heroku-test@mesirii.de";

    @Test
    public void testLogin() throws Exception {
        final Credentials credentials = HerokuApi.login(EMAIL, "herokutest");
        assertNotNull(credentials);
        assertEquals(EMAIL, credentials.user());
    }
}
