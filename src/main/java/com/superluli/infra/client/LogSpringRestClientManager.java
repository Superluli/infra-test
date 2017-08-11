package com.superluli.infra.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.superluli.infra.client.LogSpringRestClientConfiguration.LogIncludeField;
import com.superluli.infra.client.LogSpringRestClientConfiguration.LogSpringRestClient;

import jodd.json.JsonSerializer;
import jodd.util.Wildcard;

@Component
public class LogSpringRestClientManager {
	@Autowired
	private LogSpringRestClientConfiguration logSpringRestClientConfiguration;

	private Map<String, Map<LogIncludeField, JsonSerializer>> logSpringRestClientIncludeMap = null;
	
	private Map<String,String> aliasMap;
	@Autowired
	private MaskManager logMaskManager;
	
	private boolean hasLogSpringRestClientConfig;
	
	private JsonSerializer jsonSerializer;
	

	@PostConstruct
	public void init() {
		jsonSerializer = new JsonSerializer();
		
		jsonSerializer
		.use(Object.class, new MyObjectJsonSerializer())
		.use(ObjectNode.class, new JacksonObjectNodeSerializer())
		.use(String.class, logMaskManager.getLogMaskSerializer())
		
		.deep(true);
		
		logSpringRestClientIncludeMap = new HashMap<String, Map<LogIncludeField, JsonSerializer>>();
		aliasMap = new HashMap<String,String>();
		Map<String, LogSpringRestClient> logSpringRestClientMap = logSpringRestClientConfiguration
				.getLogSpringRestClient();
		
		if(logSpringRestClientMap == null || logSpringRestClientMap.keySet().isEmpty()) {
			hasLogSpringRestClientConfig = false;
			return;
		} else {
			hasLogSpringRestClientConfig = true;
		}
		//Set<String> names = logSpringRestClientMap.keySet();
		for (Map.Entry<String, LogSpringRestClient> entry : logSpringRestClientMap.entrySet()) {
			LogSpringRestClient logRestClient = entry.getValue();
			Map<LogIncludeField, JsonSerializer> logIncludeFieldMap = getLogIncludeFieldMap(logRestClient);
			logSpringRestClientIncludeMap.put(entry.getKey(), logIncludeFieldMap);
			
			aliasMap.put(entry.getKey(), logRestClient.getAlias());
		}

	}
	
	
	public boolean hasLogSpringRestClientConfig() {
		return hasLogSpringRestClientConfig;
	}
	
	public MaskManager getLogMaskManager() {
		return logMaskManager;
	}

	

	public String getAlias(String name) {
		
		String alias = aliasMap.get(name);
		if(alias == null) {
			return name;
		}
		return alias;
	}
	public LogSpringRestClientConfiguration getLogSpringRestClientConfiguration() {
		return logSpringRestClientConfiguration;
	}



	public void setLogSpringRestClientConfiguration(
			LogSpringRestClientConfiguration logSpringRestClientConfiguration) {
		this.logSpringRestClientConfiguration = logSpringRestClientConfiguration;
	}



	private Map<LogIncludeField, JsonSerializer> getLogIncludeFieldMap(
			LogSpringRestClient logRestClient) {
		
		Map<LogIncludeField,JsonSerializer> toReturn = new HashMap<LogIncludeField,JsonSerializer>();

		if (logRestClient != null && logRestClient.getIncludes() != null) {
			List<LogIncludeField> includes = logRestClient.getIncludes();
			if (includes != null) {
				for (LogIncludeField logIncludeField : includes) {
					JsonSerializer jsonSerializer = new JsonSerializer();
					List<String> logReturns = logIncludeField.getLogReturn();
					jsonSerializer
					.use(Object.class, new MyObjectJsonSerializer())
					.use(ObjectNode.class, new JacksonObjectNodeSerializer())
	                .use(String.class, logMaskManager.getLogMaskSerializer())
	                
	                .exclude("*")
	                .deep(true);
					for (String logReturn : logReturns) {
						jsonSerializer.include(logReturn);
					}

					toReturn.put(logIncludeField, jsonSerializer);

				}
			}
		}
		return toReturn;
	}

	public String serialzeJson(String loggerName, Map<String, Object> logs) {

		if (logSpringRestClientConfiguration.isLoggedAll()) {

			try {
				String json = jsonSerializer.serialize(logs);
				return json;
			} catch (Exception e) {
				return null;
			}
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> logRequest = (Map<String, Object>) logs
				.get("request");
		
		if (logRequest == null) {
			return null;
		}

		if (!logRequest.containsKey("uri") && !logRequest.containsKey("method")) {
			return null;
		}
		String name = StringUtil.getLastIndexString(loggerName, ".");
		String requestUrl =  logRequest.get("url").toString();
		String requestMethod = logRequest.get("method").toString();
		
		LogIncludeField logIncludeField = getLogIncludeField(name,
				requestUrl, requestMethod);
		if (logIncludeField != null) {
			JsonSerializer jsonSerializer = getJsonSerializer(name, logIncludeField);
			String json = jsonSerializer.serialize(logs);
			if (json != null) {
				json = json.replaceAll("\\\\/", "/");
			}
			return json;
		} else {
			return null;
		}

	}
	
	public String serializerJSONDebug(Map<String,Object> logs)  {
		
			try {
				String json = jsonSerializer.serialize(logs);
				return json;
			} catch (Exception e) {
				return null;
			}
		
	}

	private LogIncludeField getLogIncludeField(
			String className, String requestUrl, String requestMethod) {

		LogIncludeField toReturn = null;
		if (logSpringRestClientConfiguration != null) {
			List<LogIncludeField> includes =  getLogIncludeField(className);
			if (includes != null) {
				for (LogIncludeField logIncludeField : includes) {
					Map<String, String> logIf = logIncludeField.getLogIf();
					boolean matches = true;
					if (logIf.keySet().isEmpty()) {
						matches = false;
					}
					for (Map.Entry<String, String> entry : logIf.entrySet()) {
						String key = entry.getKey();
						String pattern = entry.getValue();
						if ("requestUrl".equals(key)) {
							matches = Wildcard.matchPath(requestUrl, pattern);

						}

						if ("requestMethod".equals(key)) {
							matches = Wildcard.match(requestMethod, pattern);
						}

					
						if (!matches) {
							break;
						}

					}

					if (matches) {
						toReturn = logIncludeField;
						return toReturn;
					}

				}
			}
		}
		return toReturn;
	}

	
	
	
	private JsonSerializer getJsonSerializer(String className, LogIncludeField logIncludeField) {
		
		if(logSpringRestClientIncludeMap.containsKey(className)) {
			return logSpringRestClientIncludeMap.get(className).get(logIncludeField);
		}
		return null;
		
	}
	
	
	private List<LogIncludeField> getLogIncludeField(String className) {
		
		if(logSpringRestClientConfiguration.getLogSpringRestClient() != null && logSpringRestClientConfiguration.getLogSpringRestClient().containsKey(className)) {
			List<LogIncludeField> includes = logSpringRestClientConfiguration.getLogSpringRestClient().get(className).getIncludes();
			return includes;
		}
		return null;
		
		
	}
	



}
