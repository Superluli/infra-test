package com.superluli.infra.client;

public class HttpClientInitializationFailureException extends Exception {

    private static final long serialVersionUID = 8265700992994902258L;

    private static final String PREFIX = "HttpClient Initialization failed : ";

    public HttpClientInitializationFailureException(String message, Throwable cause) {
        super(PREFIX + message, cause);
    }
}
