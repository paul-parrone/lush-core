package com.px3j.lush.core.exception;

import org.slf4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Print each line of the stack trace individually to the passed in Logger.  Ensures that the context information
 * shows up on each line of the stack trace in the log.
 *
 * Use it like this:<br><br>
 *         exception.printStackTrace( new StackTraceToLoggerWriter(logger) );
 *
 * @author Paul P. Parrone Jr.
 */
public class StackTraceToLoggerWriter extends PrintWriter {
    /**
     * Constructor.
     *
     * @param pLogger The logger that the stack trace will be written to.
     */
    public StackTraceToLoggerWriter(Logger pLogger ) {
        super( new StringWriter() );
        _logger = pLogger;
    }

    @Override
    public void print(String s) {
        StringBuffer lMsg = new StringBuffer( "    " );
        lMsg.append( s );
        _logger.error( lMsg.toString() );
    }

    /**
     * Overloaded to call logger.error() for each line in the stack trace.  Will be called by the logger.
     *
     * @param x an element of the stack trace.
     */
    @Override
    public void println( char x[] ) {
        StringBuffer lMsg = new StringBuffer( "    " );
        lMsg.append( x );
        _logger.error( lMsg.toString() );
    }



    /**
     * Overloaded to call logger.error() for each line in the stack trace.  Will be called by the logger.
     *
     * @param x an element of the stack trace
     */
    @Override
    public void println( String x ) {
        StringBuffer lMsg = new StringBuffer( "    " );
        lMsg.append( x );
        _logger.error( lMsg.toString() );
    }

    private final Logger _logger;
}
