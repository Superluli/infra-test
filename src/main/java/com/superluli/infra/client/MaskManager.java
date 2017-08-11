package com.superluli.infra.client;


import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.superluli.infra.client.MaskConfiguration.MaskPolicy;
import com.superluli.infra.client.MaskConfiguration.MaskPolicy.MaskType;
import com.superluli.infra.client.MaskConfiguration.MaskRegex;

import jodd.json.JsonContext;
import jodd.json.TypeJsonSerializer;
import jodd.util.Wildcard;

@Component
public class MaskManager {

	@Autowired
	private MaskConfiguration maskConfiguration;

	private TypeJsonSerializer<String> logMaskSerializer;

	@Autowired
	@Value("${mask.config.exceptlast4.regex.pattern:(.*).(?!.{0,3}$)}")
	private String REGEX_EXCEPT_LAST4;
	
	@Value("${mask.config.all.regex:(.*).}")
	private  String REGEX_MASK_ALL;
	
	@Value("${mask.config.all.regex.replacement:*****}")
	private  String REPLACEMENT_MASK_ALL;
	
	
	@Value("${mask.config.exceptlast4.regex.replacement:*****}")
	private  String REPLACEMENT_EXCEPT_LAST4;

	@PostConstruct
	public void init() {
		logMaskSerializer = newJsonTypeSerializer(MaskType.LOG);
	}

	public TypeJsonSerializer<String> getLogMaskSerializer() {
		return logMaskSerializer;
	}

	public String maskExceptLast4(String string) {
		if(string == null) {
			return null;
		}
		String newValue = string.replaceAll(REGEX_EXCEPT_LAST4,REPLACEMENT_EXCEPT_LAST4);
		return newValue;
	}
	
	public String maskAll(String string) {
		if(string == null) {
			return null;
		}
		return string.replaceAll(REGEX_MASK_ALL,REPLACEMENT_MASK_ALL );
		
		
	}
	
	public String maskReqId(String requestId) {
		if(requestId == null) {
			return null;
		}
		String[] requestIdArray = requestId.split("-");
		if(requestIdArray.length >= 1) {
			StringBuilder reqIdBuilder = new StringBuilder();
			for(int i = 0; i < requestIdArray.length;i++) {
				if(i!= 0) {
					reqIdBuilder.append("-");
				}
				String preqId = requestIdArray[i];
				if(i == 1 || i == 0) {
					reqIdBuilder.append(maskExceptLast4(preqId));
				} else {
					reqIdBuilder.append(preqId);
				}
				
				
			}
			return reqIdBuilder.toString();
		}
		return requestId;
		
	}

	private TypeJsonSerializer<String> newJsonTypeSerializer(MaskType myMaskType) {

		return new TypeJsonSerializer<String>() {
			
			
			@Override
			public void serialize(JsonContext jsonContext, String value) {
				
				if (value == null) {
					return;
				}
			
				String path = jsonContext.getPath().toString().replaceAll("\\[|\\]","");
					List<MaskPolicy> maskPolicies = maskConfiguration.getMaskPolicies();
					if(maskPolicies != null) {
						for (MaskPolicy maskPolicy : maskPolicies) {
							MaskType maskType = maskPolicy.getMaskType();
							if(maskType == myMaskType) {
								for (String logMaskField : maskPolicy.getMaskFields()) {
									
									if (Wildcard.match(path, logMaskField)) {
										
										MaskRegex logMaskRegex = maskPolicy.getMaskRegex();
										String pattern = logMaskRegex.getPattern();
										String replacement = logMaskRegex.getReplacement();
										if (pattern != null && replacement != null) {
											String newValue = value.replaceAll(pattern, replacement);
											jsonContext.writeString(newValue);
											return;
										}

									}
								}
							}
							
							
						}
					}
				jsonContext.writeString(value);
			}
		};

	}
	

}
