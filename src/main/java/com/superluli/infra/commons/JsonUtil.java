package com.superluli.infra.commons;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public final class JsonUtil {

	private static final ObjectMapper mapper;

	private JsonUtil() {

	}

	static {
		mapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE)
				.setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
				.setVisibility(PropertyAccessor.GETTER, Visibility.ANY);

		mapper.registerModule(new SimpleModule("MyModule").addSerializer(byte[].class, new ByteArraySerializer()));
	}

	public static String toJson(Object value) throws JsonGenerationException, JsonMappingException, IOException {
		return mapper.writeValueAsString(value);
	}

	public static void toJson(OutputStream out, Object value)
			throws JsonGenerationException, JsonMappingException, IOException {
		mapper.writeValue(out, value);
	}

	public static <T> T fromJson(String src, Class<T> clazz)
			throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(src, clazz);
	}

	public static <T> T fromJson(InputStream source, Class<T> clazz)
			throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(source, clazz);
	}

	public static <T> T fromJson(String src, TypeRef<T> ref)
			throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(src, ref);
	}

	public static <T> T fromJson(InputStream src, TypeRef<T> ref)
			throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(src, ref);
	}

	public static JsonNode fromJson(InputStream source) throws JsonProcessingException, IOException {
		return mapper.readTree(source);
	}

	public static JsonNode fromJson(String src) throws JsonProcessingException, IOException {
		return mapper.readTree(src);
	}

	public static ObjectNode newObjectNode() {
		return mapper.createObjectNode();
	}

	public static ArrayNode newArrayNode() {
		return mapper.createArrayNode();
	}

	public static JsonNode toNode(Object value) {
		return mapper.valueToTree(value);
	}

	public static <T> T toObject(JsonNode node, Class<T> clazz) throws JsonProcessingException {
		return mapper.treeToValue(node, clazz);
	}

	public static String toPrettifyString(Object object)
			throws JsonGenerationException, JsonMappingException, IOException {
		return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
	}

	// Here we need a defensive copy
	// TODO Haven't got how to do it in new version jackson api
	public static ObjectMapper getMapper() {
		return mapper;
	}

	/**
	 * A wrapper class of com.fasterxml.jackson.core.type.TypeReference for
	 * encapsulation purpose
	 * 
	 * @param <T>,
	 */
	public static abstract class TypeRef<T> extends TypeReference<T> {

	}

	@JacksonStdImpl
	static class ByteArraySerializer extends StdSerializer<byte[]> {

		/**
			 * 
			 */
		private static final long serialVersionUID = 1L;

		public ByteArraySerializer() {
			super(byte[].class);
		}

		@Override
		public void serialize(byte[] bytes, JsonGenerator jgen, SerializerProvider provider)
				throws IOException, JsonProcessingException {
			jgen.writeStartArray();
			for (byte b : bytes) {
				jgen.writeNumber(unsignedToBytes(b));
			}
			jgen.writeEndArray();
		}

		@Override
		public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
			ObjectNode o = createSchemaNode("array", true);
			o.set("items", createSchemaNode("integer"));
			return o;
		}

		private static int unsignedToBytes(byte b) {
			return b & 0xFF;
		}

		@Override
		public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint)
				throws JsonMappingException {
			if (visitor != null) {
				JsonArrayFormatVisitor v2 = visitor.expectArrayFormat(typeHint);
				if (v2 != null) {
					v2.itemsFormat(JsonFormatTypes.INTEGER);
				}
			}
		}

	}
}
