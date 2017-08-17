package com.superluli.infra.kafka.consumer;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.superluli.infra.kafka.consumer.KafkaConsumerConfig.TopicConfig;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;

//@Component
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Autowired
    private KafkaConsumerConfig kafkaConsumerConfig;
    
    @Autowired
    private MessageProcessorProvider messageProcessorProvider;

    //private ConsumerConnector consumerConnector;
    
    //private ConsumerConnector pushConsumerConnector;
    
    private Map<String, ConsumerConnector> consumerConnectors;
    
    private Map<String, List<KafkaStream<byte[], byte[]>>> streamsMap;

    private ExecutorService executorService;

    @PostConstruct
    public void init() {

        logger.info("----------- Initializing KafkaConsumerService 0004 ---------");
        
        Properties props = new Properties();
        props.put("zookeeper.session.timeout.ms", "1000");
        props.put("zookeeper.sync.time.ms", "1000");
        props.put("auto.commit.enable", "false");
        props.put("auto.offset.reset", "smallest");
        // add new properties defined in config file. this will also override the default values
        props.putAll(kafkaConsumerConfig.getConsumerConfig());
        
        consumerConnectors = new HashMap<String, ConsumerConnector>();
        streamsMap = new HashMap<String, List<KafkaStream<byte[], byte[]>>>();

        // create consumerConnector
        logger.info("Creating consumer connector using properties : " + props.toString());

        int poolSize = 0;
        
        // createMessageStreams
        //Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        kafkaConsumerConfig.getTopics().forEach(value -> {
        	
        	ConsumerConnector cConnector = Consumer.createJavaConsumerConnector(new ConsumerConfig(props));
        	Map<String, List<KafkaStream<byte[], byte[]>>> sMap = cConnector.createMessageStreams(Collections.singletonMap(value.getName(), value.getNumOfStreams()));
        	
        	consumerConnectors.put(value.getName(), cConnector);
        	streamsMap.put(value.getName(), sMap.get(value.getName()));
        	
            logger.info("Adding topic '" + value.getName() + "'");
        });
        
        Iterator<TopicConfig> iTopics = kafkaConsumerConfig.getTopics().iterator();
        while (iTopics.hasNext()) {
        	poolSize += iTopics.next().getNumOfStreams();
        }
        
        // init executorService
        executorService = Executors.newFixedThreadPool(poolSize);
        logger.info("Created executor service of size: " + poolSize);

        // submit tasks
        int threadNumber = 0;
        for (TopicConfig topicConfig : kafkaConsumerConfig.getTopics()) {
        	logger.info("-={ topicConfig " + topicConfig.getName());
        	
            List<KafkaStream<byte[], byte[]>> streams = streamsMap.get(topicConfig.getName());
            		
            for (final KafkaStream<byte[], byte[]> stream : streams) {
            	logger.info("-={ stream " + stream.toString() + " identityHashCode: " + System.identityHashCode(stream));
            	logger.info("-={ threadNumber " + threadNumber);
                
            	executorService.submit(new KafkaStreamProcesser(consumerConnectors.get(topicConfig.getName()), stream,
                        threadNumber,
                        messageProcessorProvider.getProcessor(topicConfig.getMessageProcessor())));
            	
                logger.info("****** messageProcessorProvider.getProcessor(topicConfig.getMessageProcessor()) " 
                        + messageProcessorProvider.getProcessor(topicConfig.getMessageProcessor()));
                logger.info("topicConfig.getMessageProcessor() " + topicConfig.getMessageProcessor());
                threadNumber++;
            }
        }
        
        logger.info("----------- Initializing KafkaConsumerService Done ---------");
    }

    @PreDestroy
    public void clear() {
    	
    	Iterator<ConsumerConnector> iCC = consumerConnectors.values().iterator();
    	while (iCC.hasNext()) {
    		ConsumerConnector consumerConnector = iCC.next();
    		if (consumerConnector != null) {
                consumerConnector.shutdown();
            }
    	}

        if (executorService != null) {
            executorService.shutdown();
        }
        try {
            if (!executorService.awaitTermination(10000, TimeUnit.MILLISECONDS)) {
                logger.info("Timed out waiting for consumer threads to shut down, exiting uncleanly");
            }
        } catch (InterruptedException e) {
            logger.info("Interrupted during shutdown, exiting uncleanly");
        }
    }

    public static class KafkaStreamProcesser implements Runnable {

        private ConsumerConnector consumerConnector;
        private KafkaStream<byte[], byte[]> stream;
        private int threadNumber;
        private MessageProcessor messageProcessor;

        public KafkaStreamProcesser(ConsumerConnector consumerConnector,
                KafkaStream<byte[], byte[]> stream, int threadNumber,
                MessageProcessor messageProcessor) {
            this.consumerConnector = consumerConnector;
            this.stream = stream;
            this.threadNumber = threadNumber;
            this.messageProcessor = messageProcessor;
        }

        @Override
        public void run() {

            try {
                ConsumerIterator<byte[], byte[]> it = stream.iterator();

                while (it.hasNext()) {

                    MessageAndMetadata<byte[], byte[]> message = it.next();

                    String key = message.key() == null ? "NO_ID" : new String(message.key(),
                        StandardCharsets.UTF_8);
                    
                    if (logger.isDebugEnabled()) {

                        logger.debug("StreamProcessor thread #" + threadNumber
                                + " fetched message ID : " + key + ", message : "
                                + new String(message.message(), StandardCharsets.UTF_8));
                    }
                    
                    else if (logger.isInfoEnabled()) {

                        logger.info("StreamProcessor thread #" + threadNumber
                                + " fetched message ID : " + key);
                    }

                    logger.info("messageProcessor is a " + messageProcessor.getClass().getName());
                    messageProcessor.processMessage(key, message.message());
                    consumerConnector.commitOffsets();
                    //try {
                    //	Thread.sleep(1000);
                    //} catch (Exception e) {}
                    
                }
                
            } catch (Exception e) {

                logger.error("StreamProcesser #" + threadNumber + "critical failure", e);
            }
        }
    }
}
