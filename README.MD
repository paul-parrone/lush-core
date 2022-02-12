


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
    Mono<Cat> updateAge( @RequestParam int age, Context context )
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




