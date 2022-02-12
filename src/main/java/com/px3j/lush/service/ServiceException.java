package com.px3j.lush.service;

public class ServiceException extends RuntimeException {
    private final String code;
    private final String displayableMessage;

    public ServiceException(String code, String displayableMessage) {
        this.code = code;
        this.displayableMessage = displayableMessage;
    }

    public ServiceException(Throwable cause, String code, String displayableMessage) {
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