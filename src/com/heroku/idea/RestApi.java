package com.heroku.idea;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.*;

import java.io.IOException;
import java.util.Map;

/**
 * @author mh
 * @since 17.12.11
 */
public class RestApi {
    HttpClient client;
    private static final int CONNECTION_TIMEOUT = 20000;
    private static final int SO_TIMEOUT = 5000;
    private final String baseUri;


    public RestApi(String baseUri) {
        this.baseUri = baseUri;
        client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout(CONNECTION_TIMEOUT);
        client.getHttpConnectionManager().getParams().setSoTimeout(SO_TIMEOUT);
    }

    public String post(String path, Map<String, String> options, String data) {
        final PostMethod method = new PostMethod(baseUri+path);
        return execute(path, options, data, method);
    }
    public String get(String path, Map<String, String> options, String data) {
        final GetMethod method = new GetMethod(baseUri+path);
        return execute(path, options, data, method);
    }
    public String put(String path, Map<String, String> options, String data) {
        final PutMethod method = new PutMethod(baseUri+path);
        return execute(path, options, data, method);
    }
    public String delete(String path, Map<String, String> options, String data) {
        final DeleteMethod method = new DeleteMethod(baseUri+path);
        return execute(path, options, data, method);
    }

    private String execute(String path, Map<String, String> options, String data, HttpMethodBase method) {
        for (Map.Entry<String, String> entry : options.entrySet()) {
            method.addRequestHeader(entry.getKey(), entry.getValue());
        }
        method.addRequestHeader("Accept", "application/json");
        method.addRequestHeader("Content-Type","application/json");
        method.addRequestHeader("X-Heroku-API-Version","2");
        if (method instanceof EntityEnclosingMethod) {
            ((EntityEnclosingMethod)method).setRequestBody(data);
        }
        try {
            final int status = client.executeMethod(method);
            if (status==200) {
                return method.getResponseBodyAsString();
            }
            throw new RestException("Error executing request to "+path+" with "+options+" and payload "+data+" status: "+status+" "+method.getResponseBodyAsString());
        } catch (IOException e) {
            throw new RestException("Error executing request to "+path+" with "+options+" and payload "+data,e);
        }
    }

    static class RestException extends RuntimeException {
        public RestException(String s) {
            super(s);
        }

        public RestException(String s, Throwable throwable) {
            super(s, throwable);
        }
    }
}
