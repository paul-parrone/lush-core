####Thoughts?
* Passport service?
* Apache Apisix?
* JWT/Basic -> Gateway -> Passport -> Microservice
* 

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

 
####Goals
* Provide value without getting in the way
* Provide patterns for common application interactions
* Provide integrated architecture code for front/back end.


* Automatic log tracking.
* Controller implementation patterns.
* Exception handling patterns.
* Integrated front-end portion - carries patterns to UI.
* Simple well-defined conversation protocol.



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




