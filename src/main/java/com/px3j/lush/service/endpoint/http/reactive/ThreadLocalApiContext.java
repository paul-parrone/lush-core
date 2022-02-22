package com.px3j.lush.service.endpoint.http.reactive;

import com.px3j.lush.core.LushContext;

/**
 * ThreadLocal to contain ApiContext - allows passing from the WebFlux layer into our Aspects
 *
 * @author Paul Parrone
 */
class ThreadLocalApiContext {
    static ThreadLocal<LushContext> threadLocal = ThreadLocal.withInitial(CarryingContext::new);

    public static LushContext get() {
        return threadLocal.get();
    }
}
