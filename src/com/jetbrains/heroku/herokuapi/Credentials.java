package com.jetbrains.heroku.herokuapi;

import com.jetbrains.heroku.rest.RestFormatHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mh
 * @since 17.12.11
 */
public class Credentials {
    private Map data = new HashMap();

    public Credentials() {
    }

    public Credentials(Map<String, Object> data) {
        this.data.putAll(data);
    }

    /*
   credentials.verifiedAt = map.get("verified_at");
   credentials.apiKey = map.get("api_key");
   credentials.idEmail = map.get("id");
   credentials.verified = map.get("verified");
    */
    public String user() {
        return (String) get("email");
    }

    public String token() {
        return (String) get("api_key");
    }

    public boolean valid() {
        return token() != null && user() != null; //data.containsKey("verified") && data.get("verfied").equals(true);
    }

    public Map<String, String> authParams() {
        return (Map) RestFormatHelper.map("user", user(), "password", token());
    }

    private Object get(String key) {
        return data.get(key);
    }
}
