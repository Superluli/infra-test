package com.superluli.infra.kafka.consumer;

public interface MessageProcessor {

    public void processMessage(String key, byte[] message);
}
