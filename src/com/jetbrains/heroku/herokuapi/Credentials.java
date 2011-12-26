package com.jetbrains.heroku.herokuapi;

/**
 * @author mh
 * @since 17.12.11
 */
public class Credentials {

    private String email;
    private String token;

    public Credentials() {
    }

    public Credentials(String email, String token) {
        this.email = email;
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean valid() {
        return getToken() != null && getEmail() != null; //data.containsKey("verified") && data.get("verfied").equals(true);
    }
}
