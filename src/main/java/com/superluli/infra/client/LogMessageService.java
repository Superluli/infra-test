package com.superluli.infra.client;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jodd.json.JsonSerializer;

@Component
public class LogMessageService {

    @Value("${tokenRequestor.cardType:unknown}")
    private String TOKEN_REQ_ID_STR;

    private static final String EXCEPTION_STR = "exception";

    private static final String DETAIL_STR = "detail";

    public static final String TR_STR = "TR";

    @Autowired
    private MaskManager logMaskManager;

    private JsonSerializer jsonSerializer;

    @PostConstruct
    public void init() {
        jsonSerializer = new JsonSerializer();
        jsonSerializer.use(String.class, logMaskManager.getLogMaskSerializer()).deep(true);

    }

    public Builder builder() {
        Builder builder = new Builder();
        return builder.initializeBuilder();
    }

    public class Builder {
        // help saving all key-pairs in logMessage
        private Map<String, Object> fields;

        public Builder() {
            fields = new LinkedHashMap<String, Object>();
        }

        protected Builder initializeBuilder() {
            // Adding the TR tag
            return this.customizedMessage(TR_STR, TOKEN_REQ_ID_STR);
        }

        public Builder customizedMessage(String key, Object message) {
            fields.put(key, message);
            return this;
        }


        public Builder exception(Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String stacktrace = errors.toString();
            return customizedMessage(EXCEPTION_STR, e.getClass().getSimpleName())
                    .customizedMessage(DETAIL_STR, stacktrace);
        }

        public Builder throwable(Throwable t) {
            StringWriter errors = new StringWriter();
            t.printStackTrace(new PrintWriter(errors));
            String stacktrace = errors.toString();
            return customizedMessage(EXCEPTION_STR, t.getClass().getSimpleName())
                    .customizedMessage(DETAIL_STR, stacktrace);
        }

        public String build() {

            LogMessage logMessage = new LogMessage(this.fields);
            String json = jsonSerializer.serialize(logMessage);
            return json;
            // return logMessage.toJsonStr();
        }
    }

    public String getTokenRequestId() {
        return TOKEN_REQ_ID_STR;
    }
}
