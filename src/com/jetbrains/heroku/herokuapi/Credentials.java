package com.jetbrains.heroku.herokuapi;

/**
 * @author mh
 * @since 17.12.11
 */
public class Credentials {

    private String token;

    public Credentials() {
    }

    public Credentials(String token) {
        this.token = token;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean valid() {
        return getToken() != null && !getToken().isEmpty();
    }
}
