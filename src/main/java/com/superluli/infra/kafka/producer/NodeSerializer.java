package com.superluli.infra.kafka.producer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.superluli.infra.commons.JsonUtil;

public class NodeSerializer implements Serializer<ObjectNode> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {}

    @Override
    public byte[] serialize(String topic, ObjectNode data) {
        try {
            if (data == null) {
                return null;
            } else
                return JsonUtil.toJson(data).getBytes(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new SerializationException(
                    "Error when serializing string to byte[] due to" + e.getMessage(), e);
        }
    }

    @Override
    public void close() {}
}
