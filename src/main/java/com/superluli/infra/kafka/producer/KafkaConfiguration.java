package com.superluli.infra.kafka.producer;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.kafka", locations = {"${app.kafka.config.location}",
        "${spring.config.location}", "classpath:application.properties"})
public class KafkaConfiguration {

    private static final String NUM_RETRIES_3 = "3";
    private static final String ERROR_MISSING_REQUIRED_CONFIG = "Missing required configuration!";
    private static final String KEY_SERIALIZER_CLASS_CONFIG =
            "org.apache.kafka.common.serialization.StringSerializer";
    private static final String VALUE_SERIALIZER_CLASS_CONFIG =
    		"org.apache.kafka.common.serialization.StringSerializer";//"com.samsung.cloud.promotion.apiserver.kafka.producer.NodeSerializer";

    private String topicName;

    private Map<String, String> producerConfigs;

    public KafkaConfiguration() {

    }

    @PostConstruct
    public void init() throws Exception {

        if (topicName == null || producerConfigs == null || producerConfigs.isEmpty()) {
            throw new Exception(ERROR_MISSING_REQUIRED_CONFIG);
        }

        if (!producerConfigs.containsKey(ProducerConfig.RETRIES_CONFIG)) {
            producerConfigs.put(ProducerConfig.RETRIES_CONFIG, NUM_RETRIES_3);
        }

        if (!producerConfigs.containsKey(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)) {
            producerConfigs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                    KEY_SERIALIZER_CLASS_CONFIG);
        }

        if (!producerConfigs.containsKey(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)) {
            producerConfigs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                    VALUE_SERIALIZER_CLASS_CONFIG);
        }
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public Map<String, String> getProducerConfigs() {
        return producerConfigs;
    }

    public void setProducerConfigs(Map<String, String> producerConfigs) {
        this.producerConfigs = producerConfigs;
    }
}
