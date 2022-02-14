package com.px3j.lush.service;

import lombok.*;

/**
 * Context available if needed by any Controller.  Provides access to an ApiResponse instance to be used to convey
 * outcome to the caller (usually a UI).
 *
 * @author Paul Parrone
 */
@NoArgsConstructor
@Data
public class Context {
    private ResponseAdvice response;
    private String traceId;
}