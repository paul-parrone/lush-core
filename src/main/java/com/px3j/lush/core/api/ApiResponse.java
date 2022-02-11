package com.px3j.lush.core.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ApiResponse {
    private String requestKey = "";
    private int statusCode = 0;
    private String displayableMessage = "";
    private final Map<String,Object> extras = new HashMap<>();

    public ApiResponse() {
    }

    public ApiResponse(String requestKey, int statusCode, String displayableMessage) {
        this.requestKey = requestKey;
        this.statusCode = statusCode;
        this.displayableMessage = displayableMessage;
    }

    public String getRequestKey() {
        return requestKey;
    }

    public void setRequestKey(String requestKey) {
        this.requestKey = requestKey;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getDisplayableMessage() {
        return displayableMessage;
    }

    public void setDisplayableMessage(String displayableMessage) {
        this.displayableMessage = displayableMessage;
    }

    public Map<String, Object> getExtras() {
        return Collections.unmodifiableMap(extras);
    }

    public void putExtra(final String key, final Object value ) {
        this.extras.put( key, value );
    }
}
