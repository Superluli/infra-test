package com.superluli.infra.client;

import jodd.json.JsonContext;
import jodd.json.impl.ValueJsonSerializer;

public class MyObjectJsonSerializer extends ValueJsonSerializer<Object> {

    public void serializeValue(final JsonContext jsonContext, Object value) {
        jsonContext.writeOpenObject();

        MyBeanSerializer beanVisitor = new MyBeanSerializer(jsonContext, value);
        beanVisitor.serialize();

        jsonContext.writeCloseObject();
    }
}