package com.superluli.infra.commons.utils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CommonUtils {

    private static DateFormat FORMAT;

    private static ObjectMapper MAPPER = new ObjectMapper();

    static {
        FORMAT = new SimpleDateFormat("HH:mm:ss, MM/dd/yyyy zzz");
        FORMAT.setTimeZone(TimeZone.getTimeZone("PST"));
    }

    private CommonUtils() {

    }

    /**
     * We need to construct this so allow analytics request id not masked. two item sperated item
     * spearated by "-" is masked because PF is sending serail number and imei
     * 
     * @param requestId
     * @return
     */
    public static String constructRequestIdHeader(String requestId, Date time) {
        // We need to construct this so allow analytics request id not masked. two item sperated
        // item spearated by "-" is masked
        // because PF is sending serail number and imei

        return "*****" + "-" + "*****" + "-" + requestId + "-" + time.getTime();
    }

    public static String generateUUID() {

        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }

    public static String generateUUIDLowerCase() {

        return UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();
    }
    
    public static String toReadableDateTime(long timestamp) {

        return FORMAT.format(new Date(timestamp));
    }

    public static <T> T deepClone(T original, Class<T> targetClazz) {

        try {
            byte[] bytes = MAPPER.writeValueAsBytes(original);
            return MAPPER.readValue(bytes, targetClazz);

        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Check if there's special character in header value
     * 
     * @param str
     * @return
     */
    public static boolean isValidHeaderValue(String str) {

        return str != null && !str.isEmpty() && !str.contains("\\n") && !str.contains("\\r");
    }
}
