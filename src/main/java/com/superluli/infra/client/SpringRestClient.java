package com.superluli.infra.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.xml.transform.Source;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.superluli.infra.commons.JsonUtil;


@Service
public class SpringRestClient {

    @Autowired
    private LogMessageService logMessageService;

    private Logger logger = LoggerFactory.getLogger(SpringRestClient.class);
    @Value("${restClientTimeout:30000}")
    private int REQUEST_TIMEOUT;
    // private static final int PAYLOAD_LOGGING_THRESHOLD = Integer.parseInt(PropertiesUtil
    // .getProperty("restClientRequestPayloadLogging", "30000"));
    @Value("${restClientMaxConnection:500}")
    private int MAX_CONNNECTION;

    // @Autowired
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private LogSpringRestClientManager logSpringRestClientManager;

    public SpringRestClient() {}
    
    public SpringRestClient(Logger logger) {
        this.logger = logger;
    }

    @PostConstruct
    public void init() throws Exception {

        // // set request timeout
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        // Set max pool to 100
        cm.setMaxTotal(MAX_CONNNECTION);
        cm.setDefaultMaxPerRoute(MAX_CONNNECTION);

        HttpClient client = HttpClientBuilder.create().setConnectionManager(cm).build();

        HttpComponentsClientHttpRequestFactory reqFactory =
                new HttpComponentsClientHttpRequestFactory();

        // SimpleClientHttpRequestFactory reqFactory = new
        // SimpleClientHttpRequestFactory();
        reqFactory.setHttpClient(client);
        reqFactory.setReadTimeout(REQUEST_TIMEOUT);
        reqFactory.setConnectTimeout(REQUEST_TIMEOUT);
        this.restTemplate.setRequestFactory(reqFactory);

        // Create a list for the message converters
        MappingJackson2HttpMessageConverter jsonConvertor =
                new MappingJackson2HttpMessageConverter();
        jsonConvertor.setObjectMapper(JsonUtil.getMapper());
        List<HttpMessageConverter<?>> convertersList = new ArrayList<HttpMessageConverter<?>>();

        ByteArrayHttpMessageConverter byteArrayHttpMessageConverter =
                new ByteArrayHttpMessageConverter();
        convertersList.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        convertersList.add(jsonConvertor);
        convertersList.add(byteArrayHttpMessageConverter);
        convertersList.add(new ResourceHttpMessageConverter());
        convertersList.add(new SourceHttpMessageConverter<Source>());
        convertersList.add(new AllEncompassingFormHttpMessageConverter());
        restTemplate.setMessageConverters(convertersList);
        restTemplate.setErrorHandler(new CustomErrorHandler());
    }


    public LogSpringRestClientManager getLogSpringRestClientManager() {
        return logSpringRestClientManager;
    }
    
