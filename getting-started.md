# Lush Service Architecture - Getting Started

## Overview
This getting started guide will take you through the steps required to create a service based on Lush.  Once we create a service based on the Lush archetype, we will investigate each Lush concept and you can make use of it in your applicaiton.

### Create Your First Service
This section will show you how to use the Maven archetype to create your first Lush service.  We will assume a command line in this document, but feel free to use your favorite IDE if you are so inclined.

#### Generate from the Lush Service Archetype
```shell
echo Create a Lush service using Maven archetype
mvn archetype:generate -DarchetypeGroupId=com.px3j -DarchetypeArtifactId=lush-service -DarchetypeVersion=2022.7.0 -DgroupId=com.poc -DartifactId=my-lush-service
mvn ..
```

If all goes well, you should see a new service created, built and test case output.  I'm sure the output looks quite messy, but in the next sections we will dissect it and at the same time introduce the core concepts of Lush.

### Let's Dissect
First a short definition of what Lush considers an entrypoint or endpoint.  This is the point in the service that is exposed to consumers.  The reason this is important is that this is where Lush does much of its magic.  It'll make more sense as we move along.  For purposes of this document we will focus on HTTP endpoints.

#### The Controller
First, the simplest case - a simple HTTP endpoint that takes no parameters and returns a String.  If we look at the ExampleController we will find such a method:

##### Test Ping
```java
    @LushControllerMethod
    @RequestMapping("ping")
    @PreAuthorize("isAuthenticated()")
    public Mono<String> ping() {
        log.info( "ping() has been called" );
        return Mono.just( "Powered By Lush" );
    }
```


Below is  the output
```text
2022-07-21 12:57:58.467  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : START: testPing
2022-07-21 12:57:58.471  INFO [lush-app-test,11240e415483ed5b,11240e415483ed5b] 5588 --- [     parallel-6] [paul] c.p.l.e.s.controller.ExampleController   : ping() has been called
2022-07-21 12:57:58.477  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : Ping results: Powered By Lush
2022-07-21 12:57:58.477  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : END: testPing
```




##### Test FluxOfInts
```java
    /**
     * This endpoint illustrates how you can use a Flux to return a collection of data back to the caller.
     *
     * @return A Flux that publishes a list of integers.
     */
    @LushControllerMethod
    @RequestMapping("fluxOfInts")
    @PreAuthorize("isAuthenticated()")
    public Flux<Integer> fluxOfInts() {
        return Flux.fromIterable(List.of(1,2,3,4,5,6,7,8,9,10));
    }
```

This is the simplest usage of Lush, you'll notice that it is not much different from what you would normally do with Spring.  We simply have added the @LushControllerMethod annotation.  

Output:
```text
2022-07-21 12:57:58.467  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : START: testPing
2022-07-21 12:57:58.471  INFO [lush-app-test,11240e415483ed5b,11240e415483ed5b] 5588 --- [     parallel-6] [paul] c.p.l.e.s.controller.ExampleController   : ping() has been called
2022-07-21 12:57:58.477  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : Ping results: Powered By Lush
2022-07-21 12:57:58.477  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : END: testPing
```

##### Test FluxOfIntsWithAdvice

```java
    /**
     * This endpoint illustrates how you can use a Flux to return a collection of data back to the caller.
     *
     * @return A Flux that publishes a list of integers.
     */
    @LushControllerMethod
    @RequestMapping("fluxOfIntsWithAdvice")
    @PreAuthorize("isAuthenticated()")
    public Flux<Integer> fluxOfIntsWithAdvice( LushTicket ticket, LushContext lushContext ) {
        //
        // Advice automatically injected by Lush - you can modify it, it will be returned to the caller
        //
        LushAdvice advice = lushContext.getAdvice();

        // You can set a status code for this request - this is different than the HTTP status code, with Lush
        // all requests will return a 200 status code, you use LushAdvice to specify the application level
        // status code,
        advice.setStatusCode( 0 );

        // Advice also lets you set 'extras', this is any number of key/value pairs that is usable to your
        // callers.
        advice.putExtra( "helloMessage", String.format("hello: %s", ticket.getUsername()));
        advice.putExtra( "hasMoreData", false );

        // Warnings are a special category of return type.  You can use these to signify specific things that
        // your caller may need to respond to.  Each LusWarning can have a status code and a set of key/value pairs
        // representing details of the warning.
        advice.addWarning( new LushAdvice.LushWarning(600, Map.of("delayedData", true)));

        // And return data...
        return Flux.fromIterable(List.of(1,2,3,4,5,6,7,8,9,10));
    }
```

output 
```text
2022-07-21 12:57:58.509  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : ** START: Lush LushAdvice **
2022-07-21 12:57:58.509  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : **        Status Code: 0
2022-07-21 12:57:58.509  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : **        Trace Id:    9582bdc4b6e5fb58,9582bdc4b6e5fb58
2022-07-21 12:57:58.509  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : **        Extras:
2022-07-21 12:57:58.509  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : **           extra - key: helloMessage value: hello: tester:
2022-07-21 12:57:58.509  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : **           extra - key: hasMoreData value: false:
2022-07-21 12:57:58.509  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : **        Warnings:
2022-07-21 12:57:58.509  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : **          Code: 600
2022-07-21 12:57:58.509  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : **          Details: 
2022-07-21 12:57:58.509  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : **             warning - key: delayedData details: true:
2022-07-21 12:57:58.510  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : **          
2022-07-21 12:57:58.510  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : ** END:   Lush LushAdvice **
2022-07-21 12:57:58.510  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : 1
2022-07-21 12:57:58.510  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : 2
2022-07-21 12:57:58.510  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : 3
2022-07-21 12:57:58.510  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : 4
2022-07-21 12:57:58.510  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : 5
2022-07-21 12:57:58.510  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : 6
2022-07-21 12:57:58.510  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : 7
2022-07-21 12:57:58.510  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : 8
2022-07-21 12:57:58.510  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : 9
2022-07-21 12:57:58.510  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : 10
```

##### Test UnexpectedException

```java
    /**
     * This endpoint illustrates how Lush will wrap controller methods and provide consistent exception handling
     * and logging.
     *
     * @return A String containing a message to the caller.
     */
    @LushControllerMethod
    @RequestMapping("uae")
    @PreAuthorize("isAuthenticated()")
    public Mono<String> uae( LushTicket ticket) {
        // Illustration calling a method that may throw an exception, developers don't need to concern themselves with
        // these as Lush will handle it appropriately.
        methodThatThrowsUnexpectedException();

        return Mono.just( String.format( "%s says hi!", ticket.getUsername()) );
    }
```

