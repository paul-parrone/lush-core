# Lush Service Architecture



## Introduction
In this day and age of libraries and frameworks stacked on top of libraries and frameworks, it can be quite daunting to narrow down and pick the optimal set of components to design and build an service architecture.

By design, frameworks such as Spring exist to solve a very broad range of problems (and they do that quite well) - some of these things we need for our applications and others we don't.  The design of Spring reflects this, you choose what you need for your particular application (and ignore what you don't) and the framework ensures that those chosen components work together as easily as possible.

This begs the question - How does one go from the seemingly endless functionality of the Spring Framework down to an architecture that will support our application?  This is where the ideas behind Lush come from.

Lush is born of multiple iterations of service architecture that I have had the good fortune to build (on top of the vast Spring Framework) over the years.  If you think of it in visual terms, Lush is what comes out if we were to pour the entire Spring Framework through a **service architecture funnel**.

Lush doesn't try to wrap or hide the Spring Framework at all, it simply provides a pre-assembled set of components that already work together to support a service architecture - a head start if you will.  If Spring is individual blocks, Lush is a set of those blocks partially assembled.

I've been fortunate to build quite a few systems from the ground up.  I have repeatedly found that there is a bare minimum amount of infrastructure code and design patterns necessary to get started and build a consistent and reliable product.  Think of it as infrastructure that answers the following questions (not an all-inclusive list):

* What exception handling guidelines to I enforce and how do I remove the responsibility from developers?
* How can consistently send data to the front-end?
* How can I enable a consistent 'conversational protocol' between my services and consumers of them?
* How should services be secured?
* How to propagate user information (identifiers, roles, etc.) into and across services?
* How can I make it as simple as possible for support team to analyze logs when things go wrong?

I believe that a service architecture should provide simple, understandable implementations and/or patterns.  It strives to take common higher-level decisions out of developers hands - and does so as seamlessly as possible.  Lush is my technique to build something that does just these things.

Ok, with all of that said, lets get into some details.

## Overview
As stated earlier, think of Lush as a pre-assembled set of building blocks - below are the Spring components already integrated into Lush:

* **Spring Boot**
  * Nothing much to say here just that all Lush services are based on Spring Boot.


* **Spring WebFlux**
  * Simple annotation (@LushControllerMethod) to inject Lush behaviors into Controllers.
  * Automatically inject Lush Ticket and/or LushContext as desired.
  * Lush Advice is automatically propagated to the caller of the Controller method.
  * Lush provides automatic exception handling around Controller Endpoint methods.


* **Spring Security**
  * Lush Ticket abstraction already integrated
  * Configurable secure/insecure paths (Spring Security)
  * Seamless use of Spring Security related annotations (@PreAuthorize, @PostAuthorize)


* **Spring Cloud Discovery**
  * Discovery automatically enabled in all Lush Services.
  * For local development, Lush uses Consul for service discovery.


* **Spring Cloud Sleuth**
  * Already configured to capture the current user of a service.
  * Extra baggage field added so that original user is carried through all Lush service calls.


* **Spring Boot Actuator**
  * Already included in the starter and preconfigured.

In addition to the preconfigured Spring building blocks, Lush provides a number of other benefits:

#### Consistent Logging and Exception Handling
* Actor name present in all Lush service logs.
* Spring Cloud Sleuth is preconfigured - unique trace id is transferred across Lush Services.
* Leave the exception handling to Lush - your developers have enough to worry about.

#### General
* Maven Archetype makes it trivial quickly create a Lush service.
* Lush 'starter' POM to easily add Lush to any existing services.

#### Rich Protocol
* Based on Spring WebFlux
* Lush Advice - automatically provided by the architecture and sent back to callers
* JavaScript code to interpret and react to Lush Protocol.

#### Monitoring
* JSON formatted logs
* Spring Boot Actuator included and preconfigured

#### Aids in development
* Special **developer** profile to provide out-of-box settings when run on developers computer.
* A **clear-ticket** profile to allow a clear text JSON Lush Ticket to access secured services.

Now that we have all that out of the way, let's introduce a few of the core concepts of Lush.  This will help provide some
understanding of how Lush accomplishes what it does - then we can move on to the Getting Started guide.

### Lush Concepts
Lush introduces a few simple concepts that you can add to your existing knowledge of Spring.

* Entry point
* LushContext
* LushAdvice
* LushTicket

#### Entry Point
Lush views an Entry Point as the those places where our service functionality is exposed to consumers.  In many cases this is through a REST endpoint (Controller).  Lush also has built in support for exposing service functionality via JMS - with more to come.  Lush injects much of its magic on inbound/outbound traffic through Entry Points.

I won't go too deep into these concepts here as it will be crystal clear once you create your first service (from the Maven archetype) and view the Getting Started guide.

#### LushContext
LushContext is a simple object containing an instance of LushAdvice (described below) and a unique trace id for the current request.
```java
public class LushContext {
    private LushAdvice advice;
    private String traceId;
}
```

#### LushAdvice
LushAdvice gives you the ability to return more than just data to the consumer of your service - without having to wrap your responses in an envelope object.

| Member     | Description                                                               | Usage                                                                                                                                                                                                |
|------------|---------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| traceId    | The unique traceId for the current request                                | Used mainly by Lush but accessible if you should need it.                                                                                                                                            |
| statusCode | The application status code (this is different than the HTTP status code) | This is the overall application defined status code for the request - use to signify application warnings/failures to your callers.                                                                  |
| warnings   | List of LushWarning objects that can be returned to the caller            | Warnings that the caller should respond to.  Each instance includes a code and some details.  You can use this to send data back to the caller but also signal that there could be potential issues. |
| extras     | Map of key/value pairs that also can be returned                          | Any other information you want to send back to the caller.                                                                                                                                           |  
#### Lush Ticket
LushTicket is how callers gain entry to any Lush based service.  Out of the box, Lush comes with Spring Security preconfigured to allow access only if there is a Lush Ticket contained in the incoming request (for REST endpoints, it is contained in an HTTP header).  

Lush doesn't define how Lush Tickets are created - and it is extensible enough to carry any information your particular use-case requires.  

#### Lush Annotations
Last but not least, I wanted to introduce you to Lush Annotations.  Most of Lush's features are injected into your code by using these annotations, they are defined below. 

| Annotation            | Usage                                                                                                 |
|-----------------------|-------------------------------------------------------------------------------------------------------|
| @LushControllerMethod | Tells Lush to inject it's behavior, typically used on Controller methods exposed via @RequestMapping. |
| @LushJmsEndpoint      | Tells Lush that this listener method should be decorated.                                             |
| @LushJmsTemplate      | Use this to tell Lush to inject it's behavior around a JmsTemplate instance                           |


#### Developer Mode
Before concluding, I wanted to touch upon how Lush helps with writing/testing services on development machines.  This as always been a pain point that I've tried to solve - especially when there are multiple services in play.  This is where __developer mode__ comes from.  

As of this writing, Lush provides a few simple techniques that make it easier to work on a service locally.

1. A Maven build profile for local builds that will auto-configure Consul for service discovery.
2. A Spring profile called __clear-ticket__ which enables developers to satisfy service security by using a clear text JSON formatted Lush Ticket.

Hopefully this has given a good overview of what Lush is and how it can help you. Please view the [Getting Started](http://www.github.com/paul-parrone/lush-core/blob/master/docs/getting-started.md) guide to get a quick hands on overview of Lush.
