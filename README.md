## Spring Cloud PATCH Bug Demo

This project is solely provided to demonstrate that the Spring Cloud RestTemplate does not currently support the PATCH method.

#### Steps to Reproduce:

1. Run the Cars Service:
	
	```
	cd car-service
	./gradlew bootRun
	```

	Leave this service running, as it's needed by the API Gateway below.
	
1. Demonstrate that the Car Service calls work:

	Create a new Car:
	
	```
	curl -H "Content-Type: application/json" -X POST -d '{"make":"Audi","model":"A4"}' http://localhost:8080/cars
	
	{
	  "make" : "Audi",
	  "model" : "A4",
	  "_links" : {
	    "self" : {
	      "href" : "http://localhost:8080/cars/74e62f88-bd65-496a-bd97-1f8b7685f619"
	    },
	    "car" : {
	      "href" : "http://localhost:8080/cars/74e62f88-bd65-496a-bd97-1f8b7685f619"
	    }
	  }
	}
	```
	
	Update that Car using the HATEOAS link in the previous output:
	
	```
	curl -H "Content-Type: application/json" -X PATCH -d '{"model":"S4"}' http://localhost:8080/cars/74e62f88-bd65-496a-bd97-1f8b7685f619

	{
	  "make" : "Audi",
	  "model" : "S4",
	  "_links" : {
	    "self" : {
	      "href" : "http://localhost:8080/cars/74e62f88-bd65-496a-bd97-1f8b7685f619"
	    },
	    "car" : {
	      "href" : "http://localhost:8080/cars/74e62f88-bd65-496a-bd97-1f8b7685f619"
	    }
	  }
	}	
	```
	
1. Run the API Gateway Service:
	
	```
	cd api-gateway
	./gradlew bootRun
	```
	
