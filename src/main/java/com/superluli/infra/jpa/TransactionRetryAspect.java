package com.superluli.infra.jpa;

import java.util.Random;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.superluli.infra.exception.NestedServerRuntimeException;

@Aspect
@Component
public class TransactionRetryAspect {

	private static Logger logger = LoggerFactory.getLogger(TransactionRetryAspect.class);

	@Around(value = "@annotation(annotation)")
	public Object test(final ProceedingJoinPoint joinPoint, final LockAcquisitionRetry annotation) throws Throwable {

		Random rnd = new Random();
		int times = annotation.times();
		int delayRange = annotation.delayRange();
		
		int i = 0;

		while (i < times) {
			
			try {
				return joinPoint.proceed();
            } catch (CannotAcquireLockException e) {
            	i++;
            	long delay = i * rnd.nextInt(delayRange);
            	logger.error("dead lock detected, retry after " + delay + " millis");
    			Thread.sleep(delay);
            } catch (NestedServerRuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new NestedServerRuntimeException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "unexpecxted exception", e);
            }
		}

		throw new NestedServerRuntimeException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Cannot acquire lock after all retries");
	}
}
