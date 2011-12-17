package com.heroku.idea;

import java.util.Collections;
import java.util.Map;

import static com.heroku.idea.Json.format;
import static com.heroku.idea.Json.map;

/**
 * @author mh
 * @since 17.12.11
 */
public class HerokuApi {
    public static final Map<String, String> NO_OPTIONS = Collections.emptyMap();
    private final String server = "https://api.heroku.com";
    private final RestApi restApi = new RestApi(server);

    class Credentials {
        private final Map<String, Object> data;

        public Credentials(Map<String, Object> data) {
            this.data = data;
        }
        /*
        credentials.verifiedAt = map.get("verified_at");
        credentials.apiKey = map.get("api_key");
        credentials.verified = map.get("verified");
         */
        public String getEmail() {
            return (String) data.get("email");
        }
        public String getToken() {
            return (String) data.get("id");
        }
    }

    public Credentials login(String email, String password) {
        final String result = restApi.post("/login", NO_OPTIONS, format(map("username", email, "password", password)));
        Map<String, Object> map = (Map<String, Object>) Json.parse(result);
        return new Credentials(map);
    }

    public void logout() {

    }

    public Application get(String name) {
        return new Application();
    }
    public Application create(String name, Map<String, String> params) {
        return null;
    }

    class Application {
        /*InputStream getLogs();

        void push();

        void ps(int dynos);

        void destroy();

        URL open(); // todo appurl for browser

        String getName();

        boolean rename(String newName);

        Map<String, Object> config(); // todo config object with add , remove
        */

        Map<String, Object> info() {
            return Collections.emptyMap();
        }


    }

}
