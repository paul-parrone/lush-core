package com.px3j.lush.core.model;

import lombok.*;

/**
 * Context that will be injected to a Controller method if listed as a parameter.  Provides access to an ResponseAdvice
 * instance to be used to convey outcome to the caller (usually a UI).
 *
 * The tradeId is a unique key that can be used to trace a request throughout the system.
 *
 * @author Paul Parrone
 */
@NoArgsConstructor
@Data
public class LushContext {
    private LushAdvice advice;
    private String traceId;
}