```text
2022-07-21 12:57:58.493  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : START: testUnexpectedException
2022-07-21 12:57:58.496 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    com.px3j.lush.core.exception.LushException: Illustrate Exception Handling
2022-07-21 12:57:58.496 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at ExampleController.methodThatThrowsUnexpectedException(ExampleController.java:164)
2022-07-21 12:57:58.497 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at ExampleController.uae(ExampleController.java:136)
2022-07-21 12:57:58.497 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at ExampleController$$FastClassBySpringCGLIB$$9cd58d1f.invoke(<generated>)
2022-07-21 12:57:58.497 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at org.springframework.cglib.proxy.MethodProxy.invoke(MethodProxy.java:218)
2022-07-21 12:57:58.497 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.invokeJoinpoint(CglibAopProxy.java:793)
2022-07-21 12:57:58.497 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :           ** skipped **
2022-07-21 12:57:58.497 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at com.px3j.lush.endpoint.http.reactive.ControllerDecorator.decoratorImpl(ControllerDecorator.java:102)
2022-07-21 12:57:58.497 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at com.px3j.lush.endpoint.http.reactive.ControllerDecorator.lambda$monoInvocationAdvice$2(ControllerDecorator.java:58)
2022-07-21 12:57:58.497 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.MonoFlatMap$FlatMapMain.onNext(MonoFlatMap.java:125)
2022-07-21 12:57:58.497 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onNext(FluxMapFuseable.java:129)
2022-07-21 12:57:58.497 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :           ** skipped **
2022-07-21 12:57:58.497 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at org.springframework.cloud.sleuth.instrument.reactor.ScopePassingSpanSubscriber.onSubscribe(ScopePassingSpanSubscriber.java:68)
2022-07-21 12:57:58.497 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.MonoCurrentContext.subscribe(MonoCurrentContext.java:36)
2022-07-21 12:57:58.497 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :           ** skipped **
2022-07-21 12:57:58.497 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.Operators$ScalarSubscription.request(Operators.java:2398)
2022-07-21 12:57:58.497 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at org.springframework.cloud.sleuth.instrument.reactor.ScopePassingSpanSubscriber.request(ScopePassingSpanSubscriber.java:75)
2022-07-21 12:57:58.497 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :           ** skipped **
2022-07-21 12:57:58.497 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.FluxPeek$PeekSubscriber.onNext(FluxPeek.java:200)
2022-07-21 12:57:58.497 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.FluxPeek$PeekSubscriber.onNext(FluxPeek.java:200)
2022-07-21 12:57:58.497 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :           ** skipped **
2022-07-21 12:57:58.497 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.MonoPeekTerminal$MonoTerminalPeekSubscriber.onNext(MonoPeekTerminal.java:180)
2022-07-21 12:57:58.498 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.FluxDefaultIfEmpty$DefaultIfEmptySubscriber.onNext(FluxDefaultIfEmpty.java:101)
2022-07-21 12:57:58.498 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :           ** skipped **
2022-07-21 12:57:58.498 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.MonoPeekTerminal$MonoTerminalPeekSubscriber.onComplete(MonoPeekTerminal.java:299)
2022-07-21 12:57:58.498 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.MonoIgnoreElements$IgnoreElementsSubscriber.onComplete(MonoIgnoreElements.java:89)
2022-07-21 12:57:58.498 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :           ** skipped **
2022-07-21 12:57:58.498 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.MonoZip.subscribe(MonoZip.java:128)
2022-07-21 12:57:58.498 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.Mono.subscribe(Mono.java:4400)
2022-07-21 12:57:58.498 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :           ** skipped **
2022-07-21 12:57:58.498 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.MonoIgnoreThen$ThenIgnoreMain.onComplete(MonoIgnoreThen.java:203)
2022-07-21 12:57:58.498 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at org.springframework.security.test.context.support.ReactorContextTestExecutionListener$DelegateTestExecutionListener$SecuritySubContext.onComplete(ReactorContextTestExecutionListener.java:130)
2022-07-21 12:57:58.498 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :           ** skipped **
2022-07-21 12:57:58.498 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.MonoNext$NextSubscriber.onNext(MonoNext.java:82)
2022-07-21 12:57:58.498 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.FluxConcatMap$ConcatMapImmediate.innerNext(FluxConcatMap.java:282)
2022-07-21 12:57:58.498 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :           ** skipped **
2022-07-21 12:57:58.498 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.Operators$MultiSubscriptionSubscriber.onSubscribe(Operators.java:2068)
2022-07-21 12:57:58.498 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at org.springframework.security.test.context.support.ReactorContextTestExecutionListener$DelegateTestExecutionListener$SecuritySubContext.onSubscribe(ReactorContextTestExecutionListener.java:115)
2022-07-21 12:57:58.498 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :           ** skipped **
2022-07-21 12:57:58.498 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.MonoDefer.subscribe(MonoDefer.java:52)
2022-07-21 12:57:58.498 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.InternalMonoOperator.subscribe(InternalMonoOperator.java:64)
2022-07-21 12:57:58.498 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :           ** skipped **
2022-07-21 12:57:58.498 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.MonoNext$NextSubscriber.onNext(MonoNext.java:82)
2022-07-21 12:57:58.498 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.FluxConcatMap$ConcatMapImmediate.innerNext(FluxConcatMap.java:282)
2022-07-21 12:57:58.499 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :           ** skipped **
2022-07-21 12:57:58.499 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.Operators$MonoSubscriber.complete(Operators.java:1816)
2022-07-21 12:57:58.499 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.MonoFlatMap$FlatMapMain.onNext(MonoFlatMap.java:151)
2022-07-21 12:57:58.499 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :           ** skipped **
2022-07-21 12:57:58.499 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.MonoFlatMap$FlatMapMain.onNext(MonoFlatMap.java:157)
2022-07-21 12:57:58.499 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.FluxMap$MapSubscriber.onNext(FluxMap.java:122)
2022-07-21 12:57:58.499 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :           ** skipped **
2022-07-21 12:57:58.499 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.FluxPeekFuseable$PeekFuseableSubscriber.request(FluxPeekFuseable.java:144)
2022-07-21 12:57:58.499 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.FluxFlatMap$FlatMapMain.onSubscribe(FluxFlatMap.java:371)
2022-07-21 12:57:58.499 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :           ** skipped **
2022-07-21 12:57:58.499 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.InternalMonoOperator.subscribe(InternalMonoOperator.java:64)
2022-07-21 12:57:58.499 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.MonoDefer.subscribe(MonoDefer.java:52)
2022-07-21 12:57:58.499 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :           ** skipped **
2022-07-21 12:57:58.499 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.Operators$MultiSubscriptionSubscriber.set(Operators.java:2194)
2022-07-21 12:57:58.499 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.Operators$MultiSubscriptionSubscriber.onSubscribe(Operators.java:2068)
2022-07-21 12:57:58.499 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :           ** skipped **
2022-07-21 12:57:58.499 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.FluxPeekFuseable$PeekFuseableSubscriber.onComplete(FluxPeekFuseable.java:277)
2022-07-21 12:57:58.499 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.FluxIterable$IterableSubscription.slowPath(FluxIterable.java:294)
2022-07-21 12:57:58.499 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :           ** skipped **
2022-07-21 12:57:58.499 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.FluxDefaultIfEmpty$DefaultIfEmptySubscriber.onComplete(FluxDefaultIfEmpty.java:109)
2022-07-21 12:57:58.499 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.FluxMap$MapSubscriber.onComplete(FluxMap.java:144)
2022-07-21 12:57:58.500 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :           ** skipped **
2022-07-21 12:57:58.500 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at reactor.core.publisher.MonoPeekTerminal$MonoTerminalPeekSubscriber.onNext(MonoPeekTerminal.java:180)
2022-07-21 12:57:58.500 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at org.springframework.cloud.sleuth.instrument.reactor.ScopePassingSpanSubscriber.onNext(ScopePassingSpanSubscriber.java:89)
2022-07-21 12:57:58.500 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :           ** skipped **
2022-07-21 12:57:58.500 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
2022-07-21 12:57:58.500 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:304)
2022-07-21 12:57:58.500 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
2022-07-21 12:57:58.500 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
2022-07-21 12:57:58.500 ERROR [lush-app-test,05a48d0992877809,05a48d0992877809] 5588 --- [     parallel-2] [paul] lush.core.debug                          :    	at java.base/java.lang.Thread.run(Thread.java:833)
2022-07-21 12:57:58.501  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : Lush LushAdvice: LushAdvice(traceId=05a48d0992877809,05a48d0992877809, statusCode=-999, warnings=[], extras={lush.isUnexpectedException=true})
2022-07-21 12:57:58.501  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : END: testUnexpectedException
```

