package com.superluli.infra.client;

import jodd.json.BeanSerializer;
import jodd.json.JsonContext;

public class MyBeanSerializer extends BeanSerializer {

    public MyBeanSerializer(JsonContext jsonContext, Object bean) {
        super(jsonContext, bean);
    }

    @Override
    protected void onSerializableProperty(String propertyName, Class propertyType, Object value) {
        if (value == null) {
            return;
        }
        
        super.onSerializableProperty(propertyName, propertyType, value);
    }
}