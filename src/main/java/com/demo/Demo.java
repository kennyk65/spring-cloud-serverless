package com.demo;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.cloud.serverless.CloudFunction;
import org.springframework.cloud.serverless.Handler;

import com.amazonaws.services.lambda.runtime.Context;

@Handler
public class Demo {

	private AtomicInteger counter = new AtomicInteger(0);
	
	@CloudFunction
	public Object handle(Object o, Context context) {
		return "Invocation #" + counter.incrementAndGet();
	}
}
