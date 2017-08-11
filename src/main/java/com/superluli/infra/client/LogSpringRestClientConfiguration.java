package com.superluli.infra.client;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "logSpringRestClient")
@Validated
public class LogSpringRestClientConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(LogSpringRestClientConfiguration.class);

	private String hello;
    private boolean loggedAll;

	@Valid
    @Size(min = 1)
	private Map<String, LogSpringRestClient> logSpringRestClient;

	public LogSpringRestClientConfiguration() {
	    logger.debug("In the constructor");
	}

	public boolean isLoggedAll() {
		return loggedAll;
	}

	public void setLoggedAll(boolean loggedAll) {
		this.loggedAll = loggedAll;
	}


	public String getHello() {
		return hello;
	}

	public void setHello(String hello) {
		this.hello = hello;
	}

	public Map<String, LogSpringRestClient> getLogSpringRestClient() {
		return logSpringRestClient;
	}

	public void setLogSpringRestClient(Map<String, LogSpringRestClient> logSpringRestClient) {
		this.logSpringRestClient = logSpringRestClient;
	}


	public static class LogSpringRestClient {
	    @NotNull
		private String alias;
		private List<LogIncludeField> includes;
		private boolean loggedAll;
		public List<LogIncludeField> getIncludes() {
			return includes;
		}
		public void setIncludes(List<LogIncludeField> includes) {
			this.includes = includes;
		}
		public boolean isLoggedAll() {
			return loggedAll;
		}
		public void setLoggedAll(boolean loggedAll) {
			this.loggedAll = loggedAll;
		}
		public String getAlias() {
			return alias;
		}
		public void setAlias(String alias) {
			this.alias = alias;
		}
		
	}
	

	public static class LogIncludeField {

		private List<String> logReturn;
		private Map<String, String> logIf;

		public List<String> getLogReturn() {
			return logReturn;
		}

		public void setLogReturn(List<String> logReturn) {
			this.logReturn = logReturn;
		}

		public Map<String, String> getLogIf() {
			return logIf;
		}

		public void setLogIf(Map<String, String> logIf) {
			this.logIf = logIf;
		}

		public static class LogIf {
			private String uri;
			private String method;

			public String getUri() {
				return uri;
			}

			public void setUri(String uri) {
				this.uri = uri;
			}

			public String getMethod() {
				return method;
			}

			public void setMethod(String method) {
				this.method = method;
			}

		}

	}

}
