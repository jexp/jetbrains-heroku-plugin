package com.heroku.idea.rest;

/**
* @author mh
* @since 17.12.11
*/
public class RestException extends RuntimeException {
    private final int status;


    public RestException(String s, Throwable throwable) {
        super(s, throwable);
        this.status = 500;
    }

    public RestException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