##### Test UnexpectedExceptionNoLush
```java
    /**
     * This endpoint illustrates an unexpected exception that isn't wrapped by Lush. (notice, no @LushControllerMethod
     * annotation)
     *
     * @return A String containing a message to the caller.
     */
    @RequestMapping("uaeNoLush")
    @PreAuthorize("isAuthenticated()")
    public Mono<String> uaeNoLush( LushTicket ticket) {
        // Illustration calling a method that may throw an exception, developers don't need to concern themselves with
        // these as Lush will handle it appropriately.
        methodThatThrowsUnexpectedException();

        return Mono.just( String.format( "%s says hi!", ticket.getUsername()) );
    }
```

```text
2022-07-21 12:57:58.415  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : START: testUnexpectedExceptionNoLush
2022-07-21 12:57:58.416  INFO [lush-app-test,1a3fddc60b7cd74c,67a200b68ef0a7c8] 5588 --- [ntContainer#0-1] [core] c.p.l.e.s.endpoint.jms.ExampleReceiver   : LushAdvice: LushAdvice(traceId=1a3fddc60b7cd74c, statusCode=0, warnings=[], extras={})
2022-07-21 12:57:58.418  INFO [lush-app-test,1a3fddc60b7cd74c,62106ad43b37cac6] 5588 --- [ntContainer#0-1] [core] c.p.l.e.s.endpoint.jms.ExampleReceiver   : ticket: paul
2022-07-21 12:57:58.418  INFO [lush-app-test,1a3fddc60b7cd74c,62106ad43b37cac6] 5588 --- [ntContainer#0-1] [core] c.p.l.e.s.endpoint.jms.ExampleReceiver   : LushAdvice: LushAdvice(traceId=1a3fddc60b7cd74c, statusCode=0, warnings=[], extras={})
2022-07-21 12:57:58.445 ERROR [lush-app-test,9cee80a028c511d4,9cee80a028c511d4] 5588 --- [     parallel-4] [core] a.w.r.e.AbstractErrorWebExceptionHandler : [e11e809]  500 Server Error for HTTP GET "/lush/example/uaeNoLush"

com.px3j.lush.core.exception.LushException: Illustrate Exception Handling
	at ExampleController.methodThatThrowsUnexpectedException(ExampleController.java:164)
	Suppressed: reactor.core.publisher.FluxOnAssembly$OnAssemblyException: 
Error has been observed at the following site(s):
	*__checkpoint ⇢ Handler ExampleController#uaeNoLush(LushTicket) [DispatcherHandler]
	*__checkpoint ⇢ com.px3j.lush.endpoint.http.reactive.EndpointFilter [DefaultWebFilterChain]
	*__checkpoint ⇢ org.springframework.security.web.server.authorization.AuthorizationWebFilter [DefaultWebFilterChain]
	*__checkpoint ⇢ org.springframework.security.web.server.authorization.ExceptionTranslationWebFilter [DefaultWebFilterChain]
	*__checkpoint ⇢ org.springframework.security.web.server.authentication.logout.LogoutWebFilter [DefaultWebFilterChain]
	*__checkpoint ⇢ org.springframework.security.web.server.savedrequest.ServerRequestCacheWebFilter [DefaultWebFilterChain]
	*__checkpoint ⇢ org.springframework.security.web.server.context.SecurityContextServerWebExchangeWebFilter [DefaultWebFilterChain]
	*__checkpoint ⇢ org.springframework.security.web.server.context.ReactorContextWebFilter [DefaultWebFilterChain]
	*__checkpoint ⇢ org.springframework.security.web.server.header.HttpHeaderWriterWebFilter [DefaultWebFilterChain]
	*__checkpoint ⇢ org.springframework.security.config.web.server.ServerHttpSecurity$ServerWebExchangeReactorContextWebFilter [DefaultWebFilterChain]
	*__checkpoint ⇢ org.springframework.security.web.server.WebFilterChainProxy [DefaultWebFilterChain]
	*__checkpoint ⇢ org.springframework.cloud.sleuth.instrument.web.TraceWebFilter [DefaultWebFilterChain]
	*__checkpoint ⇢ org.springframework.boot.actuate.metrics.web.reactive.server.MetricsWebFilter [DefaultWebFilterChain]
	*__checkpoint ⇢ HTTP GET "/lush/example/uaeNoLush" [ExceptionHandlingWebHandler]
Original Stack Trace:
		at ExampleController.methodThatThrowsUnexpectedException(ExampleController.java:164)
		at ExampleController.uaeNoLush(ExampleController.java:119)
		at ExampleController$$FastClassBySpringCGLIB$$9cd58d1f.invoke(<generated>)
		at org.springframework.cglib.proxy.MethodProxy.invoke(MethodProxy.java:218)
		at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.invokeJoinpoint(CglibAopProxy.java:793)
		at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163)
		at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:763)
		at org.springframework.security.access.prepost.PrePostAdviceReactiveMethodInterceptor.proceed(PrePostAdviceReactiveMethodInterceptor.java:156)
		at org.springframework.security.access.prepost.PrePostAdviceReactiveMethodInterceptor.lambda$invoke$4(PrePostAdviceReactiveMethodInterceptor.java:116)
		at reactor.core.publisher.MonoFlatMap$FlatMapMain.onNext(MonoFlatMap.java:125)
		at reactor.core.publisher.FluxSwitchIfEmpty$SwitchIfEmptySubscriber.onNext(FluxSwitchIfEmpty.java:74)
		at reactor.core.publisher.FluxFilter$FilterSubscriber.onNext(FluxFilter.java:113)
		at reactor.core.publisher.FluxDefaultIfEmpty$DefaultIfEmptySubscriber.onNext(FluxDefaultIfEmpty.java:101)
		at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onNext(FluxMapFuseable.java:129)
		at reactor.core.publisher.Operators$MonoSubscriber.complete(Operators.java:1816)
		at reactor.core.publisher.MonoFlatMap$FlatMapMain.onNext(MonoFlatMap.java:151)
		at reactor.core.publisher.FluxFilterFuseable$FilterFuseableSubscriber.onNext(FluxFilterFuseable.java:118)
		at org.springframework.cloud.sleuth.instrument.reactor.ScopePassingSpanSubscriber.onNext(ScopePassingSpanSubscriber.java:89)
		at reactor.core.publisher.Operators$ScalarSubscription.request(Operators.java:2398)
		at org.springframework.cloud.sleuth.instrument.reactor.ScopePassingSpanSubscriber.request(ScopePassingSpanSubscriber.java:75)
		at reactor.core.publisher.FluxFilterFuseable$FilterFuseableSubscriber.request(FluxFilterFuseable.java:191)
		at reactor.core.publisher.MonoFlatMap$FlatMapMain.onSubscribe(MonoFlatMap.java:110)
		at reactor.core.publisher.FluxFilterFuseable$FilterFuseableSubscriber.onSubscribe(FluxFilterFuseable.java:87)
		at org.springframework.cloud.sleuth.instrument.reactor.ScopePassingSpanSubscriber.onSubscribe(ScopePassingSpanSubscriber.java:68)
		at reactor.core.publisher.MonoCurrentContext.subscribe(MonoCurrentContext.java:36)
		at reactor.core.publisher.InternalMonoOperator.subscribe(InternalMonoOperator.java:64)
		at reactor.core.publisher.MonoFlatMap$FlatMapMain.onNext(MonoFlatMap.java:157)
		at reactor.core.publisher.Operators$MonoSubscriber.complete(Operators.java:1816)
		at reactor.core.publisher.MonoFlatMap$FlatMapInner.onNext(MonoFlatMap.java:249)
		at reactor.core.publisher.FluxOnErrorResume$ResumeSubscriber.onNext(FluxOnErrorResume.java:79)
		at reactor.core.publisher.FluxPeek$PeekSubscriber.onNext(FluxPeek.java:200)
		at reactor.core.publisher.FluxPeek$PeekSubscriber.onNext(FluxPeek.java:200)
		at org.springframework.cloud.sleuth.instrument.reactor.ScopePassingSpanSubscriber.onNext(ScopePassingSpanSubscriber.java:89)
		at reactor.core.publisher.MonoIgnoreThen$ThenIgnoreMain.complete(MonoIgnoreThen.java:292)
		at reactor.core.publisher.MonoIgnoreThen$ThenIgnoreMain.onNext(MonoIgnoreThen.java:187)
		at reactor.core.publisher.Operators$MonoSubscriber.complete(Operators.java:1816)
		at reactor.core.publisher.MonoFlatMap$FlatMapMain.onNext(MonoFlatMap.java:151)
		at reactor.core.publisher.Operators$MonoSubscriber.complete(Operators.java:1816)
		at reactor.core.publisher.MonoZip$ZipCoordinator.signal(MonoZip.java:251)
		at reactor.core.publisher.MonoZip$ZipInner.onNext(MonoZip.java:336)
		at org.springframework.security.test.context.support.ReactorContextTestExecutionListener$DelegateTestExecutionListener$SecuritySubContext.onNext(ReactorContextTestExecutionListener.java:120)
		at org.springframework.cloud.sleuth.instrument.reactor.ScopePassingSpanSubscriber.onNext(ScopePassingSpanSubscriber.java:89)
		at reactor.core.publisher.MonoPeekTerminal$MonoTerminalPeekSubscriber.onNext(MonoPeekTerminal.java:180)
		at reactor.core.publisher.FluxDefaultIfEmpty$DefaultIfEmptySubscriber.onNext(FluxDefaultIfEmpty.java:101)
		at reactor.core.publisher.FluxHide$SuppressFuseableSubscriber.onNext(FluxHide.java:137)
		at org.springframework.security.test.context.support.ReactorContextTestExecutionListener$DelegateTestExecutionListener$SecuritySubContext.onNext(ReactorContextTestExecutionListener.java:120)
		at org.springframework.cloud.sleuth.instrument.reactor.ScopePassingSpanSubscriber.onNext(ScopePassingSpanSubscriber.java:89)
		at reactor.core.publisher.MonoIgnoreThen$ThenIgnoreMain.complete(MonoIgnoreThen.java:292)
		at reactor.core.publisher.MonoIgnoreThen$ThenIgnoreMain.onNext(MonoIgnoreThen.java:187)
		at reactor.core.publisher.MonoIgnoreThen$ThenIgnoreMain.subscribeNext(MonoIgnoreThen.java:236)
		at reactor.core.publisher.MonoIgnoreThen$ThenIgnoreMain.onComplete(MonoIgnoreThen.java:203)
		at org.springframework.security.test.context.support.ReactorContextTestExecutionListener$DelegateTestExecutionListener$SecuritySubContext.onComplete(ReactorContextTestExecutionListener.java:130)
		at org.springframework.cloud.sleuth.instrument.reactor.ScopePassingSpanSubscriber.onComplete(ScopePassingSpanSubscriber.java:103)
		at reactor.core.publisher.MonoPeekTerminal$MonoTerminalPeekSubscriber.onComplete(MonoPeekTerminal.java:299)
		at reactor.core.publisher.MonoPeekTerminal$MonoTerminalPeekSubscriber.onComplete(MonoPeekTerminal.java:299)
		at reactor.core.publisher.MonoIgnoreElements$IgnoreElementsSubscriber.onComplete(MonoIgnoreElements.java:89)
		at reactor.core.publisher.FluxPeek$PeekSubscriber.onComplete(FluxPeek.java:260)
		at reactor.core.publisher.FluxMap$MapSubscriber.onComplete(FluxMap.java:144)
		at reactor.core.publisher.Operators$MonoSubscriber.complete(Operators.java:1817)
		at reactor.core.publisher.MonoZip$ZipCoordinator.signal(MonoZip.java:251)
		at reactor.core.publisher.MonoZip$ZipInner.onNext(MonoZip.java:336)
		at org.springframework.security.test.context.support.ReactorContextTestExecutionListener$DelegateTestExecutionListener$SecuritySubContext.onNext(ReactorContextTestExecutionListener.java:120)
		at org.springframework.cloud.sleuth.instrument.reactor.ScopePassingSpanSubscriber.onNext(ScopePassingSpanSubscriber.java:89)
		at reactor.core.publisher.Operators$MonoSubscriber.complete(Operators.java:1816)
		at reactor.core.publisher.MonoCacheTime$CoordinatorSubscriber.signalCached(MonoCacheTime.java:337)
		at reactor.core.publisher.MonoCacheTime$CoordinatorSubscriber.onNext(MonoCacheTime.java:354)
		at reactor.core.publisher.Operators$ScalarSubscription.request(Operators.java:2398)
		at reactor.core.publisher.MonoCacheTime$CoordinatorSubscriber.onSubscribe(MonoCacheTime.java:293)
		at reactor.core.publisher.MonoJust.subscribe(MonoJust.java:55)
		at reactor.core.publisher.MonoCacheTime.subscribeOrReturn(MonoCacheTime.java:143)
		at reactor.core.publisher.Mono.subscribe(Mono.java:4385)
		at reactor.core.publisher.MonoZip.subscribe(MonoZip.java:128)
		at reactor.core.publisher.Mono.subscribe(Mono.java:4400)
		at reactor.core.publisher.MonoIgnoreThen$ThenIgnoreMain.subscribeNext(MonoIgnoreThen.java:263)
		at reactor.core.publisher.MonoIgnoreThen.subscribe(MonoIgnoreThen.java:51)
		at reactor.core.publisher.Mono.subscribe(Mono.java:4400)
		at reactor.core.publisher.FluxFlatMap.trySubscribeScalarMap(FluxFlatMap.java:203)
		at reactor.core.publisher.MonoFlatMap.subscribeOrReturn(MonoFlatMap.java:53)
		at reactor.core.publisher.Mono.subscribe(Mono.java:4385)
		at reactor.core.publisher.MonoZip.subscribe(MonoZip.java:128)
		at reactor.core.publisher.InternalMonoOperator.subscribe(InternalMonoOperator.java:64)
		at reactor.core.publisher.MonoDefer.subscribe(MonoDefer.java:52)
		at reactor.core.publisher.MonoIgnoreThen$ThenIgnoreMain.subscribeNext(MonoIgnoreThen.java:240)
		at reactor.core.publisher.MonoIgnoreThen$ThenIgnoreMain.onComplete(MonoIgnoreThen.java:203)
		at org.springframework.security.test.context.support.ReactorContextTestExecutionListener$DelegateTestExecutionListener$SecuritySubContext.onComplete(ReactorContextTestExecutionListener.java:130)
		at org.springframework.cloud.sleuth.instrument.reactor.ScopePassingSpanSubscriber.onComplete(ScopePassingSpanSubscriber.java:103)
		at reactor.core.publisher.MonoFlatMap$FlatMapMain.onComplete(MonoFlatMap.java:181)
		at reactor.core.publisher.Operators.complete(Operators.java:137)
		at reactor.core.publisher.MonoZip.subscribe(MonoZip.java:120)
		at reactor.core.publisher.Mono.subscribe(Mono.java:4400)
		at reactor.core.publisher.MonoIgnoreThen$ThenIgnoreMain.subscribeNext(MonoIgnoreThen.java:263)
		at reactor.core.publisher.MonoIgnoreThen.subscribe(MonoIgnoreThen.java:51)
		at reactor.core.publisher.InternalMonoOperator.subscribe(InternalMonoOperator.java:64)
		at reactor.core.publisher.MonoFlatMap$FlatMapMain.onNext(MonoFlatMap.java:157)
		at reactor.core.publisher.FluxSwitchIfEmpty$SwitchIfEmptySubscriber.onNext(FluxSwitchIfEmpty.java:74)
		at reactor.core.publisher.MonoNext$NextSubscriber.onNext(MonoNext.java:82)
		at reactor.core.publisher.FluxConcatMap$ConcatMapImmediate.innerNext(FluxConcatMap.java:282)
		at reactor.core.publisher.FluxConcatMap$ConcatMapInner.onNext(FluxConcatMap.java:863)
		at org.springframework.security.test.context.support.ReactorContextTestExecutionListener$DelegateTestExecutionListener$SecuritySubContext.onNext(ReactorContextTestExecutionListener.java:120)
		at org.springframework.cloud.sleuth.instrument.reactor.ScopePassingSpanSubscriber.onNext(ScopePassingSpanSubscriber.java:89)
		at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onNext(FluxMapFuseable.java:129)
		at reactor.core.publisher.MonoPeekTerminal$MonoTerminalPeekSubscriber.onNext(MonoPeekTerminal.java:180)
		at reactor.core.publisher.Operators$ScalarSubscription.request(Operators.java:2398)
		at reactor.core.publisher.MonoPeekTerminal$MonoTerminalPeekSubscriber.request(MonoPeekTerminal.java:139)
		at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.request(FluxMapFuseable.java:171)
		at org.springframework.cloud.sleuth.instrument.reactor.ScopePassingSpanSubscriber.request(ScopePassingSpanSubscriber.java:75)
		at reactor.core.publisher.Operators$MultiSubscriptionSubscriber.set(Operators.java:2194)
		at reactor.core.publisher.Operators$MultiSubscriptionSubscriber.onSubscribe(Operators.java:2068)
		at org.springframework.security.test.context.support.ReactorContextTestExecutionListener$DelegateTestExecutionListener$SecuritySubContext.onSubscribe(ReactorContextTestExecutionListener.java:115)
		at org.springframework.cloud.sleuth.instrument.reactor.ScopePassingSpanSubscriber.onSubscribe(ScopePassingSpanSubscriber.java:68)
		at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onSubscribe(FluxMapFuseable.java:96)
		at reactor.core.publisher.MonoPeekTerminal$MonoTerminalPeekSubscriber.onSubscribe(MonoPeekTerminal.java:152)
		at reactor.core.publisher.MonoJust.subscribe(MonoJust.java:55)
		at reactor.core.publisher.Mono.subscribe(Mono.java:4400)
		at reactor.core.publisher.FluxConcatMap$ConcatMapImmediate.drain(FluxConcatMap.java:451)
		at reactor.core.publisher.FluxConcatMap$ConcatMapImmediate.onSubscribe(FluxConcatMap.java:219)
		at reactor.core.publisher.FluxIterable.subscribe(FluxIterable.java:165)
		at reactor.core.publisher.FluxIterable.subscribe(FluxIterable.java:87)
		at reactor.core.publisher.InternalMonoOperator.subscribe(InternalMonoOperator.java:64)
		at reactor.core.publisher.MonoDefer.subscribe(MonoDefer.java:52)
		at reactor.core.publisher.InternalMonoOperator.subscribe(InternalMonoOperator.java:64)
		at reactor.core.publisher.MonoDefer.subscribe(MonoDefer.java:52)
		at reactor.core.publisher.MonoDefer.subscribe(MonoDefer.java:52)
		at reactor.core.publisher.Mono.subscribe(Mono.java:4400)
		at reactor.core.publisher.FluxSwitchIfEmpty$SwitchIfEmptySubscriber.onComplete(FluxSwitchIfEmpty.java:82)
		at reactor.core.publisher.MonoPeekTerminal$MonoTerminalPeekSubscriber.onComplete(MonoPeekTerminal.java:299)
		at reactor.core.publisher.MonoPeekTerminal$MonoTerminalPeekSubscriber.onComplete(MonoPeekTerminal.java:299)
		at reactor.core.publisher.MonoFlatMap$FlatMapMain.onNext(MonoFlatMap.java:148)
		at reactor.core.publisher.FluxSwitchIfEmpty$SwitchIfEmptySubscriber.onNext(FluxSwitchIfEmpty.java:74)
		at reactor.core.publisher.FluxFilter$FilterSubscriber.onNext(FluxFilter.java:113)
		at reactor.core.publisher.FluxDefaultIfEmpty$DefaultIfEmptySubscriber.onNext(FluxDefaultIfEmpty.java:101)
		at reactor.core.publisher.MonoNext$NextSubscriber.onNext(MonoNext.java:82)
		at reactor.core.publisher.FluxConcatMap$ConcatMapImmediate.innerNext(FluxConcatMap.java:282)
		at reactor.core.publisher.FluxConcatMap$ConcatMapInner.onNext(FluxConcatMap.java:863)
		at org.springframework.security.test.context.support.ReactorContextTestExecutionListener$DelegateTestExecutionListener$SecuritySubContext.onNext(ReactorContextTestExecutionListener.java:120)
		at org.springframework.cloud.sleuth.instrument.reactor.ScopePassingSpanSubscriber.onNext(ScopePassingSpanSubscriber.java:89)
		at reactor.core.publisher.Operators$MonoSubscriber.complete(Operators.java:1816)
		at reactor.core.publisher.MonoFlatMap$FlatMapInner.onNext(MonoFlatMap.java:249)
		at reactor.core.publisher.FluxDefaultIfEmpty$DefaultIfEmptySubscriber.onNext(FluxDefaultIfEmpty.java:101)
		at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onNext(FluxMapFuseable.java:129)
		at reactor.core.publisher.FluxFilterFuseable$FilterFuseableSubscriber.onNext(FluxFilterFuseable.java:118)
		at reactor.core.publisher.FluxMapFuseable$MapFuseableConditionalSubscriber.onNext(FluxMapFuseable.java:299)
		at reactor.core.publisher.FluxFilterFuseable$FilterFuseableConditionalSubscriber.onNext(FluxFilterFuseable.java:337)
		at reactor.core.publisher.Operators$MonoSubscriber.complete(Operators.java:1816)
		at reactor.core.publisher.MonoFlatMap$FlatMapMain.onNext(MonoFlatMap.java:151)
		at reactor.core.publisher.FluxFilterFuseable$FilterFuseableSubscriber.onNext(FluxFilterFuseable.java:118)
		at org.springframework.cloud.sleuth.instrument.reactor.ScopePassingSpanSubscriber.onNext(ScopePassingSpanSubscriber.java:89)
		at reactor.core.publisher.Operators$ScalarSubscription.request(Operators.java:2398)
		at org.springframework.cloud.sleuth.instrument.reactor.ScopePassingSpanSubscriber.request(ScopePassingSpanSubscriber.java:75)
		at reactor.core.publisher.FluxFilterFuseable$FilterFuseableSubscriber.request(FluxFilterFuseable.java:191)
		at reactor.core.publisher.MonoFlatMap$FlatMapMain.onSubscribe(MonoFlatMap.java:110)
		at reactor.core.publisher.FluxFilterFuseable$FilterFuseableSubscriber.onSubscribe(FluxFilterFuseable.java:87)
		at org.springframework.cloud.sleuth.instrument.reactor.ScopePassingSpanSubscriber.onSubscribe(ScopePassingSpanSubscriber.java:68)
		at reactor.core.publisher.MonoCurrentContext.subscribe(MonoCurrentContext.java:36)
		at reactor.core.publisher.InternalMonoOperator.subscribe(InternalMonoOperator.java:64)
		at reactor.core.publisher.MonoFlatMap$FlatMapMain.onNext(MonoFlatMap.java:157)
		at reactor.core.publisher.FluxMap$MapSubscriber.onNext(FluxMap.java:122)
		at reactor.core.publisher.FluxFilter$FilterSubscriber.onNext(FluxFilter.java:113)
		at reactor.core.publisher.FluxPeekFuseable$PeekConditionalSubscriber.onNext(FluxPeekFuseable.java:854)
		at reactor.core.publisher.FluxSwitchIfEmpty$SwitchIfEmptySubscriber.onNext(FluxSwitchIfEmpty.java:74)
		at reactor.core.publisher.MonoNext$NextSubscriber.onNext(MonoNext.java:82)
		at reactor.core.publisher.FluxFilter$FilterSubscriber.onNext(FluxFilter.java:113)
		at reactor.core.publisher.FluxFlatMap$FlatMapMain.tryEmitScalar(FluxFlatMap.java:488)
		at reactor.core.publisher.FluxFlatMap$FlatMapMain.onNext(FluxFlatMap.java:421)
		at reactor.core.publisher.FluxPeekFuseable$PeekFuseableSubscriber.onNext(FluxPeekFuseable.java:210)
		at reactor.core.publisher.FluxIterable$IterableSubscription.slowPath(FluxIterable.java:272)
		at reactor.core.publisher.FluxIterable$IterableSubscription.request(FluxIterable.java:230)
		at reactor.core.publisher.FluxPeekFuseable$PeekFuseableSubscriber.request(FluxPeekFuseable.java:144)
		at reactor.core.publisher.FluxFlatMap$FlatMapMain.onSubscribe(FluxFlatMap.java:371)
		at reactor.core.publisher.FluxPeekFuseable$PeekFuseableSubscriber.onSubscribe(FluxPeekFuseable.java:178)
		at reactor.core.publisher.FluxIterable.subscribe(FluxIterable.java:165)
		at reactor.core.publisher.FluxIterable.subscribe(FluxIterable.java:87)
		at reactor.core.publisher.Mono.subscribe(Mono.java:4400)
		at reactor.core.publisher.FluxConcatMap$ConcatMapImmediate.drain(FluxConcatMap.java:451)
		at reactor.core.publisher.FluxConcatMap$ConcatMapImmediate.onSubscribe(FluxConcatMap.java:219)
		at reactor.core.publisher.FluxIterable.subscribe(FluxIterable.java:165)
		at reactor.core.publisher.FluxIterable.subscribe(FluxIterable.java:87)
		at reactor.core.publisher.InternalMonoOperator.subscribe(InternalMonoOperator.java:64)
		at reactor.core.publisher.MonoDefer.subscribe(MonoDefer.java:52)
		at reactor.core.publisher.InternalMonoOperator.subscribe(InternalMonoOperator.java:64)
		at reactor.core.publisher.MonoDefer.subscribe(MonoDefer.java:52)
		at reactor.core.publisher.Mono.subscribe(Mono.java:4400)
		at reactor.core.publisher.MonoIgnoreThen$ThenIgnoreMain.subscribeNext(MonoIgnoreThen.java:263)
		at reactor.core.publisher.MonoIgnoreThen.subscribe(MonoIgnoreThen.java:51)
		at reactor.core.publisher.Mono.subscribe(Mono.java:4400)
		at reactor.core.publisher.FluxSwitchIfEmpty$SwitchIfEmptySubscriber.onComplete(FluxSwitchIfEmpty.java:82)
		at reactor.core.publisher.FluxFilter$FilterSubscriber.onComplete(FluxFilter.java:166)
		at reactor.core.publisher.FluxPeekFuseable$PeekConditionalSubscriber.onComplete(FluxPeekFuseable.java:940)
		at reactor.core.publisher.FluxSwitchIfEmpty$SwitchIfEmptySubscriber.onComplete(FluxSwitchIfEmpty.java:85)
		at org.springframework.security.test.context.support.ReactorContextTestExecutionListener$DelegateTestExecutionListener$SecuritySubContext.onComplete(ReactorContextTestExecutionListener.java:130)
		at reactor.core.publisher.Operators$ScalarSubscription.request(Operators.java:2400)
		at reactor.core.publisher.Operators$MultiSubscriptionSubscriber.set(Operators.java:2194)
		at reactor.core.publisher.Operators$MultiSubscriptionSubscriber.onSubscribe(Operators.java:2068)
		at org.springframework.security.test.context.support.ReactorContextTestExecutionListener$DelegateTestExecutionListener$SecuritySubContext.onSubscribe(ReactorContextTestExecutionListener.java:115)
		at reactor.core.publisher.MonoJust.subscribe(MonoJust.java:55)
		at reactor.core.publisher.Mono.subscribe(Mono.java:4400)
		at reactor.core.publisher.FluxSwitchIfEmpty$SwitchIfEmptySubscriber.onComplete(FluxSwitchIfEmpty.java:82)
		at reactor.core.publisher.MonoNext$NextSubscriber.onComplete(MonoNext.java:102)
		at reactor.core.publisher.FluxFilter$FilterSubscriber.onComplete(FluxFilter.java:166)
		at reactor.core.publisher.FluxFlatMap$FlatMapMain.checkTerminated(FluxFlatMap.java:846)
		at reactor.core.publisher.FluxFlatMap$FlatMapMain.drainLoop(FluxFlatMap.java:608)
		at reactor.core.publisher.FluxFlatMap$FlatMapMain.drain(FluxFlatMap.java:588)
		at reactor.core.publisher.FluxFlatMap$FlatMapMain.onComplete(FluxFlatMap.java:465)
		at reactor.core.publisher.FluxPeekFuseable$PeekFuseableSubscriber.onComplete(FluxPeekFuseable.java:277)
		at reactor.core.publisher.FluxIterable$IterableSubscription.slowPath(FluxIterable.java:294)
		at reactor.core.publisher.FluxIterable$IterableSubscription.request(FluxIterable.java:230)
		at reactor.core.publisher.FluxPeekFuseable$PeekFuseableSubscriber.request(FluxPeekFuseable.java:144)
		at reactor.core.publisher.FluxFlatMap$FlatMapMain.onSubscribe(FluxFlatMap.java:371)
		at reactor.core.publisher.FluxPeekFuseable$PeekFuseableSubscriber.onSubscribe(FluxPeekFuseable.java:178)
		at reactor.core.publisher.FluxIterable.subscribe(FluxIterable.java:165)
		at reactor.core.publisher.FluxIterable.subscribe(FluxIterable.java:87)
		at reactor.core.publisher.InternalMonoOperator.subscribe(InternalMonoOperator.java:64)
		at reactor.core.publisher.MonoDefer.subscribe(MonoDefer.java:52)
		at reactor.core.publisher.MonoFlatMap$FlatMapMain.onNext(MonoFlatMap.java:157)
		at reactor.core.publisher.Operators$MonoSubscriber.complete(Operators.java:1816)
		at reactor.core.publisher.FluxDefaultIfEmpty$DefaultIfEmptySubscriber.onComplete(FluxDefaultIfEmpty.java:109)
		at reactor.core.publisher.FluxMap$MapSubscriber.onComplete(FluxMap.java:144)
		at reactor.core.publisher.FluxMap$MapSubscriber.onComplete(FluxMap.java:144)
		at reactor.core.publisher.FluxFilter$FilterSubscriber.onComplete(FluxFilter.java:166)
		at reactor.core.publisher.FluxMap$MapConditionalSubscriber.onComplete(FluxMap.java:275)
		at reactor.core.publisher.Operators$MonoSubscriber.complete(Operators.java:1817)
		at reactor.core.publisher.MonoCacheTime$CoordinatorSubscriber.signalCached(MonoCacheTime.java:337)
		at reactor.core.publisher.MonoCacheTime$CoordinatorSubscriber.onNext(MonoCacheTime.java:354)
		at reactor.core.publisher.FluxPeek$PeekSubscriber.onNext(FluxPeek.java:200)
		at reactor.core.publisher.FluxSwitchIfEmpty$SwitchIfEmptySubscriber.onNext(FluxSwitchIfEmpty.java:74)
		at org.springframework.security.test.context.support.ReactorContextTestExecutionListener$DelegateTestExecutionListener$SecuritySubContext.onNext(ReactorContextTestExecutionListener.java:120)
		at org.springframework.cloud.sleuth.instrument.reactor.ScopePassingSpanSubscriber.onNext(ScopePassingSpanSubscriber.java:89)
		at reactor.core.publisher.MonoPeekTerminal$MonoTerminalPeekSubscriber.onNext(MonoPeekTerminal.java:180)
		at org.springframework.cloud.sleuth.instrument.reactor.ScopePassingSpanSubscriber.onNext(ScopePassingSpanSubscriber.java:89)
		at reactor.core.publisher.MonoPublishOn$PublishOnSubscriber.run(MonoPublishOn.java:181)
		at org.springframework.cloud.sleuth.instrument.reactor.ReactorSleuth.lambda$null$6(ReactorSleuth.java:324)
		at reactor.core.scheduler.SchedulerTask.call(SchedulerTask.java:68)
		at reactor.core.scheduler.SchedulerTask.call(SchedulerTask.java:28)
		at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
		at java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:304)
		at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
		at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
		at java.base/java.lang.Thread.run(Thread.java:833)

2022-07-21 12:57:58.464  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : Lush LushAdvice: LushAdvice(traceId=9cee80a028c511d4,9cee80a028c511d4, statusCode=200, warnings=[], extras={})
2022-07-21 12:57:58.464  INFO [lush-app-test,,] 5588 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : END: testUnexpectedExceptionNoLush
```

