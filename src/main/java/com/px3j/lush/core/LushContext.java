package com.px3j.lush.core;

import lombok.*;

/**
 * Context that will be injected to a Controller method if listed as a parameter.  Provides access to an ResponseAdvice
 * instance to be used to convey outcome to the caller (usually a UI).
 *
 * @author Paul Parrone
 */
@NoArgsConstructor
@Data
public class LushContext {
    private Advice advice;
    private String traceId;
}