package com.px3j.lush.core.reactive.filters;

import com.px3j.lush.core.api.ApiContext;

public class ThreadLocalApiContext {
    static ThreadLocal<ApiContext> threadLocal = ThreadLocal.withInitial(CarryingApiContext::new);

    public static ApiContext get() {
        return threadLocal.get();
    }
}