    protected RestTemplate getRestTemplate() {
        return restTemplate;
    }



    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        return logger;
    }



  
    public <T> T getByResourceId(String url, HttpHeaders headers, String id, Class<T> clazz) {

        Map<String, String> vars = new HashMap<String, String>();
        vars.put("id", id);
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);

        ResponseEntity<T> exchange =
                (ResponseEntity<T>) exchange(url + "/" + id, HttpMethod.GET, entity, clazz, vars);

        return exchange.getBody();

    }


    public <T extends Object> T insert(String url, HttpHeaders headers, T object) {
        return insert(url, new HashMap<String, String>(), headers, object);
    }

    @SuppressWarnings("unchecked")
    public <T extends Object> T insert(String url, Map<String, String> queryParams,
            HttpHeaders headers, T object) {

        url = buildUrlWithQueryParams(url, queryParams);
        HttpEntity<T> entity = new HttpEntity<T>(object, headers);
        Map<String, String> vars = new HashMap<String, String>();
        ResponseEntity<T> exchange =
                (ResponseEntity<T>) exchange(url, HttpMethod.POST, entity, object.getClass(), vars);

        return exchange.getBody();

    }



    // @SuppressWarnings("unchecked")
    public <T extends Object> T insert(String url, Map<String, String> queryParams,
            HttpHeaders headers, T object, Class<T> clazz) {

        url = buildUrlWithQueryParams(url, queryParams);
        HttpEntity<T> entity = new HttpEntity<T>(object, headers);
        Map<String, String> vars = new HashMap<String, String>();
        ResponseEntity<T> exchange =
                (ResponseEntity<T>) exchange(url, HttpMethod.POST, entity, clazz, vars);

        return exchange.getBody();

    }


    public <T> List<T> insertBatch(String url, HttpHeaders headers, List<T> objectList) {
        return insert(url, headers, objectList);
    }

    public <T> T insertBatch(String url, HttpHeaders headers, List<?> objectList, Class<T> clazz) {

        url = buildUrlWithQueryParams(url, new HashMap<String, String>());
        HttpEntity<List<?>> entity = new HttpEntity<List<?>>(objectList, headers);
        Map<String, String> vars = new HashMap<String, String>();
        ResponseEntity<T> exchange =
                (ResponseEntity<T>) exchange(url, HttpMethod.POST, entity, clazz, vars);

        return exchange.getBody();

    }

    public <T extends Object> T update(String url, String id, HttpHeaders headers, T o) {
        return update(url, id, new HashMap<String, String>(), headers, o);
    }

    public <T extends Object> T update(String url, String id, Map<String, String> queryParams,
            HttpHeaders headers, T o) {
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("id", id);
        // url += "/{id}";
        HttpEntity<T> entity = new HttpEntity<T>(o, headers);
        url = buildUrlWithQueryParams(url, queryParams);
        @SuppressWarnings("unchecked")
        ResponseEntity<T> exchange =
                (ResponseEntity<T>) exchange(url + "/" + id, HttpMethod.PUT, entity, o.getClass(),
                        vars);

        return exchange.getBody();
    }

    public <T> List<T> updateBatch(String url, HttpHeaders headers, List<T> objectList) {
        throw new UnsupportedOperationException("not supported");
    }

    public void remove(String url, HttpHeaders headers, String id) {
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("id", id);
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        exchange(url + "/" + id, HttpMethod.DELETE, entity, String.class, vars);
    }

    public <T> void removeBatch(String url, HttpHeaders headers, List<T> resouceList, Class<T> clazz) {

        Map<String, String> vars = new HashMap<String, String>();
        HttpEntity<List<T>> entityList = new HttpEntity<List<T>>(resouceList, headers);
        exchange(url, HttpMethod.DELETE, entityList, String.class, vars);
    }


    public <T> ResponseEntity<T> exchange(String url, HttpMethod method,
            HttpEntity<?> requestEntity, Class<T> responseType) {
        return exchange(url, method, requestEntity, responseType, new HashMap<String, String>());
    }
    
    
    
    
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method,
            HttpEntity<?> requestEntity, Class<T> responseType, Map<String, ?> uriVariables) {
    	return exchange(url, method, requestEntity, responseType, uriVariables, new HashMap<String,Object>());
    }
    

    public <T> ResponseEntity<T> exchange(String url, HttpMethod method,
            HttpEntity<?> requestEntity, Class<T> responseType, Map<String, ?> uriVariables,  Map<String,Object> logExtras) {

        String logId = "";
        ResponseEntity<T> successResponseEntity = null;
        ResponseEntity<String> failResponseEntity = null;
        LoggingMessage logRequest = null;
        Date reqDate = null;
        reqDate = new Date();
        long elapsed = 0;
        logId = HeaderUtils.generateUUID();
        logRequest = getLogRequest(logId, url, method, requestEntity);

        
        try {

            successResponseEntity =
                    restTemplate.exchange(url, method, requestEntity, responseType, uriVariables);
           
            Date respDate = new Date();
            elapsed = respDate.getTime() - reqDate.getTime();
        

        } catch (HttpStatusCodeException e) {
            // also log errors
            failResponseEntity =
                    new ResponseEntity<String>(e.getResponseBodyAsString(), e.getResponseHeaders(),
                            e.getStatusCode());
            if (shouldLog(url)) {
                Date respDate = new Date();
                elapsed = respDate.getTime() - reqDate.getTime();
                LoggingMessage logResponse = getLogResponse(logId, url, method, failResponseEntity);
                logRequestAndResponse(elapsed, logRequest, logResponse, uriVariables,logExtras);
            }
            throw e;
        } catch (Exception e) {
            // also log errors
            failResponseEntity = new ResponseEntity<String>(e.getMessage(), null);
            if (shouldLog(url)) {
                Date respDate = new Date();
                elapsed = respDate.getTime() - reqDate.getTime();
                LoggingMessage logResponse = getLogResponse(logId, url, method, failResponseEntity);
                logRequestAndResponse(elapsed, logRequest, logResponse, uriVariables,logExtras);
            }
            throw e;
        }

        if (shouldLog(url)) {
            LoggingMessage logResponse = getLogResponse(logId, url, method, successResponseEntity);
            logRequestAndResponse(elapsed, logRequest, logResponse, uriVariables,logExtras);
        }
        return successResponseEntity;
    }
    
    
    
    public <T> ResponseEntity<T> exchange(URI uri, HttpMethod method, HttpEntity<?> requestEntity,
            Class<T> responseType) {
    	return exchange(uri, method, requestEntity, responseType, new HashMap<String,String>(), new HashMap<String,Object>());
    }
    
    public <T> ResponseEntity<T> exchange(URI uri, HttpMethod method, HttpEntity<?> requestEntity,
            Class<T> responseType, Map<String, String> uriVariables) {
    	return exchange(uri, method, requestEntity, responseType, uriVariables, new HashMap<String,Object>());
    }




    public <T> ResponseEntity<T> exchange(URI uri, HttpMethod method, HttpEntity<?> requestEntity,
            Class<T> responseType, Map<String, String> uriVariables,  Map<String,Object> logExtras) {

        String logId = "";
        ResponseEntity<T> successResponseEntity = null;
        ResponseEntity<String> failResponseEntity = null;
        LoggingMessage logRequest = null;
        Date reqDate = new Date();
        long elapsed = 0;
        String url = null;
        try {
            url = URLDecoder.decode(uri.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
       
        logId = HeaderUtils.generateUUID();
        logRequest = getLogRequest(logId, url, method, requestEntity);

        
        try {

            successResponseEntity = restTemplate.exchange(uri, method, requestEntity, responseType);
            
            Date respDate = new Date();
            elapsed = respDate.getTime() - reqDate.getTime();
        

        } catch (HttpStatusCodeException e) {
            // also log errors
            failResponseEntity =
                    new ResponseEntity<String>(e.getResponseBodyAsString(), e.getResponseHeaders(),
                            e.getStatusCode());
            if (shouldLog(url)) {
                Date respDate = new Date();
                elapsed = respDate.getTime() - reqDate.getTime();
                LoggingMessage logResponse = getLogResponse(logId, url, method, failResponseEntity);
                logRequestAndResponse(elapsed, logRequest, logResponse, uriVariables,logExtras);
            }
            throw e;
        } catch (Exception e) {
            // also log errors
            failResponseEntity = new ResponseEntity<String>(e.getMessage(), null);
            if (shouldLog(url)) {
                Date respDate = new Date();
                elapsed = respDate.getTime() - reqDate.getTime();
                LoggingMessage logResponse = getLogResponse(logId, url, method, failResponseEntity);
                logRequestAndResponse(elapsed, logRequest, logResponse, uriVariables,logExtras);
            }
            throw e;
        }

        if (shouldLog(url)) {
            LoggingMessage logResponse = getLogResponse(logId, url, method, successResponseEntity);
            logRequestAndResponse(elapsed, logRequest, logResponse, uriVariables,logExtras);
        }
        return successResponseEntity;
    }

    private boolean shouldLog(String url) {
        return logger.isInfoEnabled();
    }


    protected void logRequestAndResponse(long elapsed, LoggingMessage request,
            LoggingMessage response, Map<String, ?> uriVariables, Map<String,Object> logExtras) {
        String logId = HeaderUtils.generateUUID();
        Map<String, Object> responseMap = response.toMap();
        Map<String, Object> requestMap = request.toMap();
        Object responseMessageObject =
                responseMap.get("message") == null ? new Object() : responseMap.get("message");

       
            Map<String, Object> logRequestResponseMap = new LinkedHashMap<String, Object>();

            Integer responseStatus = -1;
            if (responseMap.containsKey("status")) {
                responseStatus = Integer.valueOf(responseMap.get("status").toString());
                responseMap.put("status", responseStatus);
            }
            logRequestResponseMap.put(LogMessageService.TR_STR,
                    logMessageService.getTokenRequestId());
            logRequestResponseMap.put("elapsedTime", elapsed);
            logRequestResponseMap.put("apiType",
                    getLopApiType(requestMap, responseMap, uriVariables));


            logRequestResponseMap.put("logId", logId);
            logRequestResponseMap.put("response", responseMap);
            logRequestResponseMap.put("request", requestMap);
            
            if(logExtras != null && !logExtras.isEmpty()) {
            	logRequestResponseMap.put("extras", logExtras);
            }

            if (logSpringRestClientManager.hasLogSpringRestClientConfig()) {
                Integer status = -1;
                		
                if (responseMap.containsKey("status")) {
                    status = Integer.valueOf(responseMap.get("status").toString());
                    
                }
                        

                String name = StringUtil.getLastIndexString(logger.getName(), ".");

                if (status >= 300) {
                    Map<String, Object> responseMessageMap = new HashMap<String, Object>();

                    String errorMessage = getErrorMessage(name, responseMessageObject);
                    responseMessageMap.put("message", errorMessage);
                    responseMap.put("message", responseMessageMap);

                }

                
                if(logger.isDebugEnabled()) {
                	String serialzeJson =
                            logSpringRestClientManager.serializerJSONDebug(
                                    logRequestResponseMap);
                	if (serialzeJson != null) {
                        logger.debug(serialzeJson);
                    }
                } else {
                	String serialzeJson =
                            logSpringRestClientManager.serialzeJson(logger.getName(),
                                    logRequestResponseMap);
                	if (serialzeJson != null) {
                        logger.info(serialzeJson);
                    }
                }
                

            } 



        

    }


    protected String getLopApiType(Map<String, Object> requestMap, Map<String, Object> responseMap,
            Map<String, ?> uriVariable) {


        String requestUrl = requestMap.get("url") == null ? "" : requestMap.get("url").toString();
        String method =
                requestMap.get("method") == null ? "" : requestMap.get("method").toString()
                        .toUpperCase();
        
        Integer status = -1;
		
        if (responseMap.containsKey("status")) {
            status = Integer.valueOf(responseMap.get("status").toString());
            
        }


        String statusString = "";

        if (status == 200) {
            statusString = "OK";
            if ("GET".equals(method.toUpperCase(Locale.ENGLISH))) {
                statusString = "FOUND";
            }
        } else if (status == 201) {
            statusString = "CREATED";
        } else if (status == 202) {
            statusString = "ACCEPTED";
        } else if(status >= 200 && status < 300) {
			statusString = "SUCCESS_" + status;
		} else if(status >= 300 && status < 400) {
			statusString =  "REDIRECT_" + status;
		}  else {

            statusString = "ERROR_" + status;
        }



        String[] requestURLArray = requestUrl.split("\\?");
        String requestURLPath = requestUrl;
        if (requestURLArray.length >= 1) {
            requestURLPath = requestURLArray[0];
        }
        String lastRequestURL = StringUtil.getLastIndexString(requestURLPath, "/").toUpperCase();
        if (uriVariable.containsKey("id")) {
            lastRequestURL =
            		StringUtil.getSplitString(requestURLPath, "/",
                            requestURLPath.split("/").length - 2).toUpperCase()
                            + "_BY_ID";
        } else {
            lastRequestURL = StringUtil.getLastIndexString(requestURLPath, "/").toUpperCase();
        }
        String name = StringUtil.getLastIndexString(logger.getName(), ".");
        String alias = logSpringRestClientManager.getAlias(name);
        StringBuilder toReturnBuilder = new StringBuilder();
        toReturnBuilder.append(alias).append("_").append(method).append("_").append(lastRequestURL)
                .append("_").append(statusString);

        return StringUtil.strip(toReturnBuilder.toString(), 1024);


    }


    protected String getErrorMessage(String name, Object responseMessageBody) {
        String alias = logSpringRestClientManager.getAlias(name);
        StringBuilder toReturnBuilder =
                new StringBuilder().append("[").append(alias).append("]").append("[ERROR]");
        if (responseMessageBody == null) {
            return toReturnBuilder.toString();
        }

        if (responseMessageBody instanceof Map) {
            String jsonString = "";
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> responeMessageBodyMap =
                        (Map<String, Object>) responseMessageBody;
                if (responeMessageBodyMap.containsKey("message")) {
                    jsonString = responeMessageBodyMap.get("message").toString();
                } else {
						jsonString = JsonUtil.toJson(responseMessageBody);
                }

                toReturnBuilder.append(jsonString);
            } catch (IOException e) {
                jsonString = responseMessageBody.toString();
                toReturnBuilder.append(jsonString);
            }
        } else {
            toReturnBuilder.append(" ").append(responseMessageBody.toString());
        }

        return toReturnBuilder.toString();
    }

    private LoggingMessage getLogRequest(String id, String url, HttpMethod method,
            HttpEntity<?> requestEntity) {

        final LoggingMessage message = new LoggingMessage(id, url);
        message.getHttpMethod().append(method);
        if (requestEntity != null) {
            message.getContentType().append(requestEntity.getHeaders().get("Content-Type"));
            setLogHeader(requestEntity.getHeaders(), message.getHeaders());
            if(requestEntity.getBody() != null && requestEntity.getBody() instanceof String) {
            	
            	if(requestEntity.getHeaders() != null && requestEntity.getHeaders().getContentType() != null && "application/json".equals(requestEntity.getHeaders().getContentType().toString())) {
            		try {
						Map<String,Object> fromJson = JsonUtil.fromJson((String)requestEntity.getBody(), Map.class);
            			message.setPayload(fromJson);
            		} catch (Exception e){
            			Map<String,String> messageMap = new HashMap<String,String>();
            			messageMap.put("payload", StringUtil.strip((String)requestEntity.getBody(), 1024));
            			message.setPayload(messageMap);
            		}
            		
            	} else {
            		Map<String,String> messageMap = new HashMap<String,String>();
        			messageMap.put("payload", StringUtil.strip((String)requestEntity.getBody(), 1024));
        			message.setPayload(messageMap);
            	}
            }  else if(requestEntity.getBody() != null && requestEntity.getBody() instanceof byte[]) {
            	Map<String,byte[]> messageMap = new HashMap<String,byte[]>();
    			messageMap.put("bytes", (byte[])requestEntity.getBody());
    			message.setPayload(requestEntity.getBody());
            } else if(requestEntity.getBody() != null && requestEntity.getBody() instanceof List) {
            	Map<String,List> messageMap = new HashMap<String,List>();
    			messageMap.put("list", (List)requestEntity.getBody());
    			message.setPayload(requestEntity.getBody());
            } else {
            	 message.setPayload(requestEntity.getBody());
            }
        }
        return message;
    }

    private LoggingMessage getLogResponse(String id, String url, HttpMethod method,
            ResponseEntity<?> responseEntity) {

        final LoggingMessage message = new LoggingMessage(id, url);

        if (responseEntity != null) {
            if (responseEntity.getStatusCode() != null) {
                message.getResponseCode().append(responseEntity.getStatusCode());
            }
            message.getContentType().append(responseEntity.getHeaders().get("Content-Type"));
            setLogHeader(responseEntity.getHeaders(), message.getHeaders());
            
            
            if(responseEntity.getBody() != null && responseEntity.getBody() instanceof String) {
            	
            	if(responseEntity.getHeaders() != null && responseEntity.getHeaders().getContentType() != null && "application/json".equals(responseEntity.getHeaders().getContentType().toString())) {
            		try {
            			@SuppressWarnings("unchecked")
						Map<String,Object> fromJson = JsonUtil.fromJson((String)responseEntity.getBody(), Map.class);
            			message.setMessage(fromJson);
            		} catch (Exception e){
            			Map<String,String> messageMap = new HashMap<String,String>();
            			messageMap.put("message", StringUtil.strip((String)responseEntity.getBody(), 1024));
            			message.setMessage(messageMap);
            		}
            		
            	} else {
            		Map<String,String> messageMap = new HashMap<String,String>();
        			messageMap.put("message", StringUtil.strip((String)responseEntity.getBody(), 1024));
        			message.setMessage(messageMap);
            	}
            } else if(responseEntity.getBody() != null && responseEntity.getBody() instanceof byte[]) {
            	Map<String,byte[]> messageMap = new HashMap<String,byte[]>();
    			messageMap.put("bytes", (byte[])responseEntity.getBody());
    			message.setMessage(responseEntity.getBody());
            } else if(responseEntity.getBody() != null && responseEntity.getBody() instanceof List) {
            	Map<String,List> messageMap = new HashMap<String,List>();
    			messageMap.put("list", (List)responseEntity.getBody());
    			message.setMessage(responseEntity.getBody());
            } else {
            	 message.setMessage(responseEntity.getBody());
            }
           

        }
        return message;
    }


    private void setLogHeader(HttpHeaders headers, Map<String, String> headerMap) {

        for (String headerName : headers.keySet()) {


            if (headerName.contains("Authorization") || headerName.contains("Key")) {
                headerMap.put(headerName.toLowerCase(), "*****");
            } else {

                List<String> list = headers.get(headerName);
                int j = 0;
                StringBuilder headerBuilder = new StringBuilder();
                for (String headerItem : list) {
                    if (j != 0) {
                        headerBuilder.append(",");
                    }
                    headerBuilder.append(headerItem);
                    j++;
                }
                headerMap.put(headerName.toLowerCase(), headerBuilder.toString());

            }
            
            
    		
            
        }
        
        String requestId = headerMap.get("request-id");
        if(requestId != null) {
        	requestId = logSpringRestClientManager.getLogMaskManager().maskReqId(requestId);
        	if(requestId != null) {
        		headerMap.put("request-id", requestId);
                
        	}
    		
        }

    }
    

    private String buildUrlWithQueryParams(String url, Map<String, String> queryParams) {

        StringBuilder builder = new StringBuilder(url);
        if (queryParams != null) {
            if (!queryParams.keySet().isEmpty()) {
                builder.append("?");
            }
            int i = 0;
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                //String value = queryParams.get(key);
                if (i != 0) {
                    builder.append("&");
                }
                builder.append(entry.getKey()).append("=").append(entry.getValue());

                i++;
            }
        }
        return builder.toString();

    }

    static final class LoggingMessage {

        private static final AtomicInteger ID = new AtomicInteger();

        private final StringBuilder address;
        private final StringBuilder contentType;
        private final StringBuilder encoding;
        private final StringBuilder httpMethod;
        private final Map<String, String> headers;
        private Object message;
        private Object payload;
        private final StringBuilder responseCode;

        private final String url;

        public LoggingMessage(String i, String u) {


            url = u;
            contentType = new StringBuilder();
            address = new StringBuilder();
            encoding = new StringBuilder();
            httpMethod = new StringBuilder();
            headers = new LinkedHashMap<String, String>();
            


            responseCode = new StringBuilder();
        }

        public static String nextId() {
            return HeaderUtils.generateUUID();
        }

        public static String id() {
            return ID.toString();
        }

        public StringBuilder getAddress() {
            return address;
        }

        public StringBuilder getEncoding() {
            return encoding;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public StringBuilder getHttpMethod() {
            return httpMethod;
        }

        public StringBuilder getContentType() {
            return contentType;
        }

        public void setMessage(Object message) {
            if (message == null) {
                return;
            }
            this.message = message;
        }

        public void setMessage(String message) {
            if (message == null) {
                return;
            }
            Map<String, String> messageMap = new HashMap<String, String>();
            messageMap.put("message", message);
            this.message = messageMap;
        }

        public void setMessage(Integer message) {
            if (message == null) {
                return;
            }
            Map<String, Integer> messageMap = new HashMap<String, Integer>();
            messageMap.put("messageInt", message);
            this.message = messageMap;
        }

        public void setMessage(Double message) {
            if (message == null) {
                return;
            }
            Map<String, Double> messageMap = new HashMap<String, Double>();
            messageMap.put("messageDouble", message);
            this.message = messageMap;
        }



        public void setPayload(Object payload) {
            this.payload = payload;
        }

        public StringBuilder getResponseCode() {
            return responseCode;
        }

        // public String toString() {
        // StringBuilder buffer = new StringBuilder();
        // buffer.append(heading);
        // buffer.append("\nID: ").append(id);
        // if (url.length() > 0) {
        // buffer.append("\nUrl: ");
        // buffer.append(url);
        // }
        // if (address.length() > 0) {
        // buffer.append("\nAddress: ");
        // buffer.append(address);
        // }
        // if (responseCode.length() > 0) {
        // buffer.append("\nResponse-Code: ");
        // buffer.append(responseCode);
        // }
        // if (encoding.length() > 0) {
        // buffer.append("\nEncoding: ");
        // buffer.append(encoding);
        // }
        // if (httpMethod.length() > 0) {
        // buffer.append("\nHttp-Method: ");
        // buffer.append(httpMethod);
        // }
        // buffer.append("\nContent-Type: ");
        // buffer.append(contentType);
        // buffer.append("\nHeaders: ");
        // buffer.append(header);
        // if (message != null ) {
        // buffer.append("\nMessages: ");
        // try {
        // buffer.append(JsonUtil.toJson(message));
        // }
        // catch(Exception e) {
        // buffer.append(message.toString());
        // }
        //
        // }
        // if (payload != null) {
        // buffer.append("\nPayload: ");
        // try {
        // buffer.append(JsonUtil.toJson(payload));
        // }
        // catch(Exception e) {
        // buffer.append(payload.toString());
        // }
        //
        // }
        // buffer.append("\n--------------------------------------");
        // return buffer.toString();
        // }



        private Map<String, Object> toMap() {
            Map<String, Object> request = new LinkedHashMap<String, Object>();


            if (url.length() > 0) {
                request.put("url", url);
            }
            if (address.length() > 0) {
                request.put("address", address);
            }


            if (responseCode.length() > 0) {
                request.put("status", responseCode);
            }
            if (encoding.length() > 0) {
                request.put("status", encoding);
            }
            if (httpMethod.length() > 0) {
                request.put("method", httpMethod);
            }

            if (headers != null) {
                request.put("headers", headers);
            }


            if (message != null) {
                request.put("message", message);
            }

            if (payload != null) {
                request.put("payload", payload);
            }
            return request;


        }
    }
}
