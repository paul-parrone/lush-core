package com.px3j.lush.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates the response from an 'API' call.
 *
 * @author Paul Parrone
 */
@NoArgsConstructor
public class ServiceResponse {
    @Getter @Setter
    private String requestKey = "";
    @Getter @Setter
    private int statusCode = 0;
    @Getter @Setter
    private String displayableMessage = "";
    private final Map<String,Object> extras = new HashMap<>();

    public ServiceResponse(String requestKey, int statusCode, String displayableMessage) {
        this.requestKey = requestKey;
        this.statusCode = statusCode;
        this.displayableMessage = displayableMessage;
    }

    public Map<String, Object> getExtras() {
        return Collections.unmodifiableMap(extras);
    }

    /**
     * Carry any extra data back to the caller if necessary.
     *
     * @param key key.
     * @param value value to send back.
     */
    public void putExtra(final String key, final Object value ) {
        this.extras.put( key, value );
    }
}
