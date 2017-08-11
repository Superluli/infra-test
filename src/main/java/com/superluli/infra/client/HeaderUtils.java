package com.superluli.infra.client;

import java.util.UUID;

import org.springframework.util.MultiValueMap;

public class HeaderUtils {
	
  	public static final String PF_VERSION = "pfVersion";
	public static final String PF_VERSION_HEADER = "PF-Version";
	public static final String PF_VERSION_SPLITER = "\\.";
    public static final String REQUEST_SOURCE = "request-source";
    public static final String LOCATION_HEADER = "Location";
    public static final String DEVICE_PIP = "device-pip";
	public static String getDeviceCertUid(MultiValueMap<String, String> headers ) {
		String deviceCertIDInHeader = headers.getFirst("device-uid");
		if(deviceCertIDInHeader == null) {
			deviceCertIDInHeader = headers.getFirst("Device-Uid");
		}
		
		if(deviceCertIDInHeader == null) {
			deviceCertIDInHeader = headers.getFirst("Device-id");
		}
		
		if(deviceCertIDInHeader == null) {
			deviceCertIDInHeader = headers.getFirst("device-id");
		}
		return deviceCertIDInHeader;
	}
	
	

	public static String getServerCertId(MultiValueMap<String, String> headers ) {
		String serverCertId = headers.getFirst("server-cert-id");
		if(serverCertId == null) {
			serverCertId = headers.getFirst("Server-Cert-Id");
		}
		
		return serverCertId;
	}
	
	
	public static String getRequestId(MultiValueMap<String, String> headers ) {
		String requestId = headers.getFirst("request-id");
		if(requestId == null) {
			requestId = headers.getFirst("Request-Id");
		}
		
		return requestId;
	}
	
	
	
	
	public static boolean isPf(MultiValueMap<String,String> headers) {
		String requestResource = headers.getFirst(REQUEST_SOURCE) == null ? "" : headers.getFirst(REQUEST_SOURCE);
		if ("pf".equals(requestResource)) {
				return true;
		}
		return false;
			
	}
	
	public static boolean isWs(MultiValueMap<String,String> headers) {
		String requestResource = headers.getFirst(REQUEST_SOURCE) == null ? "" : headers.getFirst(REQUEST_SOURCE);
		if ("ws".equals(requestResource)) {
				return true;
		}
		return false;
			
	}
	
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }


}