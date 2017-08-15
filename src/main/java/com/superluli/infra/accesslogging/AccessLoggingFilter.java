package com.superluli.infra.accesslogging;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.superluli.infra.commons.CommonUtils;
import com.superluli.infra.commons.Constants;

public class AccessLoggingFilter implements Filter {
	
	public static Logger logger = LoggerFactory.getLogger(AccessLoggingFilter.class);

    private AccessLoggingService loggingService;

    public AccessLoggingService getLoggingService() {
		return loggingService;
	}

	public void setLoggingService(AccessLoggingService loggingService) {
		this.loggingService = loggingService;
	}

	@Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        LoggingHttpServletRequestWrapper wrappedRequest =
                new LoggingHttpServletRequestWrapper(httpServletRequest);
        LoggingHttpServletResponseWrapper wrappedResponse =
                new LoggingHttpServletResponseWrapper(httpServletResponse);

        String requestId = wrappedRequest.getHeader(Constants.REQUEST_ID);
        
        if (!CommonUtils.isValidHeaderValue(requestId)) {
            requestId = "INJECTED_" + CommonUtils.generateUUID();
        }
        
        wrappedRequest.setAttribute(Constants.REQUEST_ID, requestId);
        wrappedResponse.setHeader(Constants.RESPONSE_ID, requestId);
        
        long beforeProcessing = System.currentTimeMillis();
        chain.doFilter(wrappedRequest, wrappedResponse);
        
        loggingService.logAccess(wrappedRequest, wrappedResponse, System.currentTimeMillis()
                - beforeProcessing);
    }

    @Override
    public void destroy() {

    }
}
