### Introduction
In this day and age of libraries and frameworks - stacked on top of libraries and frameworks, it can be somewhat daunting to narrow down and pick the optimal set of components to design and build an application architecture.

By design, frameworks such as Spring are designed to solve a very broad range of issues - all of these things above what one would need to build an application.  The design of Spring reflects this, choose what you need for your particular application and the framework will integrate these components as easily as possible.

This begs the question - How does one go from the seemingly endless functionality of Spring down to an application architecture that can support whatever it is we are building?  This is where the ideas behind Lush come from.

When I try to explain Lush with a visual, I say that it helps to think of Lush as being what comes out if we were to put the entire Spring Framework through an **application architecture funnel**.

TODO: PUT A FUNNEL IMAGE HERE :)

The end result being an application architecture that works for our application while avoiding complexities we may not need.  It's also critical that our application architecture is not so opinionated that it does not allow for the breadth of Spring if/when we need it.  

Before we continue, let me give you some thoughts on what I believe an application architecture is.  Over the years, I've been fortunate to build quite a few systems from the ground up.  I have always found that there is a bare minimum amount of infrastructure code that is necessary to both get started and build the system in a consistent and reliable manner.

To me, an application architecture provides simple, understandable patterns and/or implementations.  It tries to take higher level decisions out of the developers hands which in turns enables them to focus on building the application.  A good application architecutre will do these things and seamlessly ensure things such as:

* Log tracing
* Exception handling
* Exception propagation
* Security

### Categorize
Given this, below are the high level goals of Lush:

* Provide value without getting in the way
* Define patterns for common application interactions.
* Provide integrated architecture code for front/back end.

Lush strives to have a very small learning curve by building on the same patterns that Spring already provides.  As you'll see in the examples below, Lush specific behavior is completely optional and provided via simple parameter injection and/or AOP.

By providing a small but powerful set of core classes and defining a set of simple protocols Lush helps create an application that is production ready and consistent.  All achieved without developers being burdened with the details on how to make these things happen,  As a matter of fact, we like to think that Lush can take many of the mundane decisions out of developers hands.









#### Spring Integrations
* Spring Sleuth and Zipkin for log tracing.
  * acting user name automatically added to MDC
  * acting user name automatically propagated to Zipkin
* Spring Security for endpoints.
* Spring WebFlux




###TODO: Things to explain
* Passport
* Security
* Advice/Warnings
* LushContext
* 


Lush tries to break things down into three types of endpoint requests:

1. Just give me some list of data.
2. Submit some data (probably from a form) and update some persistent store.
3. Submit some data, validate it, let me know if the data is accepted or not.

On the surface, this is simple and in the happy path it is.  One can use the normal Spring abstractions to create a Controller, Service and Dao and all is well.  But, if we are thinking of making sure this is production ready, a bunch of other ugly questions rear their heads:

1. What if an exception occurs?
2. What if I'm validating the input and the changes can't be made?
3. If something does go wrong, how will we troubleshoot it?

Now, we can leave the answers to these questions up to each individual developer but that is guaranteed to result in very inconsistent results.  This is the space that Lush tries to fill in.

How does Lush do this?  Via the below:

* Enhanced controller implementation patterns - Spring with extras.
* A simple and well-defined conversation protocol.
* *Invisible* exception handling - through the power of AOP.
* Integrated front-end libraries - automatically carry Lush patterns to the front-end.
* Automatic injection of LushContext and/or Passport if needed.




###Requests
The three types of requests:

* Just give me some data
* Submit some data (aka: form)
* Conversational - submit some data, may result is acceptance or denial based on some validation


**Just give me some data**
**Submit some data**
Lush stays out of the way, you can build your controller exactly as you would in Spring.  No extra work is *necessary* but Lush is still there to handle any unexpected conditions in the Lush way.

TODO: Example



**Conversational**
Again, Lush stays out of the way, but through the Lush context, the server side code can send result details back to the caller.  In this manner, Lush allows for data to be sent back as well as 'advice' as to why or why not the operation was allowed to happen.

TODO: Example





####Roadmap
1. Scope Lush (at least what want to show)
2. Implement it
3. Create Maven archetype
4. Document it (iterative)
5. Decide on an illustrative project
6. Implement the project
7. Document it
8. Deploy it


###Integrations
####Zipkin
docker pull openzipkin/zipkin
docker run -d -p 9411:9411 openzipkin/zipkin

