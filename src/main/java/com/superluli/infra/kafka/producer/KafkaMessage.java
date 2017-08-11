package com.superluli.infra.kafka.producer;

public class KafkaMessage<V> {

    private String key;

    private V value;

    public KafkaMessage(String key, V value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }
}
