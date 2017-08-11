package com.superluli.infra.jpa;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

public @interface LockAcquisitionRetry {

	/*
	 * For details about how these fields are used, look at :
	 * LockAcquisitionRetryAspect
	 */
	int times() default 3;

	int delayRange() default 10;
}
