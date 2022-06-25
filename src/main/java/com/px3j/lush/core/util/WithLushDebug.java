package com.px3j.lush.core.util;

import org.slf4j.Logger;

public interface WithLushDebug {
    Logger getLushDebug();

    default boolean isDbg() {
        return getLushDebug().isDebugEnabled();
    }

    default void log(final String message ) {
        getLushDebug().debug( "Lush :: " + message );
    }

    default void warn( final String message ) {
        getLushDebug().warn( "Lush :: " + message );
    }
}
