package com.superluli.infra.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public class GenericLogMessageService extends LogMessageService {

    private static final Logger logger = LoggerFactory.getLogger(GenericLogMessageService.class);

    @Value("${service.name:unknown}")
    private String serviceName;

    @Override
    public void init() {
        super.init();
        logger.info(String.format("service.name: %s", serviceName));
    }

    @Override
    public String getTokenRequestId() {
        return serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    @Override
    public Builder builder() {
        GenericBuilder builder = new GenericBuilder();
        return builder.initializeBuilder();
    }

    public class GenericBuilder extends LogMessageService.Builder {

        @Override
        protected Builder initializeBuilder() {
            return customizedMessage("serviceName", serviceName);
        }
    }
}
