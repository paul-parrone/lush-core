package com.px3j.lush.web.common;


import brave.baggage.BaggageField;
import com.px3j.lush.core.exception.StackTraceToLoggerWriter;
import com.px3j.lush.core.model.LushAdvice;
import com.px3j.lush.core.model.LushContext;
import com.px3j.lush.core.ticket.LushTicket;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Decorator, applied via AOP, that intercepts calls to any Lush based controllers.  It silently intercepts and provides
 * the Lush functionality.
 *
 * @author Paul Parrone
 */
@Slf4j(topic = "lush.core.debug")
public abstract class ControllerDecorator {
    protected final BaggageField lushUserNameField;
    protected final Tracer tracer;

    @Autowired
    public ControllerDecorator(BaggageField lushUserNameField, Tracer tracer) {
        this.lushUserNameField = lushUserNameField;
        this.tracer = tracer;
    }

    @Pointcut("@annotation(com.px3j.lush.web.common.LushControllerMethod)")
    public void lushControllerMethods() {
    }

    /**
     * Helper method to populate the returned LushAdvice properly in the event that an unexpected exception occurs during
     * this call.
     *
     * @param lushContext The context to populate.
     * @param throwable   The exception causing the error.
     */
    protected void errorHandler(LushContext lushContext, Throwable throwable) {
        throwable.printStackTrace(new StackTraceToLoggerWriter(log));

        LushAdvice advice = lushContext.getAdvice();
        if (advice == null) {
            log.warn("LushAdvice is null within LushContext - cannot set status codes");
            return;
        }

        advice.setStatusCode(-99);
        advice.putExtra("lush.isUnexpectedException", true);
    }

    /**
     * Helper method to inject the LushTicket if the method being called requests it.
     *
     * @param method The method being called.
     * @param pjp    The joinpoint.
     * @param ticket The ticket instance to inject.
     */
    protected void injectTicket(Method method, ProceedingJoinPoint pjp, LushTicket ticket) {
        findArgumentIndex(method, LushTicket.class)
                .ifPresent((i) -> {
                    if (log.isDebugEnabled()) {
                        log.debug("Lush - Injecting LushTicket");
                    }

                    LushTicket contextArg = (LushTicket) pjp.getArgs()[i];
                    contextArg.populateFrom(ticket);
                });
    }

    /**
     * Helper method to inject the LushContext if the method being called requests it.
     *
     * @param method      The method being called
     * @param pjp         The joinpoint.
     * @param lushContext The context instance to inject.
     */
    protected void injectLushContext(Method method, ProceedingJoinPoint pjp, LushContext lushContext) {
        findArgumentIndex(method, LushContext.class)
                .ifPresent((i) -> {
                    if (log.isDebugEnabled()) {
                        log.debug("Lush - Injecting LushContext");
                    }

                    LushContext contextArg = (LushContext) pjp.getArgs()[i];
                    contextArg.setTraceId(lushContext.getTraceId());
                    contextArg.setAdvice(lushContext.getAdvice());
                });
    }

    /**
     * Find the argument index of the parameter of type clazz (if there is one) and return the index.
     *
     * @param method The method object to check.
     * @param clazz  The type of argument to find.
     * @return The index if found, or empty.
     */
    protected Optional<Integer> findArgumentIndex(Method method, Class clazz) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i] == clazz) {
                return Optional.of(i);
            }
        }

        return Optional.empty();
    }

    /**
     * Extract a Method object representing the method being called from the joinpoint.
     *
     * @param pjp The joinpoint to inspect.
     * @return A Method instance represent the method being called.
     * @throws NoSuchMethodException If the method being called cannot be found.
     * @throws SecurityException     If the method cannot be accessed.
     */
    protected Method getMethodBeingCalled(ProceedingJoinPoint pjp) throws NoSuchMethodException, SecurityException {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();

        if (method.getDeclaringClass().isInterface()) {
            method = pjp.getTarget().getClass().getDeclaredMethod(signature.getName(), method.getParameterTypes());
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Lush ControllerDecorator - invoking: %s::%s", method.getDeclaringClass(), method.getName()));
        }

        return method;
    }
}
