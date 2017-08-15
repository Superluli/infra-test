package com.superluli.infra.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.superluli.infra.accesslogging.LoggingHttpServletRequestWrapper;
import com.superluli.infra.commons.Constants;
import com.superluli.infra.commons.resources.AbstractRestResource;
import com.superluli.infra.exception.ConflictResourceException;
import com.superluli.infra.exception.NestedServerRuntimeException;


@ControllerAdvice
public class DefaultExceptionHandler extends ResponseEntityExceptionHandler {

    private static Logger logger = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    @ExceptionHandler(ConflictResourceException.class)
    @ResponseBody
    ResponseEntity<?> handleConflictResourceException(LoggingHttpServletRequestWrapper request,
    		ConflictResourceException ex) {

        logger.error(Constants.REQUEST_ID + " : " + request.getAttribute(Constants.REQUEST_ID), ex);
        
        return new ResponseEntity<AbstractRestResource>(ex.getConflictResource(), ex.getStatus());
    }
    
    @ExceptionHandler(NestedServerRuntimeException.class)
    @ResponseBody
    ResponseEntity<?> handleControllerException(LoggingHttpServletRequestWrapper request,
            NestedServerRuntimeException ex) {

        logger.error(Constants.REQUEST_ID + " : " + request.getAttribute(Constants.REQUEST_ID), ex);

        return new ResponseEntity<ErrorView>(new ErrorView(ex.getMessage()), ex.getStatus());
    }

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    ResponseEntity<?> handleThrowable(LoggingHttpServletRequestWrapper request, Throwable ex) {

        logger.error(Constants.REQUEST_ID + " : " + request.getAttribute(Constants.REQUEST_ID), ex);

        return new ResponseEntity<ErrorView>(new ErrorView(ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    static class ErrorView {

        String errorCode;
        String message;
        String url;

        public ErrorView(String message) {
            super();
            this.message = message;
        }

        public ErrorView(String message, String url) {
            super();
            this.message = message;
            this.url = url;
        }

        public ErrorView(String message, String url, String errorCode) {
            super();
            this.message = message;
            this.url = url;
            this.errorCode = errorCode;
        }

        /**
         * @return the message
         */
        public String getMessage() {
            return message;
        }

        /**
         * @param message the message to set
         */
        public void setMessage(String message) {
            this.message = message;
        }

        /**
         * @return the url
         */
        public String getUrl() {
            return url;
        }

        /**
         * @param url the url to set
         */
        public void setUrl(String url) {
            this.url = url;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

    }

}
