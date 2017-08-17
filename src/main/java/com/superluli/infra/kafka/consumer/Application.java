package com.superluli.infra.kafka.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

//@SpringBootApplication
//@ComponentScan("com.superluli.infra.kafka")
public class Application {

//    public static void main(String[] args) {
//
//        SpringApplication app = new SpringApplication(Application.class);
//        /*
//         * Do not start server
//         */
//        app.setWebEnvironment(false);
//        app.run(args);
//    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
}
