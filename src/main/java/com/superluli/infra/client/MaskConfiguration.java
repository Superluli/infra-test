package com.superluli.infra.client;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 
 * string.replaceAll("(.)\1*(?!.{0,3}$)", "*") // MASK ONLY Last 4 Characters
 * string.replaceAll("(.).*", "CONTENT TO BIG") // MASK ALL { "logMask": {
 * "logMaskPolicies" : [ {
 * 
 * "logMaskRegex" : { "pattern" : "(.)\1*(?!.{0,3}$)", "replaceWith" : "*" },
 * 
 * "logAffectedFields" : [ ".*imei.*", "", "dddd"
 * 
 * ] }
 * 
 * @author dave
 *
 */
@Component
@ConfigurationProperties(prefix = "mask")
@Validated
public class MaskConfiguration {
	@Size(min = 1)
	private List<MaskPolicy> maskPolicies;

	public List<MaskPolicy> getMaskPolicies() {
		return maskPolicies;
	}

	public void setMaskPolicies(List<MaskPolicy> maskPolicies) {
		this.maskPolicies = maskPolicies;
	}

	public static class MaskPolicy {
        @NotNull
		private MaskRegex maskRegex;
		public enum MaskType{LOG};
		
		private MaskType maskType;
		private List<String> maskFields;
		public MaskRegex getMaskRegex() {
			return maskRegex;
		}
		public void setMaskRegex(MaskRegex maskRegex) {
			this.maskRegex = maskRegex;
		}
		public MaskType getMaskType() {
			return maskType;
		}
		public void setMaskType(MaskType maskType) {
			this.maskType = maskType;
		}
		public List<String> getMaskFields() {
			return maskFields;
		}
		public void setMaskFields(List<String> maskFields) {
			this.maskFields = maskFields;
		}
		

		
		
		

		
	}

	public static class MaskRegex {
		private String pattern;
		private String replacement;

		public String getPattern() {
			return pattern;
		}

		public void setPattern(String pattern) {
			this.pattern = pattern;
		}

		public String getReplacement() {
			return replacement;
		}

		public void setReplacement(String replacement) {
			this.replacement = replacement;
		}

		

	}

}
