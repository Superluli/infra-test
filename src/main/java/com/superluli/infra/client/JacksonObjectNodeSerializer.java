package com.superluli.infra.client;

import java.util.Map;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.superluli.infra.commons.JsonUtil;

import jodd.json.JsonContext;
import jodd.json.Path;
import jodd.json.impl.ValueJsonSerializer;
import jodd.util.StringPool;

public class JacksonObjectNodeSerializer extends ValueJsonSerializer<ObjectNode> {

	public void serializeValue(JsonContext jsonContext, ObjectNode objectNode) {
		jsonContext.writeOpenObject();

		int count = 0;
		
		Map<?,?> map = JsonUtil.getMapper().convertValue(objectNode, Map.class);
		
		
		Path currentPath = jsonContext.getPath();

		for (Map.Entry<?, ?> entry : map.entrySet()) {
			final Object key = entry.getKey();
			final Object value = entry.getValue();

			if (key != null) {
				currentPath.push(key.toString());
			} else {
				currentPath.push(StringPool.NULL);
			}

			// check if we should include the field

			boolean include = true;

			if (value != null) {

				// + all collections are not serialized by default

				include = jsonContext.matchIgnoredPropertyTypes(value.getClass(), false, include);

				// + path queries: excludes/includes

				include = jsonContext.matchPathToQueries(include);
			}

			// done

			if (!include) {
				currentPath.pop();
				continue;
			}

			if (key == null) {
				jsonContext.pushName(null, count > 0);
			} else {
				jsonContext.pushName(key.toString(), count > 0);
			}

			jsonContext.serialize(value);

			if (jsonContext.isNamePopped()) {
				count++;
			}

			currentPath.pop();
		}

		jsonContext.writeCloseObject();
	}
	
	
//	public static class POJO {
//		private ObjectNode securedData;
//		private String hello;
//		public ObjectNode getSecuredData() {
//			return securedData;
//		}
//		public void setSecuredData(ObjectNode securedData) {
//			this.securedData = securedData;
//		}
//		public String getHello() {
//			return hello;
//		}
//		public void setHello(String hello) {
//			this.hello = hello;
//		}
//	}
//	
//
//	public static void main(String[] args) {
//		
//		POJO pojo = new POJO();
//		pojo.setHello("HELLO");
//		pojo.setSecuredData(JsonUtil.newObjectNode());;
//		JsonSerializer logAllSerializer = new JsonSerializer();
//		logAllSerializer.use(Object.class, new MyObjectJsonSerializer())
//		.use(ObjectNode.class, new JacksonObjectNodeSerializer());
//		System.out.println(logAllSerializer.serialize(pojo));
//		
//	}
}