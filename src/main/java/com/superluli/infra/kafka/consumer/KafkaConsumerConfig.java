package com.superluli.infra.kafka.consumer;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.kafka.config", locations = {"${app.kafka.config.location}",
        "${spring.config.location}", "classpath:application.properties"})
public class KafkaConsumerConfig {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    private Map<String, String> consumerConfig;

    private List<TopicConfig> topics;

    @PostConstruct
    public void init() {
        logger.info(toString());
    }

    public static final class TopicConfig {

        private String name;

        private String messageProcessor;
        
        private Integer numOfStreams = 1;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMessageProcessor() {
            return messageProcessor;
        }

        public void setMessageProcessor(String messageProcessor) {
            this.messageProcessor = messageProcessor;
        }

        public Integer getNumOfStreams() {
            return numOfStreams;
        }

        public void setNumOfStreams(Integer numOfStreams) {
            this.numOfStreams = numOfStreams;
        }

        @Override
        public String toString() {
            return "TopicConfig [name=" + name + ", messageProcessor=" + messageProcessor
                    + ", numOfStreams=" + numOfStreams + "]";
        }
    }

    public Map<String, String> getConsumerConfig() {
        return consumerConfig;
    }

    public void setConsumerConfig(Map<String, String> consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public List<TopicConfig> getTopics() {
        return topics;
    }

    public void setTopics(List<TopicConfig> topics) {
        this.topics = topics;
    }

    @Override
    public String toString() {
        return "KafkaConsumerConfig [consumerConfig=" + consumerConfig + ", topics=" + topics + "]";
    }
}
