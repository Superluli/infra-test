package com.superluli.infra.client;


public class HttpClientConfiguration {

    protected int connectionPoolMaxSize;
    protected int connectionTimeout;
    protected int connectionRequestTimeout;
    protected int socketReadTimeout;

    protected String keyStore;
    protected String keyStoreType;
    protected String keyStorePassword;

    protected String trustStore;
    protected String trustStoreType;
    protected String trustStorePassword;

    protected boolean httpOnly;
    protected boolean clientSSLAuth;

    public int getConnectionPoolMaxSize() {
        return connectionPoolMaxSize;
    }

    public void setConnectionPoolMaxSize(int connectionPoolMaxSize) {
        this.connectionPoolMaxSize = connectionPoolMaxSize;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public int getSocketReadTimeout() {
        return socketReadTimeout;
    }

    public void setSocketReadTimeout(int socketReadTimeout) {
        this.socketReadTimeout = socketReadTimeout;
    }

    public String getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    public String getTrustStoreType() {
        return trustStoreType;
    }

    public void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    /**
     * @return the httpOnly
     */
    public boolean isHttpOnly() {
        return httpOnly;
    }

    /**
     * @param httpOnly the httpOnly to set
     */
    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    /**
     * @return the clientSSLAuth
     */
    public boolean isClientSSLAuth() {
        return clientSSLAuth;
    }

    /**
     * @param clientSSLAuth the clientSSLAuth to set
     */
    public void setClientSSLAuth(boolean clientSSLAuth) {
        this.clientSSLAuth = clientSSLAuth;
    }
}
