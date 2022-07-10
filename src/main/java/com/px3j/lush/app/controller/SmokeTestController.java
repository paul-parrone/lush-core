package com.px3j.lush.app.controller;

import com.px3j.lush.core.exception.LushException;
import com.px3j.lush.core.model.Advice;
import com.px3j.lush.core.model.LushContext;
import com.px3j.lush.core.ticket.Ticket;
import com.px3j.lush.endpoint.http.LushControllerMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * This is an example controller that shows how you can take advantage of Lush in your applications Controller layer.
 *
 *  @author Paul Parrone
 */
@Slf4j
@RestController
@RequestMapping("/lush/app")
public class SmokeTestController {
    /**
     * Example controller method that returns a String (wrapped by a Mono) as we are using Spring WebFlux.
     *
     * A few things to note:
     * <ul>
     *     <li>@PreAuthorize is automatically wired to recognize a Ticket as authenticated.</li>
     *     <li>This controller doesn't use the Ticket so it isn't a parameter, see below for how to have ticket injected.</li>
     * </ul>
     *
     * @return A Mono wrapping a hard-coded String
     */
    @LushControllerMethod
    @RequestMapping("ping")
    @PreAuthorize("isAuthenticated()")
    public Mono<String> ping() {
        log.info( "ping() has been called" );
        return Mono.just( "Powered By Lush" );
    }

    /**
     * This method illustrates how to use the ticket in a controller method.  Lush will automatically inject it if it
     * is a declared parameter to the method.
     *
     * @param ticket The ticket representing the user triggering this request.
     * @return A Mono wrapping a String that says hi.
     */
    @LushControllerMethod
    @RequestMapping("sayHi")
    @PreAuthorize("isAuthenticated()")
    public Mono<String> sayHi( Ticket ticket) {
        return Mono.just( String.format( "%s says hi!", ticket.getUsername()) );
    }

    /**
     * This method shows how Lush will intercept an exception and inject its response protocol to signal the error
     * to the caller.
     */
    @LushControllerMethod
    @RequestMapping("uae")
    @PreAuthorize("isAuthenticated()")
    public Mono<String> uae( Ticket ticket) {
        if( true ) {
            throw new LushException( "Illustrate Exception Handling" );
        }

        return Mono.just( String.format( "%s says hi!", ticket.getUsername()) );
    }

    /**
     * This method shows how to use the Lush Advice concept to return meaningful information back to your caller in
     * addition to some data response.
     */
    @LushControllerMethod
    @RequestMapping("withAdvice")
    @PreAuthorize("isAuthenticated()")
    public Mono<String> withAdvice(Ticket ticket, LushContext context ) {
        Advice advice = context.getAdvice();

        /* You can set a status code in advice - this is not the same as the HTTP Status Code */
        advice.setStatusCode( 555 );

        /* You can add extra information to return via Advice */
        advice.putExtra( "recommendation", "use Lush" );

        /* You can add warnings to advice */
        advice.addWarning( new Advice.Warning(1, Map.of( "collision", "field1,field2")));

        return Mono.just( "Advice Attached" );

    }

    @LushControllerMethod
    @RequestMapping("xray")
    @PreAuthorize("isAuthenticated()")
    public Mono<String> xray(Ticket ticket, LushContext context ) {
        Advice advice = context.getAdvice();

        log.info( "Injected ticket: {}", ticket.toString() );
        log.info( "Injected context: {}", context);

        /* You can set a status code in advice - this is not the same as the HTTP Status Code */
        advice.setStatusCode( 555 );

        /* You can add extra information to return via Advice */
        advice.putExtra( "recommendation", "use Lush" );

        /* You can add warnings to advice */
        advice.addWarning( new Advice.Warning(1, Map.of( "collision", "field1,field2")));

        return Mono.just( "Advice Attached" );

    }




}
