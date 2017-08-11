package com.superluli.infra.app;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.hateoas.HypermediaAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.guava.GuavaCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.superluli.infra.accesslogging.AccessLoggingFilter;
import com.superluli.infra.accesslogging.AccessLoggingService;
import com.superluli.infra.accesslogging.EventTypeProvider;
import com.superluli.infra.kafka.producer.KafkaConfiguration;
import com.superluli.infra.kafka.producer.KafkaProducerService;


@SpringBootApplication
@EnableAutoConfiguration(exclude = {HypermediaAutoConfiguration.class})
@ComponentScan(basePackages = {"com.superluli"})
@EnableAspectJAutoProxy
@EnableTransactionManagement
@EnableCaching
public class Application extends WebMvcConfigurerAdapter {

	public static Logger logger = LoggerFactory.getLogger(Application.class);
	
    @Value("${executorService.corePoolSize: 200}")
    private int executorCorePoolSize;

    @Value("${executorService.maxPoolSize: 5000}")
    private int executorMaxPoolSize;

    @Value("${executorService.queueCapacity: 1000}")
    private int executorQueueCapacity;
    
    @Value("${cacheService.shortTimeoutSecond: 30}")
    private int cacheShortTimeoutSecond;
    
    @Value("${cacheService.longTimeoutMinute: 3}")
    private int cacheLongTimeoutMinute;

    /**
     * This is temporary to allow api stubs
     */
    @Value("${promotion.thirdparty.prizelogic.clientType:default}")
    private String prizeLogicClientType;

    @Bean(name = "mainTaskExecutor")
    public ThreadPoolTaskExecutor mainTaskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadGroupName("mainTaskExecutor");
        executor.setThreadNamePrefix("mainTaskExecutor-");
        executor.setCorePoolSize(executorCorePoolSize);
        executor.setMaxPoolSize(executorMaxPoolSize);
        executor.setKeepAliveSeconds(60);
        executor.setQueueCapacity(executorQueueCapacity);
        
        return executor;
    }

    /*
     * Reject policy is to just log the error 
     */
    @Bean(name = "nonCriticalTaskExecutor")
    public ThreadPoolTaskExecutor nonCriticalTaskExecutor(){
    	
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadGroupName("nonCriticalTaskExecutor");
        executor.setThreadNamePrefix("nonCriticalTaskExecutor-");
        executor.setCorePoolSize(executorCorePoolSize);
        executor.setMaxPoolSize(executorMaxPoolSize);
        executor.setKeepAliveSeconds(60);
        executor.setQueueCapacity(executorQueueCapacity);
		executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) { 
				logger.error("task rejected, executor info : " + executor);
			}
		});
        
        return executor;    	
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

    }

    @Bean(name = "kafkaProducerService")
    public KafkaProducerService<String> kafkaProducerService() {
        return new KafkaProducerService<String>(kafkaConfiguration().getTopicName(),
                kafkaProducer());
    }

    @Bean(name = "kafkaProducer")
    public KafkaProducer<String, String> kafkaProducer() {
        return new KafkaProducer<String, String>(Collections
                .<String, Object>unmodifiableMap(kafkaConfiguration().getProducerConfigs()));
    }

    @Bean(name = "kafkaConfiguration")
    public KafkaConfiguration kafkaConfiguration() {
        return new KafkaConfiguration();
    }

    @Bean
    public FilterRegistrationBean getAccessLogFilterRegistrationBean() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        AccessLoggingFilter accessLogFilter = new AccessLoggingFilter();
        accessLogFilter.setLoggingService(accessLoggingService());
        registration.setOrder(9999);
        registration.setFilter(accessLogFilter);
        return registration;
    }

    @Bean
    public AccessLoggingService accessLoggingService() {
        return new AccessLoggingService();
    }

    @Bean(name = "accessLoggingEventTypeProvider")
    public EventTypeProvider eventTypeProvider() {

        return new EventTypeProvider() {

            @Override
            public String getEventType(HttpServletRequest request) {

                String requestURI = request.getRequestURI();
                return requestURI;
            }
        };
    }

    @Bean(name = "wsrSpringRestClient")
    public SpringRestClient getWsrSpringRestClient() {
        SpringRestClient springRestClient = new SpringRestClient() {
            @Override
            public void init() throws Exception {
                super.init();
                setLogger(WsrSender.logger);
            }
        };

        return springRestClient;
    }

    @Bean(name = "gcHttpClient")
    public HttpClient gcHttpClient() {
        return new HttpClient();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {

        MappingJackson2HttpMessageConverter jacksonMessageConverter =
                new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = jacksonMessageConverter.getObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        converters.add(jacksonMessageConverter);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean(name = "longTimeoutCacheManager")
    public CacheManager longTimeoutCacheManager() {
        GuavaCacheManager cacheManager = new GuavaCacheManager();
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(cacheLongTimeoutMinute, TimeUnit.MINUTES);
        cacheManager.setCacheBuilder(cacheBuilder);
        return cacheManager;
    }

}
