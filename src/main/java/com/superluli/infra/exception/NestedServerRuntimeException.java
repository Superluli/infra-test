package com.superluli.infra.exception;

import org.springframework.core.NestedRuntimeException;
import org.springframework.http.HttpStatus;

public class NestedServerRuntimeException extends NestedRuntimeException {

    private static final long serialVersionUID = -2619344865165584293L;

    private HttpStatus status;

    public NestedServerRuntimeException(HttpStatus status, String msg) {
        super(msg);
        this.status = status;
    }


    public NestedServerRuntimeException(HttpStatus status, String msg, Throwable cause) {
        super(msg, cause);
        this.status = status;
    }


    /**
     * @return the status
     */
    public HttpStatus getStatus() {
        return status;
    }


    /**
     * @param status the status to set
     */
    public void setStatus(HttpStatus status) {
        this.status = status;
    }

}
