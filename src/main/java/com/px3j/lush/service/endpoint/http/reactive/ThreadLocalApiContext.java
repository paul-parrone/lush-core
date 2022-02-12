package com.px3j.lush.service.endpoint.http.reactive;

import com.px3j.lush.service.Context;

/**
 * ThreadLocal to contain ApiContext - allows passing from the WebFlux layer into our Aspects
 *
 * @author Paul Parrone
 */
class ThreadLocalApiContext {
    static ThreadLocal<Context> threadLocal = ThreadLocal.withInitial(CarryingContext::new);

    public static Context get() {
        return threadLocal.get();
    }
}
