package com.px3j.service.core.api;

public class ApiException extends RuntimeException {
    private String code;
    private String displayableMessage;

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
