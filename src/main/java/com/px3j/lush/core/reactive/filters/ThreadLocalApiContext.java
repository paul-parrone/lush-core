package com.px3j.lush.core.reactive.filters;

import com.px3j.lush.core.api.ApiContext;

/**
 * ThreadLocal to contain ApiContext - allows passing from the WebFlux layer into our Aspects
 *
 * @author Paul Parrone
 */
public class ThreadLocalApiContext {
    static ThreadLocal<ApiContext> threadLocal = ThreadLocal.withInitial(CarryingApiContext::new);

    public static ApiContext get() {
        return threadLocal.get();
    }
}
