package com.superluli.infra.client;

public class StringUtil {
	public static String getIdFromHref(String href) {
		if(href == null) {
			return null;
		}
		int lastIndex = href.lastIndexOf("/");
		if(lastIndex > 0) {
			String id =href.substring(lastIndex+1);
			return id;
		}
		return null;
	}
	
	
	public static String getLastIndexString(String str, String c ) {
		if(str == null) {
			return null;
		}
		int lastIndex = str.lastIndexOf(c);
		if(lastIndex > 0) {
			String toReturn =str.substring(lastIndex+1);
			return toReturn;
		}
		return str;
	}
	
	
	public static String strip(String str, int length ) {
		if(str == null) {
			return null;
		}
		int maxLength = 0;
		if(str.length() < length) {
			maxLength = str.length();
		} else {
			maxLength = length;
		}
			
			String toReturn =str.substring(0, maxLength);
			return toReturn;
		
	
	}
	
	public static String getSplitString(String str, String c,int index ) {
		if(str == null) {
			return null;
		}
		String[] strArray = str.split(c);
		
		if(index < strArray.length) {
			
			return strArray[index];
		}
		return str;
	}

}
