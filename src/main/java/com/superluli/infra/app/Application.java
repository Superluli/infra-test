package com.superluli.infra.app;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

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
import com.superluli.infra.client.MyHttpClient;
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

    @Bean(name = "mainTaskExecutor")
    public ThreadPoolTaskExecutor mainTaskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadGroupName("mainTaskExecutor");
        executor.setThreadNamePrefix("mainTaskExecutor-");
        executor.setCorePoolSize(200);
        executor.setMaxPoolSize(5000);
        executor.setKeepAliveSeconds(60);
        executor.setQueueCapacity(1000);
        
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
        executor.setCorePoolSize(200);
        executor.setMaxPoolSize(5000);
        executor.setKeepAliveSeconds(60);
        executor.setQueueCapacity(1000);
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
        accessLogFilter.setLoggingService(new AccessLoggingService());
        registration.setOrder(9999);
        registration.setFilter(accessLogFilter);
        return registration;
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

    @Bean(name = "myHttpClient")
    public MyHttpClient myHttpClient() {
        return new MyHttpClient();
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
                .expireAfterWrite(10, TimeUnit.SECONDS);
        cacheManager.setCacheBuilder(cacheBuilder);
        return cacheManager;
    }

}
