package com.superluli.infra.jpa;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Aspect
@Component
public class RepositoryLoggingAspect {

	private static Logger logger = LoggerFactory.getLogger(RepositoryLoggingAspect.class);

	@Autowired
	private ObjectMapper mapper;

	private static final String ELAPSED_TIME = "elapsedTime";

	@Around("execution(public * org.springframework.data.repository.Repository+.*(..))")
	public Object repositoryOperations(final ProceedingJoinPoint jp) throws Throwable {

		long start = System.currentTimeMillis();
		try {

			Object retVal = jp.proceed();
			long end = System.currentTimeMillis();
			logReturned(jp, retVal, end - start);
			return retVal;
		}
		/*
		 * Catch all exceptions to prevent logging from impacting normal flow
		 */
		catch (Throwable e) {
			long end = System.currentTimeMillis();
			logThrown(jp, e, end - start);
			throw e;
		}
	}

	private void logReturned(JoinPoint jp, Object retVal, long elapsedTime) {

		ObjectNode node = getLogNode(jp);
		// this log is too much and not necessary
		// node.set("retVal", mapper.convertValue(retVal, JsonNode.class));
		node.set(ELAPSED_TIME, new LongNode(elapsedTime));
		logger.debug(node.toString());
	}

	private void logThrown(JoinPoint jp, Throwable ex, long elapsedTime) {

		ObjectNode node = getLogNode(jp);
		node.set("throwable", mapper.createObjectNode().put("message", ex.getMessage()));
		node.set(ELAPSED_TIME, new LongNode(elapsedTime));
		logger.error(node.toString());
	}

	private ObjectNode getLogNode(JoinPoint jp) {

		ObjectNode node = mapper.createObjectNode();
		node.put("signature", jp.getSignature().toString());

		Object[] args = jp.getArgs();
		JsonNode argsNode = mapper.convertValue(args, JsonNode.class);
		node.set("args", argsNode);

		return node;
	}
}
