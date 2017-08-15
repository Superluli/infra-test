package com.superluli.infra.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

public class MyHttpClient extends RestTemplate {

    private PoolingHttpClientConnectionManager connectionManager;

    private Logger logger;
    
    public MyHttpClient() {
    	super();
    }

    public void init(HttpClientConfiguration config, Logger delegateLogger)
            throws HttpClientInitializationFailureException {

        try {
            connectionManager = new PoolingHttpClientConnectionManager(configRegistry(config));
        } catch (Exception e) {
            throw new HttpClientInitializationFailureException(e.getMessage(), e);
        }

        // configure connection pooling

        connectionManager.setMaxTotal(config.getConnectionPoolMaxSize());
        connectionManager.setDefaultMaxPerRoute(config.getConnectionPoolMaxSize());

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(HttpClients.custom()
                        .disableConnectionState().setConnectionManager(connectionManager).build());

        // configure timeouts
        requestFactory.setConnectTimeout(config.getConnectionTimeout());
        requestFactory.setConnectionRequestTimeout(config.getConnectionRequestTimeout());
        requestFactory.setReadTimeout(config.getSocketReadTimeout());

        setRequestFactory(requestFactory);

        // Ignore HTTP errors, handle them in business logic codes

        setErrorHandler(new ResponseErrorHandler() {

            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {}
        });

        this.logger = delegateLogger;
    }

    private Registry<ConnectionSocketFactory> configRegistry(HttpClientConfiguration config)
            throws Exception {

        // configure mutual SSL
    	SSLContext sslcontext = null;
        
        if (config.isClientSSLAuth()) {

            InputStream keyStoreIn = null;
            InputStream trustStoreIn = null;
            SSLContextBuilder sslcontextBuilder = null;
            
            try {
                KeyStore keyStore = KeyStore.getInstance(config.getKeyStoreType());
                char[] keyStorePassword = config.getKeyStorePassword().toCharArray();
                keyStoreIn = new FileInputStream(config.getKeyStore());
                keyStore.load(keyStoreIn, keyStorePassword);
                
                sslcontextBuilder =
                      SSLContexts.custom().loadKeyMaterial(keyStore, keyStorePassword);
                
               if(config.getTrustStore() != null && !config.getTrustStore().isEmpty()){

                   KeyStore trustStore = KeyStore.getInstance(config.getTrustStoreType());
                     char[] trustStorePassword = config.getTrustStorePassword().toCharArray();
                     trustStoreIn = new FileInputStream(config.getTrustStore());
                     trustStore.load(trustStoreIn, trustStorePassword);
                     sslcontextBuilder.loadTrustMaterial(trustStore, null);
               }
                
               sslcontext = sslcontextBuilder.build(); 
                
            } finally {
                if (keyStoreIn != null) {
                    safeClose(keyStoreIn);
                }
                if (trustStoreIn != null) {
                    safeClose(trustStoreIn);
                }
            }
        }

        else {
            sslcontext = SSLContexts.custom().build();
        }

        /*
         * Supported protocols : TLSv1.2 Cipher Suites is not working : new String[]
         * {"TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256"} IS NOT SUPPORTED YET
         */
        SSLConnectionSocketFactory sslsf =
                new SSLConnectionSocketFactory(sslcontext, new String[] {"TLSv1.2"}, null,
                        new DefaultHostnameVerifier());

        PlainConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();

        return RegistryBuilder.<ConnectionSocketFactory>create().register("https", sslsf)
                .register("http", plainsf).build();
    }

    private void safeClose(InputStream fis) {
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity, Class<T> responseType) {

        return super.exchange(requestEntity.getUrl(), requestEntity.getMethod(), requestEntity,
                responseType);
    }
}
