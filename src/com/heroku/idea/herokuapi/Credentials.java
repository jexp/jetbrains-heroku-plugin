package com.heroku.idea.herokuapi;

import java.util.HashMap;
import java.util.Map;

import static com.heroku.idea.rest.RestFormatHelper.map;

/**
 * @author mh
 * @since 17.12.11
 */
public class Credentials {
    private Map<String, Object> data = new HashMap<String, Object>();

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
        return (Map) map("user", user(), "password", token());
    }

    private Object get(String key) {
        return data.get(key);
    }
}
