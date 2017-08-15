package com.superluli.infra.accesslogging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Enumeration;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.superluli.infra.commons.Constants;

@Service
public class AccessLoggingService {

	private static final Logger logger = LoggerFactory.getLogger(AccessLoggingService.class);

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Value("${server.port}")
	private String host;

	@Autowired(required = false)
	@Qualifier("accessLoggingEventTypeProvider")
	public EventTypeProvider eventTypeProvider;

	@PostConstruct
	public void init() {

	}

	void logAccess(LoggingHttpServletRequestWrapper request, LoggingHttpServletResponseWrapper response,
			long elapsedTime) {

		ObjectNode logNode = getLogAccessNode(request, getEventType(request), elapsedTime);

		ObjectNode contextNode = (ObjectNode) logNode.get("context");
		contextNode.set("request", getRequestNode(request));
		contextNode.set("response", getResponseNode(response));

		logger.info(logNode.toString());
	}

	@Deprecated
	public void logThrown(LoggingHttpServletRequestWrapper request, Throwable ex) {

		ObjectNode logNode = MAPPER.createObjectNode();
		logNode.put(Constants.REQUEST_ID, request.getAttribute(Constants.REQUEST_ID).toString());
		logNode.set("throwable", getThrowableNode(ex));
		logger.error(logNode.toString());
	}

	private JsonNode getThrowableNode(Throwable ex) {

		ObjectNode createObjectNode = MAPPER.createObjectNode();

		createObjectNode.put("message", ex.getMessage());
		createObjectNode.set("stackTrace", MAPPER.convertValue(ex.getStackTrace(), JsonNode.class));
		if (ex.getCause() != null) {
			createObjectNode.set("cause", getThrowableNode(ex.getCause()));
		}

		return createObjectNode;
	}

	private ObjectNode getLogAccessNode(LoggingHttpServletRequestWrapper request, String eventType, long elapsedTime) {

		ObjectNode logNode = MAPPER.createObjectNode();
		logNode.put("host", host);
		logNode.put("eventType", eventType);
		logNode.put("timestamp", System.currentTimeMillis());
		logNode.set("context", MAPPER.createObjectNode());
		logNode.put("elapsedTime", elapsedTime);

		return logNode;
	}

	private ObjectNode getRequestNode(LoggingHttpServletRequestWrapper request) {

		ObjectNode requestNode = MAPPER.createObjectNode();

		String queryString = request.getQueryString();

		String requestPath = queryString == null ? request.getRequestURI()
				: request.getRequestURI() + "?" + queryString;

		requestNode.set("method", new TextNode(request.getMethod()));
		requestNode.set("url", new TextNode(requestPath));

		ObjectNode headersNode = MAPPER.createObjectNode();

		requestNode.set("headers", headersNode);

		Enumeration<String> headerNames = request.getHeaderNames();

		if (headerNames != null) {
			while (headerNames.hasMoreElements()) {
				String headerName = headerNames.nextElement();
				headersNode.put(headerName, request.getHeader(headerName));
			}
		}

		byte[] bytes = request.getBytes();

		if (bytes != null && bytes.length != 0) {

			ObjectNode bodyNode = MAPPER.createObjectNode();

			try {
				bodyNode = MAPPER.readValue(request.getInputStream(), ObjectNode.class);
			} catch (IOException e) {
				bodyNode.put("parseError", e.getMessage());
			}

			requestNode.set("body", bodyNode);
		}

		requestNode.put(Constants.REQUEST_ID, request.getAttribute(Constants.REQUEST_ID).toString());

		return requestNode;
	}

	private ObjectNode getResponseNode(LoggingHttpServletResponseWrapper response) {

		ObjectNode responseNode = MAPPER.createObjectNode();

		responseNode.put("status", response.getStatus());

		ObjectNode headersNode = MAPPER.createObjectNode();
		responseNode.set("headers", headersNode);
		Collection<String> headerNames = response.getHeaderNames();
		if (headerNames != null) {
			for (String headerName : headerNames) {
				headersNode.put(headerName, response.getHeader(headerName));
			}
		}

		byte[] responseBody = response.getBody();

		if (responseBody != null) {

			ObjectNode bodyNode = MAPPER.createObjectNode();

			try {
				bodyNode = MAPPER.readValue(response.getBody(), ObjectNode.class);
			} catch (IOException e) {
				bodyNode.put("parseError", e.getMessage());
				bodyNode.put("raw message", new String(response.getBody(), StandardCharsets.UTF_8));
			}
			responseNode.set("body", bodyNode);
		}

		return responseNode;
	}

	private String getEventType(LoggingHttpServletRequestWrapper request) {

		if (eventTypeProvider == null) {
			return "UNKOWN";
		} else {
			return eventTypeProvider.getEventType(request);
		}
	}
}