####Logstash JSON

####ELK
[ELK prerequisites](https://github.com/elastic/stack-docker#prerequisites)

*Make sure Docker VM has 4gb (Docker Desktop Settings)*

```shell
docker network create lush-dev

docker pull logstash:8.0.0
docker run -d --name logstash --net lush-dev -p 5601:5601 logstash:8.0.0

docker run --name logstash --net lush-dev --rm -it -v ~/docker/logstash/pipeline:/usr/share/logstash/pipeline/ -v ~/docker/logstash/logs:/usr/share/logstash/logs/ docker.elastic.co/logstash/logstash:8.0.0

docker pull kibana:8.0.0
docker run -d --name kibana --net lush-dev -p 5601:5601 kibana:8.0.0

docker pull elasticsearch:8.0.0
docker run -d --name elasticsearch --net lush-dev -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" elasticsearch:8.0.0
```

[Kibana](http://localhost:5601)
[Spring Boot Admin](http://localhost:9090/wallboard)



####Scope
* Normal endpoint behavior
* Security (passport)
* Sleuth logging
* Runnable tests (simple)
* 





The Lush Framework is built on top of Spring, it is not meant to replace Spring but to augment it - the Spring Framework has a much larger scope.  Think of Lush as the Spring Framework poured through an Application Architecture funnel.

The Lush Framework has a few overarching goals:

* Stay out of the way
* Provide a set of patterns and/or code to help develop applications


 ```text
Terms: endpoint, result advice, result data...


invoke endpoint
endpoint returns results in form of data and/or advice
unexpected exceptions are handled automatically
advice is injected as requested and passed back to the caller automatically


```



####Things to know:


Controller

Automatic injection of Context if necessary





####Request Categories
* **Non-conversational** - the caller requests some operation, the operation either succeeds or it fails unexpectedly the service will not knowingly refuse to perform the operation.
 
* **Conversational** - the caller requests some operation, the service may refuse to perform the action due to some validation or other state of the system

For the non-converational type requests, you can envision method signatures as shown below - bear in mind that any operation can trigger an unexpected exception, this is always signaled via runtime exceptions hence you won't see it in the mehod signature.


Retrieve - simply meant to return 1..n of some entity
```java
    @RequestMapping(value = "findOne", method = RequestMethod.POST)
    Flux<Cat> fetchAll() {}
```

Create/Update : performs some update in the system - no validation of data required.
```java
    @RequestMapping(value = "updateAge", method = RequestMethod.POST)
    Mono<Cat> updateAge( @RequestParam int age )
```


The signatures above don't need to signal anything specific to the caller - it either returns the requested data or fails unexpectedly causing a RuntimeException (as you'll see later this can be magically handled by the front-end portion of the Lush Framework).  

But, what do we do when the service has to convey to the client that it refused to perform the operation and why it has done so?

The one obvious way is to update the method signature to return some 'wrapper object' that carries both bits of information.  

For example:
```java
    class MaybeACatButPossiblyAMessage {
        Cat mayBePopulatedCat;
        RandomInfoAsToWhyOperationWasRefused refusalDetails;
    };

    @RequestMapping(value = "updateAge", method = RequestMethod.POST)
    Mono<MaybeACatButPossiblyAMessage> updateAge( @RequestParam int age )
```

The mechanism above works however it puts a lot of burden on both the caller and the callee (TODO: details on this).  The Lush Framework takes a different approach that reduces the burden on the developer, results in cleaner method calls and allows for a 'conversational approach'.

If the developer of a service endpoint determines that there is a reason that it may refuse the operation they can signal to the Framework that the method is indeed conversational.  How does it do this?  

Let's look at an example:
```java
    @RequestMapping(value = "updateAge", method = RequestMethod.POST)
    Mono<Cat> updateAge( @RequestParam int age, Context lushContext )
```

A couple of things to notice:

1. The return type hasn't changed
2. There is an extra object passed to the method - Context


This allows the framework to evolve by adding information to the Context class as needed.






Create/Update : performs some update in the system - validation of data required (action may or may not be taken based on input).


In all cases there are 1 or more outcomes.

Retrieve:
Success
Unexpected exception





While this iteration focuses on Java/Spring, I think the concepts can be carried to any language/framework.


Lush Image:
http://cliparts.co/clipart/2398680


