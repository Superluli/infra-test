package com.superluli.infra.kafka.producer;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class KafkaProducerService<V> {

    private String topic;

    private KafkaProducer<String, V> producer;

	private Long producerSendTimeout = 2500L;

    public KafkaProducerService(String topic, KafkaProducer<String, V> producer) {
        this.topic = topic;
        this.producer = producer;
    }

    public RecordMetadata send(KafkaMessage<V> message) {
    	
    	Future<RecordMetadata> future = producer.send(
                new ProducerRecord<String, V>(topic, message.getKey(), message.getValue()));
        RecordMetadata metaData = null;
        
		try {
			metaData = future.get(producerSendTimeout, TimeUnit.MILLISECONDS);
			if (metaData != null) {
				
			}
		} catch (Exception ex) {
			
		}
		return metaData;
    }
}