1. Demonstrate the Bug.

	Create a new Car through the API Gateway (port 9090) which is a POST and works:
	
	```
	curl -H "Content-Type: application/json" -X POST -d '{"make":"Audi","model":"A6"}' http://localhost:9090/cars

	http://localhost:8080/cars/eaec4b4f-c713-4f1d-b6e4-631793b9590d
	```
	
	Update that Car's model through the API Gateway (port 9090), which is a PATCH and won't work (this is the bug):
	
	```
	curl -H "Content-Type: application/json" -X PATCH http://localhost:9090/cars/eaec4b4f-c713-4f1d-b6e4-631793b9590d/S6
	
	{
		"timestamp":1489482314111,
		"status":500,
		"error":"Internal Server Error",
		"exception":"org.springframework.web.client.ResourceAccessException",
		"message":"I/O error on PATCH request for \"http://localhost:8080/cars/eaec4b4f-c713-4f1d-b6e4-631793b9590d\": Invalid HTTP method: PATCH; nested exception is java.net.ProtocolException: Invalid HTTP method: PATCH",
		"path":"/cars/eaec4b4f-c713-4f1d-b6e4-631793b9590d/S6"
	}
	```
	Note that I applied the formatting to the JSON so that it's easier to see.
	
	Observe the message field mention that the error is in making the call to the downstream Cars Service endpoint. This call works directly as demonstrated above, but does *not* work when going through the `RestTemplate`.
	
	Review the logs and you'll see similar to the followingw:
	
	```
	PATCHing to /cars/eaec4b4f-c713-4f1d-b6e4-631793b9590d with CarPatch{model=S6}

	2017-03-14 02:05:14.108 ERROR 8267 --- [nio-9090-exec-4] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is org.springframework.web.client.ResourceAccessException: I/O error on PATCH request for "http://localhost:8080/cars/eaec4b4f-c713-4f1d-b6e4-631793b9590d": Invalid HTTP method: PATCH; nested exception is java.net.ProtocolException: Invalid HTTP method: PATCH] with root cause
	
	java.net.ProtocolException: Invalid HTTP method: PATCH
		at java.net.HttpURLConnection.setRequestMethod(HttpURLConnection.java:440) ~[na:1.8.0_121]
		at sun.net.www.protocol.http.HttpURLConnection.setRequestMethod(HttpURLConnection.java:552) ~[na:1.8.0_121]
		at org.springframework.http.client.SimpleClientHttpRequestFactory.prepareConnection(SimpleClientHttpRequestFactory.java:218) ~[spring-web-4.3.7.RELEASE.jar:4.3.7.RELEASE]
		at org.springframework.http.client.SimpleClientHttpRequestFactory.createRequest(SimpleClientHttpRequestFactory.java:138) ~[spring-web-4.3.7.RELEASE.jar:4.3.7.RELEASE]
		at org.springframework.http.client.support.HttpAccessor.createRequest(HttpAccessor.java:85) ~[spring-web-4.3.7.RELEASE.jar:4.3.7.RELEASE]
		at org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:648) ~[spring-web-4.3.7.RELEASE.jar:4.3.7.RELEASE]
		at org.springframework.web.client.RestTemplate.execute(RestTemplate.java:628) ~[spring-web-4.3.7.RELEASE.jar:4.3.7.RELEASE]
		at org.springframework.web.client.RestTemplate.patchForObject(RestTemplate.java:477) ~[spring-web-4.3.7.RELEASE.jar:4.3.7.RELEASE]
		at com.example.CarController.updateCar(CarController.java:43) ~[main/:na]
		at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:1.8.0_121]
		at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[na:1.8.0_121]
		at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:1.8.0_121]
		at java.lang.reflect.Method.invoke(Method.java:498) ~[na:1.8.0_121]
		at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:205) ~[spring-web-4.3.7.RELEASE.jar:4.3.7.RELEASE]
		at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:133) ~[spring-web-4.3.7.RELEASE.jar:4.3.7.RELEASE]
		at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:116) ~[spring-webmvc-4.3.7.RELEASE.jar:4.3.7.RELEASE]
		at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:827) ~[spring-webmvc-4.3.7.RELEASE.jar:4.3.7.RELEASE]
		at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:738) ~[spring-webmvc-4.3.7.RELEASE.jar:4.3.7.RELEASE]
		at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:85) ~[spring-webmvc-4.3.7.RELEASE.jar:4.3.7.RELEASE]
		at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:963) ~[spring-webmvc-4.3.7.RELEASE.jar:4.3.7.RELEASE]
		at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:897) ~[spring-webmvc-4.3.7.RELEASE.jar:4.3.7.RELEASE]
		at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:970) ~[spring-webmvc-4.3.7.RELEASE.jar:4.3.7.RELEASE]
		at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:843) ~[spring-webmvc-4.3.7.RELEASE.jar:4.3.7.RELEASE]
		at javax.servlet.http.HttpServlet.service(HttpServlet.java:729) ~[tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:230) ~[tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:165) ~[tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:52) ~[tomcat-embed-websocket-8.5.11.jar:8.5.11]
		at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:192) ~[tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:165) ~[tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.springframework.boot.web.filter.ApplicationContextHeaderFilter.doFilterInternal(ApplicationContextHeaderFilter.java:55) ~[spring-boot-1.5.2.RELEASE.jar:1.5.2.RELEASE]
		at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:107) ~[spring-web-4.3.7.RELEASE.jar:4.3.7.RELEASE]
		at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:192) ~[tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:165) ~[tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.springframework.boot.actuate.trace.WebRequestTraceFilter.doFilterInternal(WebRequestTraceFilter.java:108) ~[spring-boot-actuator-1.5.2.RELEASE.jar:1.5.2.RELEASE]
		at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:107) ~[spring-web-4.3.7.RELEASE.jar:4.3.7.RELEASE]
		at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:192) ~[tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:165) ~[tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:99) ~[spring-web-4.3.7.RELEASE.jar:4.3.7.RELEASE]
		at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:107) ~[spring-web-4.3.7.RELEASE.jar:4.3.7.RELEASE]
		at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:192) ~[tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:165) ~[tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.springframework.web.filter.HttpPutFormContentFilter.doFilterInternal(HttpPutFormContentFilter.java:105) ~[spring-web-4.3.7.RELEASE.jar:4.3.7.RELEASE]
		at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:107) ~[spring-web-4.3.7.RELEASE.jar:4.3.7.RELEASE]
		at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:192) ~[tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:165) ~[tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.springframework.web.filter.HiddenHttpMethodFilter.doFilterInternal(HiddenHttpMethodFilter.java:81) ~[spring-web-4.3.7.RELEASE.jar:4.3.7.RELEASE]
		at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:107) ~[spring-web-4.3.7.RELEASE.jar:4.3.7.RELEASE]
		at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:192) ~[tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:165) ~[tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:197) ~[spring-web-4.3.7.RELEASE.jar:4.3.7.RELEASE]
		at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:107) ~[spring-web-4.3.7.RELEASE.jar:4.3.7.RELEASE]
		at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:192) ~[tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:165) ~[tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.springframework.boot.actuate.autoconfigure.MetricsFilter.doFilterInternal(MetricsFilter.java:106) ~[spring-boot-actuator-1.5.2.RELEASE.jar:1.5.2.RELEASE]
		at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:107) ~[spring-web-4.3.7.RELEASE.jar:4.3.7.RELEASE]
		at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:192) ~[tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:165) ~[tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:198) ~[tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:96) [tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:474) [tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:140) [tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:79) [tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:87) [tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:349) [tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:783) [tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:66) [tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:798) [tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1434) [tomcat-embed-core-8.5.11.jar:8.5.11]
		at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49) [tomcat-embed-core-8.5.11.jar:8.5.11]
		at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142) [na:1.8.0_121]
		at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617) [na:1.8.0_121]
		at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61) [tomcat-embed-core-8.5.11.jar:8.5.11]
		at java.lang.Thread.run(Thread.java:745) [na:1.8.0_121]
	```
	
#### Work Around: Use HttpClient instead of Spring's Impl.

Update `api-gateway/src/main/java/com/example/ApiGatewayApplication.java` to pass a new instance of `org.springframework.http.client.HttpComponentsClientHttpRequestFactory` into the `RestTemplate` constructor. You'll see that I have a line that does this commented out. 

You'll also need a dependency on `org.apache.httpcomponents:httpclient:4.4.1`, which is already included. 


Work around credit: [this StackOverflow Response](http://stackoverflow.com/questions/29447382/resttemplate-patch-request).