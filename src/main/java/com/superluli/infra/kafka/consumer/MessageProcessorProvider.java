package com.superluli.infra.kafka.consumer;

import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class MessageProcessorProvider {

    private static final Logger logger = LoggerFactory.getLogger(MessageProcessorProvider.class);

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        String[] beanNames = applicationContext.getBeanNamesForType(MessageProcessor.class);
        logger.info("MessageProcessors: " + Arrays.asList(beanNames).toString());
    }

    public MessageProcessor getProcessor(String name) {
        return (MessageProcessor) applicationContext.getBean(name);
    }
}