You will notice that most of this is standard Spring Framework plumbing - RequestMapping, PreAuthorize, Mono, etc.  The one variation is the usage of the @LushControllerMethod annotation - and this, my friends is the first concept in Lush.  

---
**NOTE**

It works with almost all markdown flavours (the below blank line matters).

---

You use the @LushControllerMethod annotation to tell Lush that you would like it to perform it's magic around this endpoint.  What is that magic you might ask?  I promise that by the end of this guide it will be clear.  But for now, I'll point out these two things:
 
1. Given the **@LushControllerMethod** annotation, Lush had indeed injected itself around this endpoint.  If you want to Lush to show all of it's debug logging, simply set the lush.core.debug logger to DEBUG logging level.
2. Since we are using a Lush service, security is being enforced via the Lush Ticket mechanism - more on this later.

Lo and behold, if we invoke this through our unit tests, we will see that the following is returned:

```text
2022-07-14 09:35:31.976  INFO [lush-app-test,7065515e824835f7,7065515e824835f7] 54967 --- [     parallel-4] [paul] c.p.l.e.s.controller.ExampleController   : ping() has been called
2022-07-14 09:35:31.977  INFO [lush-app-test,,] 54967 --- [           main] [core] c.p.lush.example.LushExampleServiceTest  : Ping results: Powered By Lush
```

Now, you might be wondering - ok this doesn't look like much but... that is kind of the point - Lush wants to stay out of your way and let you focus on your app and doing things the Spring way while still providing its benefits.  Let's move on...

#### The LushTicket
Introducing...  The Lush Ticket - the next concept to discuss.  You'll notice that the code below looks much like the code above except for the injection of the LushTicket parameter.

```java
    @LushControllerMethod
    @RequestMapping("sayHi")
    @PreAuthorize("isAuthenticated()")
    public Mono<String> sayHi( LushTicket ticket) {
        return Mono.just( String.format( "%s says hi!", ticket.getUsername()) );
    }
```

Lush uses a LushTicket instance to represent the current user of the service (think Spring UserDetails).  Out of the box, any Lush based service will already have Spring Security preconfigured to only allow requests that contain a LushTicket.  We will discuss how to make that happen later.

For now, back to the controller, in the example above we showed a very simple endpoint that simply returned a String (given that Lush is based on Spring WebFlux, all responses should be wrapped by a Mono or a Flux).

In the next example we will show an endpoint that returns a List of integers












#### Add GitHub repo
