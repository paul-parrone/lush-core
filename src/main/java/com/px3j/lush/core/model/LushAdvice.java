package com.px3j.lush.core.model;

import lombok.*;

import java.util.*;

/**
 * Represents the advice that is generated from the invocation of an endpoint.
 *
 * @author Paul Parrone
 */
@NoArgsConstructor
@ToString
public class LushAdvice {
    @Getter @Setter
    private String traceId = "";
    @Getter @Setter
    private int statusCode = 0;

    private List<LushWarning> warnings = new ArrayList<>();

    private final Map<String,Object> extras = new HashMap<>();

    public LushAdvice(String traceId, int statusCode) {
        this.traceId = traceId;
        this.statusCode = statusCode;
    }

    public LushAdvice(String traceId) {
        this( traceId, 0 );
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
     * Get any warnings contained in this advice.
     *
     * @return The detail of the result.
     */
    public Collection<LushWarning> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    /**
     * Add a warning to this advice.  This will be carried back to the caller.
     *
     * @param warning A ResultDetail instance to add to this advice.
     */
    public void addWarning(LushWarning warning) {
        this.warnings.add(warning);
    }

    @Data
    @AllArgsConstructor
    public static class LushWarning {
        private final int code;
        private final Map<String,Object> detail;

        public LushWarning(int code) {
            this.code = code;
            detail = new HashMap<>();
        }
    }
}
