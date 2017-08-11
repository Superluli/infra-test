package com.superluli.infra.client;

import java.util.Map;

public class LogMessage {

	private Map<String, Object> fields;


	
	LogMessage(Map<String, Object> fields) {
		this.fields = fields;
	}



	public Map<String, Object> getFields() {
		return fields;
	}



	public void setFields(Map<String, Object> fields) {
		this.fields = fields;
	}
	
	


}
