package org.springframework.cloud.serverless;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies the method to be called on the @Handler bean.  
 * Only one method allowed per bean.
 * 
 * @author kenkrueger
 */
@Target(METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CloudFunction {

}
