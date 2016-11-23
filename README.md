# spring-cloud-serverless
Provides first-class support for running Spring / Spring Boot applications as cloud functions inside “server less” architectures, such as:
* AWS Lambda
* Azure Cloud Functions
* Google Cloud Functions
* BlueMix OpenWhisk


## Appropriate platform
The reader may wonder if it is appropriate to run a (relatively) heavy Spring ApplicationContext within a cloud function - after all, Spring was definitely not designed with the server less revolution in mind.  Cloud functions are designed to be essentially stateless and event driven, and the Spring ApplicationContext is definitely stateful.

The original intention of cloud functions such as AWS Lambda is to be 1) stateless and 2) limited duration, as in ms to a few seconds.  As described by Martin Fowler:  
> In short you should assume that for any given invocation of a function none of the in-process or host state that you create will be available to any subsequent invocation. This includes state in RAM and state you may write to local disk. In other words from a deployment-unit point of view FaaS functions are stateless.

Spring applications often execute as a stateful (the ApplicationContext) long running process, thereby not matching this model.  However, to address practical realities (such as db connection pools, or the overhead associated with loading the functions themselves), cloud functions like AWS Lambda are often cached for an (indeterminate) amount of time to prevent loading the function repeatedly.  Therefore, these processes can have state, and can keep this state for minutes, hours, and days.  As a happy accident, lambdas only charge for the amount of time between the start and stop of a function, so the rest of the long running state is essentially free.

The function will stay loaded for a period of time determined by the cloud provider.  Once the application is loaded, subsequent executions won’t incur the startup penalty.  As long as the function is called on a regular basis the function will stay loaded.  Thus applications based on Spring Cloud Serverless are appropriate for cloud functions called heavily, or very rarely, but not in between.  The worst situation would be a function called on average once every hour or two.

Whether the overhead of loading the Spring ApplicationContext is justified is an architectural decision.  The purpose of this project is not to advocate the approach, but to accomodate it in circumstances where it makes sense.  

It is worth noting here that the actual runtime environment is subject to the cloud being used.  It is not a container environment.  This is also a rapidly evolving area of Cloud technology, so rapid changes should be anticipated.

See http://martinfowler.com/articles/serverless.html for more details on the Serverless Trend.

# Basic Usage

The first main difference between a Spring Cloud Serverless application and a standard Spring application of any kind is the entry point.  When a cloud function such as AWS Lambda is configured, it expects to know the Java class containing the function to be called.  A public static void main() method cannot be used because of the signature difference.  Even if it could, the goal is to avoid loading the ApplicationContext on each function invocation, so the ApplicationContext is loaded by a classic Singleton pattern.

Entry Point / Handler:  The entry point for the function is configured differently according to the cloud provider.  For AWS Lambda, the entry point is always *org.springframework.cloud.serverless.aws.ApplicationLambdaEntry*.

Main @Configuration Class:  Next, since the code that actually instantiates the ApplicationContext is outside of the developer's control, it is necessary to tell the framework the fully-qualified name of the @Configuration class to be used as the application's 'main' configuration class.  This is done by the SPRING_CONFIG_CLASS environment variable, or the spring.config.class system property, the latter takes precedent.  You MUST specify the name of your main configuration class using one of these variables.  

@Handler bean:  Next, one *and only one* of your beans should be designated with the @Handler stereotype annotation.  This indicates that this bean will contain the function to be called on each cloud function execution.  For cases where a single JAR may contain multiple @Handlers to be called in different cases, consider using Spring @Profile to selectively load only one.

@CloudFunction method:  Finally, use @CloudFunction to indicate the method to be called on the @Handler bean when the cloud function executes.  Your @Handler bean can have only one @CloudFunction method.

# @CloudFunction Method Parameters:
In the spirit of Spring MVC @RequestMapping method parameters, the signature to your @CloudFunction can vary quite a bit.  
A POJO would be instantiated and populated by the incoming JSON.
An AWS Lambda Context would be populated as-is.
A Logger would provide a Logging facade to be sent back to the underlying mechanism.

# AWS
To deploy a Spring Cloud Serverless application as an AWS Lambda function, first simply package your application as a single JAR file.  At the time of this writing, multiple JAR file applications are not supported in AWS Lambda, so Spring Boot's "uber" JARs are perfect for deployment.

Handler - Setup of an AWS Lambda function requires an indicated "Handler", that is, the class containing the method that will be called as the Lambda function.  Simply indicate *org.springframework.cloud.serverless.aws.ApplicationLambdaEntry* as the ‘handler’.  AWS Lambda will invoke the *handleRequest()* method on this class for EACH invocation.  It will take care of instantiating the Spring ApplicationContext on the first invocation.

SPRING_CONFIG_CLASS - Also required - set SPRING_CONFIG_CLASS environment variable to indicate the fully qualified classname of your main @Configuration class.  The Spring ApplictionContext will be automatically loaded once based on the classname you indicate.  There is no need - or practical accomodation - for you to instantiate the ApplicationContext within your own code.

Timeout - As noted earlier, the Lambda function will load the Spring ApplictionContext on its first invocation, therefore, the timeout setting must allow for the initial load of the Spring application context.  Even the smallest application can take several seconds.  Subsequent invocations within the cache period will not encounter this load penalty.

# Azure Cloud Functions

# Google Cloud Functions

# OpenWhisk

## FAQ:
Why is this project not called "Spring Cloud Lambda", or part of the "Spring Cloud AWS" project?
AWS Lambda is certainly the origin of the current trend toward serverless architectures.  However it has been quickly followed, and in some cases surpassed, by the capabilities offered by competitors.  This project aims to include more than just support for AWS Lambda.




