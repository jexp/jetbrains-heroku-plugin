package com.jetbrains.heroku.rest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mh
 * @since 17.12.11
 */
public class RestApi {
    public static final int SSL_PORT = 443;
    public static final String HTTPS = "https://";
    HttpClient client;
    private static final int CONNECTION_TIMEOUT = 120000;
    private static final int SO_TIMEOUT = 20000;
    private final String baseUri;

    public RestApi(String baseUri, String username, String password) {
        this(baseUri);
        client.getState().setCredentials(
                new AuthScope(baseUri, SSL_PORT, "Application"),
                new UsernamePasswordCredentials(username, password)
        );
    }

    public RestApi(String baseUri) {
        this.baseUri = baseUri;
        client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout(CONNECTION_TIMEOUT);
        client.getHttpConnectionManager().getParams().setSoTimeout(SO_TIMEOUT);
    }

    public String post(String path, Map<String, String> options, String data) {
        final PostMethod method = new PostMethod(path(path));
        return execute(path, options, data, method);
    }

    private String path(String path) {
        if (path.startsWith("http")) return path;
        return HTTPS + baseUri + path;
    }

    public String get(String path, Map<String, String> options) {
        final GetMethod method = new GetMethod(path(path));
        return execute(path, options, null, method);
    }

    public String put(String path, Map<String, String> options, String data) {
        final PutMethod method = new PutMethod(path(path));
        return execute(path, options, data, method);
    }

    public String delete(String path, Map<String, String> options) {
        final DeleteMethod method = new DeleteMethod(path(path));
        return execute(path, options, null, method);
    }

    private String execute(String path, Map<String, String> options, String data, HttpMethodBase method) {
        addHeaders(options, method);
        addBody(data, method);
        try {
            final int status = client.executeMethod(method);
            System.err.println("headers = " + Arrays.deepToString(method.getRequestHeaders()));
            System.err.println("payload = " + data);
            final String result = method.getResponseBodyAsString();
            System.err.println("result = " + result);
            if (isSuccessStatus(status)) {
                return result;
            }
            throw new RestException("Error executing request to " + path + " with " + options + " and payload " + data + " status: " + status + " " + result,status);
        } catch (IOException e) {
            throw new RestException("Error executing request to " + path + " with " + options + " and payload " + data, e);
        }
    }

    private boolean isSuccessStatus(int status) {
        return status/100 == 2;
    }

    private void addBody(String data, HttpMethodBase method) {
        if (method instanceof EntityEnclosingMethod && data!=null) {
            ((EntityEnclosingMethod) method).setRequestBody(data);
        }
    }

    private void addHeaders(Map<String, String> options, HttpMethodBase method) {
        for (Map.Entry<String, String> entry : mergeDefaultHeaders(options).entrySet()) {
            method.addRequestHeader(entry.getKey(), entry.getValue());
        }
    }

    private Map<String, String> mergeDefaultHeaders(Map<String, String> options) {
        Map<String,String> requestOptions = new HashMap<String,String>();
        requestOptions.put("Accept", "application/json");
        requestOptions.put("Content-Type", "application/json");
        requestOptions.put("X-Heroku-API-Version", "2");
        requestOptions.put("User-Agent", "heroku-gem/2.3.3");
        for (Map.Entry<String, String> entry : options.entrySet()) {
            if (entry.getValue()==null) {
                requestOptions.remove(entry.getKey());
            } else {
                requestOptions.put(entry.getKey(), entry.getValue());
            }
        }
        return requestOptions;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public String stream(String url, Map<String, String> options, int length) {
        try {
            final GetMethod method = new GetMethod(url);
            final int status = client.executeMethod(method);
            System.out.println("status = " + status);
            return readStream(method.getResponseBodyAsStream());
        } catch (Exception e) {
            throw new RestException("Error executing get request to " + url + " with " + options + " and retrieving data length " + length, e);
        }
    }

    private String readStream(InputStream is) throws IOException {
        StringBuilder sb=new StringBuilder();
        int count;
        byte[] buffer=new byte[1024];
        while ((count = is.read(buffer))!=-1) {
            sb.append(new String(buffer,0,count,"UTF-8"));
            System.out.println("count = " + count);
        }
        is.close();
        return sb.toString();
    }
}
