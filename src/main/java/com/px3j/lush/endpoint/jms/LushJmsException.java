package com.px3j.lush.endpoint.jms;

import com.px3j.lush.core.exception.LushException;

/**
 * Used by Lush to signify JMS related exceptions.
 */
public class LushJmsException extends LushException {
    public LushJmsException() {
        super();
    }

    public LushJmsException(String message) {
        super(message);
    }

    public LushJmsException(String message, Throwable cause) {
        super(message, cause);
    }

    public LushJmsException(Throwable cause) {
        super(cause);
    }

    protected LushJmsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
