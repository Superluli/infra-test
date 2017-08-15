package com.superluli.infra.exception;

import org.springframework.http.HttpStatus;

import com.superluli.infra.commons.resources.AbstractRestResource;


public class ConflictResourceException extends NestedServerRuntimeException {

	private static final long serialVersionUID = -7305617532393707947L;

	private AbstractRestResource conflictResource;

	public ConflictResourceException(String msg, AbstractRestResource conflictResource) {
		
		super(HttpStatus.CONFLICT, msg);
		this.conflictResource = conflictResource;
	}

	public AbstractRestResource getConflictResource() {
		return conflictResource;
	}

	public void setConflictResource(AbstractRestResource conflictResource) {
		this.conflictResource = conflictResource;
	}	
}
