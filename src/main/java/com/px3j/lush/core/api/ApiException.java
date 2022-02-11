package com.px3j.lush.core.api;

public class ApiException extends RuntimeException {
    private final String code;
    private final String displayableMessage;

    public ApiException(String code, String displayableMessage) {
        this.code = code;
        this.displayableMessage = displayableMessage;
    }

    public ApiException(Throwable cause, String code, String displayableMessage) {
        super(cause);
        this.code = code;
        this.displayableMessage = displayableMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayableMessage() {
        return displayableMessage;
    }
}
