package com.px3j.lush.service;

import lombok.*;

import java.util.*;

/**
 * Encapsulates the response from an 'API' call.
 *
 *
 * @author Paul Parrone
 */
@NoArgsConstructor
public class ResponseAdvice {
    @Getter @Setter
    private String traceId = "";
    @Getter @Setter
    private int statusCode = 0;

    private List<Detail> detail = new ArrayList<>();
    private final Map<String,Object> extras = new HashMap<>();

    public ResponseAdvice(String traceId, int statusCode) {
        this.traceId = traceId;
        this.statusCode = statusCode;
    }

    public Map<String, Object> getExtras() {
        return Collections.unmodifiableMap(extras);
    }

    public Collection<Detail> getDetail() {
        return Collections.unmodifiableList(detail);
    }

    public void addDetailCode( Detail detail ) {
        this.detail.add( detail );
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

    @Data
    @AllArgsConstructor
    public static class Detail {
        private int code;
        private String message;
    }
}
