package com.px3j.lush.core;

import lombok.*;

import java.util.*;

/**
 * Contains 'advice' generated from the invocation of an endpoint.
 *
 * @author Paul Parrone
 */
@NoArgsConstructor
public class ResultAdvice {
    @Getter @Setter
    private String traceId = "";
    @Getter @Setter
    private int statusCode = 0;

    private List<ResultDetail> resultDetail = new ArrayList<>();

    private final Map<String,Object> extras = new HashMap<>();

    public ResultAdvice(String traceId, int statusCode) {
        this.traceId = traceId;
        this.statusCode = statusCode;
    }

    /**
     * Get any extras that have been added to this advice.
     * @return Map of extras, could be empty.
     */
    public Map<String, Object> getExtras() {
        return Collections.unmodifiableMap(extras);
    }

    /**
     * Add an extra to be sent back to the caller if necessary.
     *
     * @param key key.
     * @param value value to send back.
     */
    public void putExtra(final String key, final Object value ) {
        this.extras.put( key, value );
    }

    /**
     * Get any detail that was added to the advice from the controller.
     *
     * @return The detail of the result.
     */
    public Collection<ResultDetail> getResultDetail() {
        return Collections.unmodifiableList(resultDetail);
    }

    /**
     * Add a ResultDetail instance to this advice.  This will be carried back to the caller.
     *
     * @param resultDetail A ResultDetail instance to add to this advice.
     */
    public void addResultDetail(ResultDetail resultDetail) {
        this.resultDetail.add(resultDetail);
    }

    @Data
    @AllArgsConstructor
    public static class ResultDetail {
        private final int code;
        private final Map<String,Object> detail;

        public ResultDetail(int code) {
            this.code = code;
            detail = new HashMap<>();
        }
    }
}
