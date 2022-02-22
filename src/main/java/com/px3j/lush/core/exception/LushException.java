package com.px3j.lush.core.exception;

/**
 * Base class for all exceptions in Lush.
 *
 * @author Paul Parrone
 */
public class LushException extends RuntimeException {
    public LushException() {
        super();
    }

    public LushException(String message) {
        super(message);
    }

    public LushException(String message, Throwable cause) {
        super(message, cause);
    }

    public LushException(Throwable cause) {
        super(cause);
    }

    protected LushException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
